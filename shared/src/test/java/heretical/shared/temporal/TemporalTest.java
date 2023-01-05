public class TemporalTest {
    public static final ZoneId UTC = ZoneId.of("UTC");

    @Test
    public void sixths() {

        // Tuesday, May 4, 2021 4:59:30.871 PM
        Instant instant = Instant.ofEpochMilli(1620147570871L);

        Instant truncated = instant.truncatedTo(DurationUnit.SIXTHS);

        assertEquals(1620147000000L, truncated.toEpochMilli());

        int i = instant.get(DurationField.SIXTH_OF_DAY);
        int expected = (16 * 60 / 10) + (59 / 10);
        assertEquals(expected, i);

        String format = DurationDateTimeFormatter.SIXTH_FORMATTER.format(instant);

        assertEquals("20210504s101", format);
    }

    @Test
    public void twelfths() {

        // Tuesday, May 4, 2021 4:59:30.871 PM
        Instant instant = Instant.ofEpochMilli(1620147570871L);

        Instant truncated = instant.truncatedTo(DurationUnit.TWELFTHS);

        assertEquals(1620147300000L, truncated.toEpochMilli());

        int i = instant.get(DurationField.TWELFTH_OF_DAY);
        int expected = (16 * 60 / 5) + (59 / 5);
        assertEquals(expected, i);

        String format = DurationDateTimeFormatter.TWELFTH_FORMATTER.format(instant);

        assertEquals("20210504t203", format);
    }

    @Test
    public void sixthsDuration() {

        // Tuesday, May 4, 2021 4:59:30.871 PM
        Instant instant = Instant.ofEpochMilli(1620147570871L);

        Instant truncated = instant.truncatedTo(DurationUnit.SIXTHS);

        assertEquals(1620147000000L, truncated.toEpochMilli());

        int i = instant.get(DurationField.SIXTH_OF_DAY);
        int expected = (16 * 60 / 10) + (59 / 10);
        assertEquals(expected, i);

        String format = DurationDateTimeFormatter.SIXTH_DURATION_FORMATTER.format(instant);

        assertEquals("20210504PT10M101", format);

        TemporalAccessor temporalAccessor = DurationDateTimeFormatter.SIXTH_DURATION_FORMATTER.parse(format);

        LocalDateTime dateTime = temporalAccessor.query(LocalDateTime::from);

        assertEquals(LocalDateTime.ofInstant(truncated, ZoneId.of("UTC")), dateTime);
    }

    @Test
    public void twelfthsDuration() {

        // Tuesday, May 4, 2021 4:59:30.871 PM
        Instant instant = Instant.ofEpochMilli(1620147570871L);

        Instant truncated = instant.truncatedTo(DurationUnit.TWELFTHS);

        assertEquals(1620147300000L, truncated.toEpochMilli());

        int i = instant.get(DurationField.TWELFTH_OF_DAY);
        int expected = (16 * 60 / 5) + (59 / 5);
        assertEquals(expected, i);

        String format = DurationDateTimeFormatter.TWELFTH_DURATION_FORMATTER.format(instant);

        assertEquals("20210504PT5M203", format);


        TemporalAccessor temporalAccessor = DurationDateTimeFormatter.TWELFTH_DURATION_FORMATTER.parse(format);

        LocalDateTime dateTime = temporalAccessor.query(LocalDateTime::from);

        assertEquals(LocalDateTime.ofInstant(truncated, ZoneId.of("UTC")), dateTime);
    }

    @Test
    public void sixthsMaths() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        assertEquals(now.toEpochMilli() + 10 * 60 * 1000, now.plus(1, DurationUnit.SIXTHS).toEpochMilli());

        Instant hour = now.truncatedTo(ChronoUnit.HOURS);

        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(hour, UTC).get(ChronoField.HOUR_OF_DAY));

        Instant first = hour.plus(3, ChronoUnit.MINUTES);
        assertEquals(3, LocalDateTime.ofInstant(first, UTC).get(ChronoField.MINUTE_OF_HOUR));

        first = first.truncatedTo(DurationUnit.SIXTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(first, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(0, LocalDateTime.ofInstant(first, UTC).get(ChronoField.MINUTE_OF_HOUR));

        Instant second = hour.plus(13, ChronoUnit.MINUTES);
        assertEquals(13, LocalDateTime.ofInstant(second, UTC).get(ChronoField.MINUTE_OF_HOUR));

        second = second.truncatedTo(DurationUnit.SIXTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(second, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(10, LocalDateTime.ofInstant(second, UTC).get(ChronoField.MINUTE_OF_HOUR));

        // we are at the beginning of a sixth after truncation, so should remain at this sixth
        Instant third = hour.plus(13, ChronoUnit.MINUTES).truncatedTo(DurationUnit.SIXTHS);
        assertEquals(10, LocalDateTime.ofInstant(third, UTC).get(ChronoField.MINUTE_OF_HOUR));

        third = third.truncatedTo(DurationUnit.SIXTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(third, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(10, LocalDateTime.ofInstant(third, UTC).get(ChronoField.MINUTE_OF_HOUR));

        // add two sixths (20 minutes), then truncate, which should remain at 20 minutes
        Instant fourth = hour.plus(2, DurationUnit.SIXTHS);
        assertEquals(20, LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.MINUTE_OF_HOUR));

        fourth = fourth.truncatedTo(DurationUnit.SIXTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(20, LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.MINUTE_OF_HOUR));
    }

    @Test
    public void twelfthsMaths() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MINUTES);

        assertEquals(now.toEpochMilli() + 5 * 60 * 1000, now.plus(1, DurationUnit.TWELFTHS).toEpochMilli());

        Instant hour = now.truncatedTo(ChronoUnit.HOURS);

        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(hour, UTC).get(ChronoField.HOUR_OF_DAY));

        Instant first = hour.plus(3, ChronoUnit.MINUTES);
        assertEquals(3, LocalDateTime.ofInstant(first, UTC).get(ChronoField.MINUTE_OF_HOUR));

        first = first.truncatedTo(DurationUnit.TWELFTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(first, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(0, LocalDateTime.ofInstant(first, UTC).get(ChronoField.MINUTE_OF_HOUR));

        Instant second = hour.plus(13, ChronoUnit.MINUTES);
        assertEquals(13, LocalDateTime.ofInstant(second, UTC).get(ChronoField.MINUTE_OF_HOUR));

        second = second.truncatedTo(DurationUnit.TWELFTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(second, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(10, LocalDateTime.ofInstant(second, UTC).get(ChronoField.MINUTE_OF_HOUR));

        // we are at the beginning of a twelfth after truncation, so should remain at this twelfth
        Instant third = hour.plus(8, ChronoUnit.MINUTES).truncatedTo(DurationUnit.TWELFTHS);
        assertEquals(5, LocalDateTime.ofInstant(third, UTC).get(ChronoField.MINUTE_OF_HOUR));

        third = third.truncatedTo(DurationUnit.TWELFTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(third, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(5, LocalDateTime.ofInstant(third, UTC).get(ChronoField.MINUTE_OF_HOUR));

        // add two TWELFTHS (20 minutes), then truncate, which should remain at 20 minutes
        Instant fourth = hour.plus(2, DurationUnit.TWELFTHS);
        assertEquals(10, LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.MINUTE_OF_HOUR));

        fourth = fourth.truncatedTo(DurationUnit.TWELFTHS);
        assertEquals(LocalDateTime.ofInstant(now, UTC).get(ChronoField.HOUR_OF_DAY), LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.HOUR_OF_DAY));
        assertEquals(10, LocalDateTime.ofInstant(fourth, UTC).get(ChronoField.MINUTE_OF_HOUR));
    }    
}
