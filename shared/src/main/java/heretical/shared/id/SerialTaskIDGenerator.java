
/**
 * Generate a long id that is a composite based on
 * <p>
 * max time: 34359738367, days: 397 (remaining: 395), years: 1 (remaining: 1)
 * max seq: 127
 * max task: 4194303, random collision prob: 50%: 2047.999756, 10% 915.893334, 1%: 289.630903, .1%: 91.589333
 * <p>
 * Where sequence is the number of events within a millisecond, reset every new millisecond.
 * <p>
 * And max task is the maximum value a given taskId will have after being masked. if using a random number or hash for the
 * task id, note sqrt(max task) is the number for unique (simultaneous/overlapping) tasks that if launched
 * will have a 50% likelihood task id of collision.
 * <p>
 * task id only matters if there are a large number of concurrent tasks generating ids that risk collision.
 * <p>
 * <p>
 * 'currentTimeMillis - EPOCH' + 'sequence' + 'task id'
 * <p>
 * timestamp is leftmost is as sequence will likely remain zero for most ids. and task id remains constant for a process.
 * <p>
 * reversing the order and then the bytes is an option, but hadoop raw comparators still deserialize longs into primitives
 * for comparison, not against byte arrays.
 */

 public class SerialTaskIDGenerator  implements IDGenerator<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(SerialTaskIDGenerator.class);

    public static final long EPOCH = 1624297628069L;

    long sequenceBits = 7L;
    long taskIdBits = 22L;
    long timeBits = Long.SIZE - sequenceBits - taskIdBits;

    long sequenceLeftShift = taskIdBits;
    long timestampLeftShift = sequenceBits + taskIdBits;
    long timeMask = ~(-1L << timeBits);
    long sequenceMask = ~(-1L << sequenceBits);
    long taskMask = ~(-1L << taskIdBits);

    Clock clock;
    long tickCount = 0;
    long taskId;
    long lastTimestamp = 0L;
    long sequence = 0;

    public SerialTaskIDGenerator(long taskId) {
        this(Clock.systemUTC(), taskId);
    }

    public SerialTaskIDGenerator(Clock clock, long taskId) {
        this.clock = clock;
        this.taskId = taskId & taskMask;
    }

    public long getSequenceBits() {
        return sequenceBits;
    }

    public long getTaskIdBits() {
        return taskIdBits;
    }

    public long getTimeBits() {
        return timeBits;
    }

    public long getTimeMask() {
        return timeMask;
    }

    public long getSequenceMask() {
        return sequenceMask;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getTaskMask() {
        return taskMask;
    }

    @Override
    public Long next() {
        long timestamp = clock.millis();

        if (timestamp < lastTimestamp) {
            throw new IllegalStateException("system clock went backwards");
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = nextTick(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        long timestampEpoch = (timestamp - EPOCH);
        long timestampShifted = (timestampEpoch << timestampLeftShift);
        long sequenceShifted = sequence << sequenceLeftShift;

        long result = timestampShifted | sequenceShifted | taskId;

        if (false) {
            System.out.println("timestamp = " + Long.toHexString(timestampShifted));
            System.out.println("sequence = " + Long.toHexString(sequenceShifted));
            System.out.println("task = " + Long.toHexString(taskId));
            System.out.println("result = " + Long.toHexString(result));
        }

        return result;
    }

    protected long nextTick(long lastTimestamp) {
        try {
            long timestamp = clock.millis();
            while (timestamp <= lastTimestamp) {
                timestamp = clock.millis();
            }
            return timestamp;
        } finally {
            if (tickCount++ % 1000 == 0) {
                LOG.warn("sequence id has rolled over forcing a delay until the next tick, max sequence: {}, num delays: {}", sequenceMask, tickCount);
            }
        }
    }

    public String printStats() {
        long days = Duration.ofMillis(timeMask).toDays();
        long remaining = Duration.ofMillis(timeMask - ((System.currentTimeMillis() - EPOCH) & timeMask)).toDays();
        double p50 = Math.sqrt(taskMask);
        double p10 = Math.sqrt(2 * taskMask * .1);
        double p01 = Math.sqrt(2 * taskMask * .01);
        double p001 = Math.sqrt(2 * taskMask * .001);
        return format("max time: %d, days: %d (remaining: %d), years: %d (remaining: %d)\n", timeMask, days, remaining, days / 365, remaining / 365) +
                format("max seq: %d\n", sequenceMask) +
                format("max task: %d, random collision prob: 50%%: %f, 10%% %f, 1%%: %f, .1%%: %f\n", taskMask, p50, p10, p01, p001);
    }
    
}
