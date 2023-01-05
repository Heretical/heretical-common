public enum DurationField  implements TemporalField{
    SIXTH_OF_DAY("SixthOfDay", DurationUnit.SIXTHS, DAYS, ValueRange.of(0, 24 * 60 / 10)),
    TWELFTH_OF_DAY("TwelfthOfDay", DurationUnit.TWELFTHS, DAYS, ValueRange.of(0, 24 * 60 / 5));

    private final String name;
    private final TemporalUnit baseUnit;
    private final TemporalUnit rangeUnit;
    private final ValueRange range;

    DurationField(String name, TemporalUnit baseUnit, TemporalUnit rangeUnit, ValueRange range) {
        this.name = name;
        this.baseUnit = baseUnit;
        this.rangeUnit = rangeUnit;
        this.range = range;
    }

    @Override
    public String getDisplayName(Locale locale) {
        Objects.requireNonNull(locale, "locale");
        return name;
    }

    @Override
    public TemporalUnit getBaseUnit() {
        return baseUnit;
    }

    @Override
    public TemporalUnit getRangeUnit() {
        return rangeUnit;
    }

    /**
     * Gets the range of valid values for the field.
     * <p>
     * All fields can be expressed as a {@code long} integer.
     * This method returns an object that describes the valid range for that value.
     * <p>
     * This method returns the range of the field in the ISO-8601 calendar system.
     * This range may be incorrect for other calendar systems.
     * Use {@link Chronology#range(ChronoField)} to access the correct range
     * for a different calendar system.
     * <p>
     * Note that the result only describes the minimum and maximum valid values
     * and it is important not to read too much into them. For example, there
     * could be values within the range that are invalid for the field.
     *
     * @return the range of valid values for the field, not null
     */
    @Override
    public ValueRange range() {
        return range;
    }

    //-----------------------------------------------------------------------

    /**
     * Checks if this field represents a component of a date.
     * <p>
     * Fields from day-of-week to era are date-based.
     *
     * @return true if it is a component of a date
     */
    @Override
    public boolean isDateBased() {
        return false;
    }

    /**
     * Checks if this field represents a component of a time.
     * <p>
     * Fields from nano-of-second to am-pm-of-day are time-based.
     *
     * @return true if it is a component of a time
     */
    @Override
    public boolean isTimeBased() {
        return true;
    }

    //-----------------------------------------------------------------------

    /**
     * Checks that the specified value is valid for this field.
     * <p>
     * This validates that the value is within the outer range of valid values
     * returned by {@link #range()}.
     * <p>
     * This method checks against the range of the field in the ISO-8601 calendar system.
     * This range may be incorrect for other calendar systems.
     * Use {@link Chronology#range(ChronoField)} to access the correct range
     * for a different calendar system.
     *
     * @param value the value to check
     * @return the value that was passed in
     */
    public long checkValidValue(long value) {
        return range().checkValidValue(value, this);
    }

    /**
     * Checks that the specified value is valid and fits in an {@code int}.
     * <p>
     * This validates that the value is within the outer range of valid values
     * returned by {@link #range()}.
     * It also checks that all valid values are within the bounds of an {@code int}.
     * <p>
     * This method checks against the range of the field in the ISO-8601 calendar system.
     * This range may be incorrect for other calendar systems.
     * Use {@link Chronology#range(ChronoField)} to access the correct range
     * for a different calendar system.
     *
     * @param value the value to check
     * @return the value that was passed in
     */
    public int checkValidIntValue(long value) {
        return range().checkValidIntValue(value, this);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isSupportedBy(TemporalAccessor temporal) {
        return temporal.isSupported(ChronoField.MINUTE_OF_DAY);
    }

    @Override
    public ValueRange rangeRefinedBy(TemporalAccessor temporal) {
        return range;
    }

    @Override
    public long getFrom(TemporalAccessor temporal) {
        if (temporal instanceof Instant) {
            temporal = ((Instant) temporal).atZone(ZoneOffset.UTC);
        }

        int i = temporal.get(ChronoField.MINUTE_OF_DAY);

        Duration duration = baseUnit.getDuration();

        return Math.floorDiv(i, duration.toMinutes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Temporal> R adjustInto(R temporal, long newValue) {
        return (R) temporal.with(this, newValue);
    }

    @Override
    public TemporalAccessor resolve(Map<TemporalField, Long> fieldValues, TemporalAccessor partialTemporal, ResolverStyle resolverStyle) {
        Long value = fieldValues.remove(this);

        if (value == null) {
            throw new IllegalStateException("field missing " + this);
        }

        fieldValues.put(ChronoField.MINUTE_OF_DAY, baseUnit.getDuration().multipliedBy(value).toMinutes());

        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return name;
    }
    
}
