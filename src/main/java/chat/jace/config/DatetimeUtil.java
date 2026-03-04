package chat.jace.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class DatetimeUtil {
    private final static DateTimeFormatter ymdhmsFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static DateTimeFormatter ddMMyyyyFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final static DateTimeFormatter timeFormater = DateTimeFormatter.ofPattern("H'h'mm");

    private final static DateTimeFormatter ddMMFormat = DateTimeFormatter.ofPattern("dd/MM");

    public static final LocalDateTime getBeginingOfTheDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(0, 0));
    }

    public static final LocalDateTime getEndingOfTheDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.of(23, 59, 59));
    }

    public static final LocalDateTime getBeginingOfToday() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0));
    }

    public static String normalizeDateFormat(LocalDateTime dt){
        return ymdhmsFormat.format(dt);
    }

    public static String normalizeNowDateFormat(){
        return normalizeDateFormat(LocalDateTime.now());
    }

    public static String normalizeDDMMYYYYFormat(LocalDateTime dt) {
        return ddMMyyyyFormat.format(dt);
    }

    public static String normalizeTimeFormat(LocalTime from, LocalTime to) {
        String fromFormatted = from.format(timeFormater);
        String toFormatted = to.format(timeFormater);

        if (from.getMinute() == 0) {
            fromFormatted = fromFormatted.replace("00", "");
        }

        if (to.getMinute() == 0) {
            toFormatted = toFormatted.replace("00", "");
        }

        return fromFormatted + "-" + toFormatted;
    }

    public static boolean isAfterOrEqual(LocalTime now, LocalTime from) {
        return now.isAfter(from) || Objects.equals(now, from);
    }

    public static boolean isBeforeOrEqual(LocalTime now, LocalTime to) {
        return now.isBefore(to) || Objects.equals(now, to);
    }

    public static boolean nowIsInTimeSlot(LocalTime from, LocalTime to) {
        LocalTime now = LocalTime.now();
        return (now.isAfter(from) || now.equals(from)) && (now.isBefore(to) || now.equals(to));
    }

    public static List<LocalDate> generateDateRange(LocalDate fromAt, LocalDate toAt) {
        List<LocalDate> dateRange = new ArrayList<>();

        LocalDate current = fromAt;
        while (!current.isAfter(toAt)) {
            dateRange.add(current);
            current = current.plusDays(1);
        }

        return dateRange;
    }

    public static String timeToStringWithH(LocalTime localTime) {
        if (Objects.isNull(localTime)) {
            return "";
        }
        if (localTime.getMinute() == 0) {
            return localTime.getHour() + "h";
        }
        return localTime.getHour() + "h" + localTime.getMinute();
    }

    public static LocalDateTime max(LocalDateTime t1, LocalDateTime t2) {
        return t1.isAfter(t2) ? t1 : t2;
    }

    public static String normalizeDDMMFormat(LocalDate dt) {
        return ddMMFormat.format(dt);
    }

    public static boolean isAfterOrEqual(LocalDateTime now, LocalDateTime from) {
        return now.isAfter(from) || Objects.equals(now, from);
    }

    public static boolean isBeforeOrEqual(LocalDateTime now, LocalDateTime to) {
        return now.isBefore(to) || Objects.equals(now, to);
    }

    public static boolean isInSameWeek(LocalDate date1, LocalDate date2) {
        LocalDate startOfWeekForDate1 = date1.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate startOfWeekForDate2 = date2.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return startOfWeekForDate1.equals(startOfWeekForDate2)
                && startOfWeekForDate1.getYear() == startOfWeekForDate2.getYear();
    }

    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        return day == Calendar.SUNDAY ? 7 : day - 1; // in calendar SUNDAY = 1
    }
}
