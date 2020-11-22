package com.github.choonchernlim.calsync.core

import groovy.transform.ToString

/**
 * User specific configuration.
 */
@ToString
class UserConfig {
    String exchangeUserName
    String exchangePassword
    String exchangeUrl
    Boolean exchangeSleepOnConnectionError
    String googleClientSecretJsonFilePath
    String googleCalendarName
    Integer totalSyncDays
    Integer nextSyncInMinutes
    Boolean includeCanceledEvents
    Boolean includeEventBody
    Boolean includeEventAttendees
}
