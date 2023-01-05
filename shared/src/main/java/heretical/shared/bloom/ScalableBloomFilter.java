
/**
 * This is a hack on the Scalable Bloom Filter paper. It does not support partitioning for example.
 * <p>
 * That said, a Stable Bloom Filter would be more appropriate, if not a real Scalable one with better perf and memory.
 * <p>
 * But this version is suitable for batch processing where we will only see a few million unique keys.
 * <p>
 * Update, are actually seeing 10's of millions of unique keys in a batch, if not 100's of million.
 * <p>
 * ╔═════════════════════╤════════╤════════════╤═════════════════════╤════════════╤════════════════════╤═════════╤════════════════════════════╗
 * ║ Type                │ Rate   │ Num Values │ Expected Insertions │ Target FPP │ False Positives    │ Greater │ Expected FPP               ║
 * ╠═════════════════════╪════════╪════════════╪═════════════════════╪════════════╪════════════════════╪═════════╪════════════════════════════╣
 */

 public class ScalableBloomFilter {
    
    private static final Logger LOG = LoggerFactory.getLogger(ScalableBloomFilter.class);
    public static final float ERROR_PROBABILITY_RATIO = 0.5F;
    public static final float FALSE_POSITIVE_PROBABILITY = 0.001F;
    public static final int INSERT_TEST_RATE = 10;
    private final String name;
    private final Rate growthRate;
    private final float errorProbabilityRatio;
    private final long initialCapacity;
    private final double falsePositiveProbability;
    private final LinkedList<BloomFilter<ByteBuffer>> bloomFilters = new LinkedList<>();
    private long insertCount = 0;
    private Function<ByteBuffer, Boolean> put = this::putSingle;

    public enum Rate {
        SLOW(Math.sqrt(2)), MEDIUM(2), FAST(4);

        public double rate;

        Rate(double rate) {
            this.rate = rate;
        }
    }

    public ScalableBloomFilter(Rate growthRate, int initialCapacity, double falsePositiveProbability) {
        this(growthRate, ERROR_PROBABILITY_RATIO, initialCapacity, falsePositiveProbability);
    }

    public ScalableBloomFilter(Rate growthRate, float errorProbabilityRatio, long initialCapacity, double falsePositiveProbability) {
        this(null, growthRate, errorProbabilityRatio, initialCapacity, falsePositiveProbability);
    }

    public ScalableBloomFilter(String name, Rate growthRate, float errorProbabilityRatio, long initialCapacity, double falsePositiveProbability) {
        this.name = Objects.requireNonNullElse(name, "default");
        this.growthRate = growthRate;
        this.errorProbabilityRatio = errorProbabilityRatio;
        this.initialCapacity = initialCapacity;
        this.falsePositiveProbability = falsePositiveProbability;

        this.bloomFilters.addLast(getBloomFilter());
    }

    public long getInsertCount() {
        return insertCount;
    }

    public int size() {
        return bloomFilters.size();
    }

    /**
     * Accept byte array so we don't have to serialize the tuple if there is a miss
     *
     * @param tuple
     * @return
     */
    public boolean put(ByteBuffer tuple) {
        // an attempt to prevent mightContain calls when we only have one filter to test
        return put.apply(tuple);
    }

    public boolean putScaled(ByteBuffer tuple) {
        if (mightContain(tuple)) {
            return true;
        }

        // insert at beginning since we are assuming older values are less likely
        if ((insertCount % INSERT_TEST_RATE) == 0 && isExpected()) {
            bloomFilters.addFirst(getBloomFilter());
        }

        boolean unique = bloomFilters.getFirst().put(tuple);

        if (!unique) {
            LOG.warn("bloom filter bit set did not change on insertion");
            return true;
        }

        insertCount++;

        return false;
    }

    public boolean putSingle(ByteBuffer tuple) {
        // insert at beginning since we are assuming older values are less likely
        if ((insertCount % INSERT_TEST_RATE) == 0 && isExpected()) {
            bloomFilters.addFirst(getBloomFilter());
            put = this::putScaled;
        }

        boolean unique = bloomFilters.getFirst().put(tuple);

        // did not contain, so mightContain would be false
        if (unique) {
            insertCount++;

            return false;
        }

        return true;
    }

    protected boolean isExpected() {
        return bloomFilters.getFirst().expectedFpp() > getScaledFPP(bloomFilters.size() - 1);
    }

    public boolean mightContain(ByteBuffer tuple) {
        for (BloomFilter<ByteBuffer> bloomFilter : bloomFilters) {
            if (bloomFilter.mightContain(tuple))
                return true;
        }

        return false;
    }

    public double[] expectedFpp() {
        double[] result = new double[bloomFilters.size()];

        int count = 0;
        for (BloomFilter<ByteBuffer> bloomFilter : bloomFilters) {
            result[count++] = bloomFilter.expectedFpp();
        }

        return result;
    }

    private BloomFilter<ByteBuffer> getBloomFilter() {
        int size = bloomFilters.size();
        double scaledCapacity = getScaledCapacity(size);
        double scaledFPP = getScaledFPP(size);

        LOG.info("creating bloom filter: {}, num: {}, with scaledCapacity: {}, scaledFalsePositiveProbability: {}", name, size + 1, scaledCapacity, scaledFPP);
        LogUtil.logMemory(LOG, "memory before bloom scaling");

        try {
            return BloomFilter.create(getFunnel(), (int) scaledCapacity, scaledFPP);
        } finally {
            LogUtil.logMemory(LOG, "memory after bloom scaling");
        }
    }

    private double getScaledCapacity(int size) {
        return initialCapacity * Math.pow(growthRate.rate, size);
    }

    private double getScaledFPP(int size) {
        return falsePositiveProbability * Math.pow(errorProbabilityRatio, size);
    }

    private Funnel<ByteBuffer> getFunnel() {
        return (from, into) -> into.putBytes(from.rewind());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ScalableBloomFilter.class.getSimpleName() + "[", "]")
                .add("name=" + name)
                .add("growthRate=" + growthRate)
                .add("errorProbabilityRatio=" + errorProbabilityRatio)
                .add("initialCapacity=" + initialCapacity)
                .add("falsePositiveProbability=" + falsePositiveProbability)
                .toString();
    }
}
