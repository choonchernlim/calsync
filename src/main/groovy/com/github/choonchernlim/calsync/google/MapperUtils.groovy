package com.github.choonchernlim.calsync.google

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
                startDateTime: toJodaDateTime(event.getStart()),
                endDateTime: toJodaDateTime(event.getEnd()),
                subject: event.getSummary(),
                location: event.getLocation()
        )
    }

    /**
     * Maps CalSyncEvent to Google Event.
     *
     * @param calSyncEvent CalSyncEvent
     * @return Google Event
     */
    static Event toGoogleEvent(CalSyncEvent calSyncEvent) {
        assert calSyncEvent != null

        return new Event(
                id: calSyncEvent.googleEventId,
                start: toGoogleEventDateTime(calSyncEvent.startDateTime),
                end: toGoogleEventDateTime(calSyncEvent.endDateTime),
                summary: calSyncEvent.subject,
                location: calSyncEvent.location,
                reminders: new Event.Reminders(
                        useDefault: true,

                        )
        )
    }

    /**
     * Creates new {@link CalSyncEvent} object.
     *
     * @param startDateTime Start datetime
     * @param endDateTime End datetime
     * @param summary Event title
     * @param location Location
     * @return CalSyncEvent
     */
    static CalSyncEvent toCalSyncEvent(
            org.joda.time.DateTime startDateTime,
            org.joda.time.DateTime endDateTime,
            String subject,
            String location = null) {
        assert startDateTime != null && endDateTime != null && startDateTime <= endDateTime
        assert subject?.trim()

        return new CalSyncEvent(
                startDateTime: startDateTime,
                endDateTime: endDateTime,
                subject: subject,
                location: location
        )
    }
}
