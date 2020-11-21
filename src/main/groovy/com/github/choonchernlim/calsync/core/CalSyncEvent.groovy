package com.github.choonchernlim.calsync.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.joda.time.DateTime

/**
 * Properties that this app cares across different calendar apps.
 *
 * Equals/HashCode allows a quick diff between calendar events.
 * organizerAddress and organizerName are ignored, because Google might change the organizer to the current calendar
 */
@EqualsAndHashCode(excludes = ['googleEventId', 'organizerAddress', 'organizerName'])
@ToString(includeNames = true)
class CalSyncEvent {
    DateTime startDateTime
    DateTime endDateTime
    String subject
    String location
    Integer reminderMinutesBeforeStart
    String body
    Boolean isAllDayEvent
    List<Attendee> attendees
    String organizerAddress
    String organizerName
    Boolean isBusy

    String googleEventId

    @EqualsAndHashCode
    static class Attendee {
        String address
        String name
        Response response
        Boolean isOptional

        static enum Response {
            ACCEPTED, DECLINED, TENTATIVE, NO_RESPONSE
        }
    }
}
