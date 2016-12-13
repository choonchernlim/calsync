package com.github.choonchernlim.calsync.exchange

import groovy.transform.ToString
import org.joda.time.DateTime

@ToString(includeNames = true)
class ExchangeEvent {
    DateTime startDateTime
    DateTime endDateTime
    String subject
    String location
    Integer reminderMinutesBeforeStart
    String body
    Boolean isCanceled
}
