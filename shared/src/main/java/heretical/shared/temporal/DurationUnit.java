public enum DurationUnit implements TemporalUnit {
    SIXTHS("Sixth", Duration.ofMinutes(10)),
    TWELFTHS("Twelfth", Duration.ofMinutes(5));

    private final String name;
    private final Duration duration;

    DurationUnit(String name, Duration estimatedDuration) {
        this.name = name;
        this.duration = estimatedDuration;
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public boolean isDurationEstimated() {
        return false;
    }

    @Override
    public boolean isDateBased() {
        return false;
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @Override
    public boolean isSupportedBy(Temporal temporal) {
        return temporal.isSupported(MINUTE_OF_DAY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        switch (this) {
            case SIXTHS:
                return (R) temporal.plus(10 * amount, ChronoUnit.MINUTES);
            case TWELFTHS:
                return (R) temporal.plus(5 * amount, ChronoUnit.MINUTES);
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    @Override
    public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        if (temporal1Inclusive.getClass() != temporal2Exclusive.getClass()) {
            return temporal1Inclusive.until(temporal2Exclusive, this);
        }
        switch (this) {
            case SIXTHS:
                return temporal1Inclusive.until(temporal2Exclusive, MINUTES) / 10;
            case TWELFTHS:
                return temporal1Inclusive.until(temporal2Exclusive, MINUTES) / 5;
            default:
                throw new IllegalStateException("Unreachable");
        }
    }

    @Override
    public String toString() {
        return name;
    }
    
}
