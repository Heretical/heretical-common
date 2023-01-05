public class DurationDateTimeFormatter {
    public static final DateTimeFormatter SIXTH_FORMATTER;
    public static final DateTimeFormatter SIXTH_DURATION_FORMATTER;
    public static final DateTimeFormatter TWELFTH_FORMATTER;
    public static final DateTimeFormatter TWELFTH_DURATION_FORMATTER;

    static {
        SIXTH_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('s')
                .appendValue(SIXTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }

    static {
        SIXTH_DURATION_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(SIXTH_OF_DAY.getBaseUnit().getDuration().toString())
                .appendValue(SIXTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }

    static {
        TWELFTH_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('t')
                .appendValue(TWELFTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }

    static {
        TWELFTH_DURATION_FORMATTER = new DateTimeFormatterBuilder()
                .parseStrict()
                .appendValue(YEAR, 4)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral(TWELFTH_OF_DAY.getBaseUnit().getDuration().toString())
                .appendValue(TWELFTH_OF_DAY, 3)
                .toFormatter()
                .withZone(ZoneOffset.UTC);
    }
    
}
