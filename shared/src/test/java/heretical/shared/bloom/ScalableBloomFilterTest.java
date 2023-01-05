@Ignore
public class ScalableBloomFilterTest {
    static final String[] HEADERS = {"Type", "Rate", "Num Values", "Expected Insertions", "Target FPP", "False Positives", "Greater", "Expected FPP", "Put Dur", "TP Dur", "FP Dur"};
    static final double EXPECTED_INSERTIONS_PERCENT = 0.5;
    static final double[] FPP = {0.001, 0.0001, 0.00001, 0.000001, 0.0000001};
    private final byte[] writeBuffer = new byte[8];
    ExposedByteArrayOutputStream bytes = new ExposedByteArrayOutputStream();
    DataOutputStream stream = new DataOutputStream(bytes);

    private static String[] row(ScalableBloomFilter filterType, double fpp, ScalableBloomFilter.Rate growthRate, long capacity, long expectedInsertions, long falsePositives, double falsePositiveRate, double[] expectedFPP, Duration[] durations) {
        return new String[]{
                filterType.getClass().getSimpleName(),
                growthRate.name(),
                String.format("%,d", capacity),
                String.format("%,d", expectedInsertions),
                String.format("%.6f %%", 100 * fpp),
                String.format("%,d (%.6f %%)", falsePositives, 100 * falsePositiveRate),
                falsePositiveRate > fpp ? "!" : "",
                Arrays.stream(expectedFPP).mapToObj(v -> String.format("(%.6f %%)", 100 * v)).collect(Collectors.joining(", ")),
                durations[0].toString(),
                durations[1].toString(),
                durations[2].toString()
        };
    }

    private static void printTable(List<String[]> rows) {
        System.out.println(FlipTable.of(HEADERS, rows.toArray(new String[0][])));
    }

    @Test
    public void testLong() {
        test(l -> l, this::writeLong);
    }

    public final ByteBuffer writeLong(Object o) {
        long v = (long) o;
        writeBuffer[0] = (byte) (v >>> 56);
        writeBuffer[1] = (byte) (v >>> 48);
        writeBuffer[2] = (byte) (v >>> 40);
        writeBuffer[3] = (byte) (v >>> 32);
        writeBuffer[4] = (byte) (v >>> 24);
        writeBuffer[5] = (byte) (v >>> 16);
        writeBuffer[6] = (byte) (v >>> 8);
        writeBuffer[7] = (byte) (v);

        return ByteBuffer.wrap(writeBuffer);
    }

    @Test
    public void testLongString() {
        test(Long::toString, this::writeLongString);
    }

    public final ByteBuffer writeLongString(Object v) {
        try {
            bytes.reset();
            stream.writeChars(v.toString());
            stream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return bytes.toByteBuffer();
    }

    public void test(LongFunction<Object> serializer, Function<Object, ByteBuffer> writer) {
        List<String[]> rows = new ArrayList<>();

        try {
            for (double fpp : FPP) {
                long i = 2 << 10;
                long max = 2 << 23;
                for (long capacity = i; capacity < max; capacity = capacity << 2) {
                    long[] input = new Random(1).longs(capacity).distinct().toArray();
                    int expectedInsertions = (int) (capacity * EXPECTED_INSERTIONS_PERCENT);

                    for (ScalableBloomFilter.Rate growthRate : ScalableBloomFilter.Rate.values()) {

                        ScalableBloomFilter filter = new ScalableBloomFilter(growthRate, expectedInsertions, fpp);

                        Duration[] durations = new Duration[3];

                        int falsePositives = testFalsePositives(serializer, writer, filter, input, durations);
                        double falsePositiveRate = ((double) falsePositives / expectedInsertions);
                        rows.add(row(filter, fpp, growthRate, input.length, expectedInsertions, falsePositives, falsePositiveRate, filter.expectedFpp(), durations));
                    }
                }
            }
        } finally {
            printTable(rows);
        }
    }

    private int testFalsePositives(LongFunction<Object> serializer, Function<Object, ByteBuffer> writer, ScalableBloomFilter filter, long[] input, Duration[] durations) {
        int falsePositives = 0;
        int truePositives = 0;
        int i = 0;

        Object[] objects = LongStream.of(input).mapToObj(serializer).toArray();

        Stopwatch putWatch = Stopwatch.createStarted();
        for (; i < (input.length / 2); i++) {
            filter.put(writer.apply(objects[i]));
        }
        putWatch.stop();

        Stopwatch trueWatch = Stopwatch.createStarted();
        for (int k = 0; k < i; k++) {
            truePositives += filter.mightContain(writer.apply(objects[k])) ? 1 : 0;
        }
        trueWatch.stop();

        assertEquals(truePositives, input.length / 2);

        Stopwatch falseWatch = Stopwatch.createStarted();
        for (; i < input.length; i++) {
            falsePositives += filter.mightContain(writer.apply(objects[i])) ? 1 : 0;
        }
        falseWatch.stop();

        durations[0] = putWatch.elapsed();
        durations[1] = trueWatch.elapsed();
        durations[2] = falseWatch.elapsed();

        return falsePositives;
    }    
}
