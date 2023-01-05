public class SerialTaskIDGeneratorTest {
    @Test
    public void generate() {
        SerialTaskIDGenerator generator = new SerialTaskIDGenerator(Hashing.sipHash24().hashUnencodedChars("123").asLong());

        System.out.println(generator.printStats());

        int maxValue = 1_000_000;
        Set<Long> results = LongStream.range(0, maxValue)
                .mapToObj(l -> generator.next())
                .peek(l -> sleep((int) (1 * Math.random())))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertEquals(maxValue, results.size());

//        results.stream().limit(100).map(Long::toHexString).peek(System.out::println).count();
//        results.stream().skip(maxValue-100).map(Long::toHexString).peek(System.out::println).count();
    }

    private void sleep(int duration) {
        try {
            Thread.sleep(0, (int) TimeUnit.MILLISECONDS.toNanos(duration));
        } catch (InterruptedException e) {
            // do nothing
        }
    }
    
}
