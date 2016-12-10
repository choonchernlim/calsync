package com.github.choonchernlim.calsync.core

import groovy.transform.ToString

@ToString
class UserConfig {
    String exchangeUserName
    String exchangePassword
    String exchangeUrl
    String googleClientSecretJsonFilePath
    String googleCalendarName
    Integer totalSyncDays

    UserConfig() {
        exchangeUserName = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_USERNAME)?.trim()
        exchangePassword = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_PASSWORD)?.trim()
        exchangeUrl = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_URL)?.trim()
        googleClientSecretJsonFilePath = System.getenv(Constant.ENV_CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH)?.trim()
        googleCalendarName = System.getenv(Constant.ENV_CALSYNC_GOOGLE_CALENDAR_NAME)?.trim()

        String totalSyncDaysString = System.getenv(Constant.ENV_CALSYNC_TOTAL_SYNC_DAYS)?.trim()

        try {
            assert googleClientSecretJsonFilePath
            assert googleCalendarName
            assert exchangeUserName
            assert exchangePassword
            assert exchangeUrl
            assert totalSyncDaysString
            assert totalSyncDaysString.isInteger()
            assert totalSyncDaysString.toInteger() > 0
        }
        // Must define type because Groovy assert throws Throwable instead of Exception
        catch (Throwable e) {
            throw new CalSyncException("""
The following environment variables must exist with valid values:
- CALSYNC_EXCHANGE_USERNAME
- CALSYNC_EXCHANGE_PASSWORD
- CALSYNC_EXCHANGE_URL
- CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH
- CALSYNC_GOOGLE_CALENDAR_NAME
- CALSYNC_TOTAL_SYNC_DAYS
""", e)
        }

        totalSyncDays = totalSyncDaysString.toInteger()
    }
}
