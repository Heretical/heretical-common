public class TemporalUnits {
    public static TemporalUnit find(String name) {
        Enum<?> unit;
        try {
            unit = ChronoUnit.valueOf(name);
        } catch (IllegalArgumentException e) {
            unit = DurationUnit.valueOf(name);
        }

        return (TemporalUnit) unit;
    }

    public static DateTimeFormatter formatter(TemporalUnit unit) {
        if (unit == DurationUnit.SIXTHS) {
            return DurationDateTimeFormatter.SIXTH_DURATION_FORMATTER;
        }
        if (unit == DurationUnit.TWELFTHS) {
            return DurationDateTimeFormatter.TWELFTH_DURATION_FORMATTER;
        }

        return DateTimeFormatter.ISO_INSTANT;
    }
    
}
