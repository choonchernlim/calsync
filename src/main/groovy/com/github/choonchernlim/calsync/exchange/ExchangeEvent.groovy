package com.github.choonchernlim.calsync.exchange

import groovy.transform.ToString
import org.joda.time.DateTime

@ToString(includeNames = true)
class ExchangeEvent {
    DateTime startDateTime
    DateTime endDateTime
    String subject
    String location
    Boolean isReminderSet
    Integer reminderMinutesBeforeStart
    String body
    Boolean isCanceled
    Boolean isAllDayEvent
    List<Attendee> optionalAttendees
    List<Attendee> requiredAttendees
    String organizerAddress
    String organizerName
    Boolean isBusy

    static class Attendee {
        String address
        String name
        String response
    }
}
