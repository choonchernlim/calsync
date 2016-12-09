package com.github.choonchernlim.calsync.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.joda.time.DateTime

/**
 * Properties that this app cares across different calendar apps.
 *
 * Equals/HashCode allows a quick diff between calendar events.
 */
@EqualsAndHashCode(excludes = ['googleEventId'])
@ToString
class CalSyncEvent {
    DateTime startDateTime
    DateTime endDateTime
    String subject
    String location

    String googleEventId
}
