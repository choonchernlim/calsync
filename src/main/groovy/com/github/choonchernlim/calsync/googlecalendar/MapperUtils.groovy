package com.github.choonchernlim.calsync.googlecalendar

import com.github.choonchernlim.calsync.core.CalSyncEvent
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime

class MapperUtils {
    /**
     * Maps Google EventDateTime to Joda DateTime.
     *
     * @param eventDateTime Google EventDateTime
     * @return Joda DateTime
     */
    static org.joda.time.DateTime toJodaDateTime(EventDateTime eventDateTime) {
        assert eventDateTime != null

        return new org.joda.time.DateTime(eventDateTime.getDateTime().getValue())
    }

    /**
     * Maps Joda DateTime to Google EventDateTime.
     *
     * @param jodaDateTime Joda DateTime
     * @return Google EventDateTime
     */
    static EventDateTime toGoogleEventDateTime(org.joda.time.DateTime jodaDateTime) {
        assert jodaDateTime != null

        return new EventDateTime(dateTime: toGoogleDateTime(jodaDateTime))
    }

    /**
     * Maps Joda DateTime to Google DateTime.
     *
     * @param jodaDateTime Joda DateTime
     * @return Google DateTime
     */
    static DateTime toGoogleDateTime(org.joda.time.DateTime jodaDateTime) {
        assert jodaDateTime != null

        return new DateTime(jodaDateTime.millis)
    }

    /**
     * Maps Google Event to CalSyncEvent.
     *
     * @param event Google Event
     * @return CalSyncEvent
     */
    static CalSyncEvent toCalSyncEvent(Event event) {
        assert event != null

        return new CalSyncEvent(
                googleEventId: event.getId(),
                startDatetime: toJodaDateTime(event.getStart()),
                endDatetime: toJodaDateTime(event.getEnd()),
                subject: event.getSummary(),
                location: event.getLocation()
        )
    }
}
