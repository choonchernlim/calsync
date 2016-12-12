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
    String googleClientSecretJsonFilePath
    String googleCalendarName
    Integer totalSyncDays
    Integer nextSyncInMinutes
}
