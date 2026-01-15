package ride.example.demo.controller;

import java.time.LocalDateTime;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateUtils {

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar calendar) {
        if (calendar == null) return null;
        return calendar.toGregorianCalendar()
                       .toZonedDateTime()
                       .toLocalDateTime();
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        try {
            javax.xml.datatype.DatatypeFactory df = javax.xml.datatype.DatatypeFactory.newInstance();
            return df.newXMLGregorianCalendar(
                localDateTime.getYear(),
                localDateTime.getMonthValue(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHour(),
                localDateTime.getMinute(),
                localDateTime.getSecond(),
                localDateTime.getNano() / 1_000_000, // millisecondes
                0 // timezone = 0 pour UTC
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
