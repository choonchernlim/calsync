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
    Integer nextSyncInMinutes

    UserConfig() {
        exchangeUserName = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_USERNAME)?.trim()
        exchangePassword = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_PASSWORD)?.trim()
        exchangeUrl = System.getenv(Constant.ENV_CALSYNC_EXCHANGE_URL)?.trim()
        googleClientSecretJsonFilePath = System.getenv(Constant.ENV_CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH)?.trim()
        googleCalendarName = System.getenv(Constant.ENV_CALSYNC_GOOGLE_CALENDAR_NAME)?.trim()

        String totalSyncDaysString = System.getenv(Constant.ENV_CALSYNC_TOTAL_SYNC_DAYS)?.trim()
        String nextSyncInMinutesString = System.getenv(Constant.ENV_CALSYNC_NEXT_SYNC_IN_MINUTES)?.trim()

        if (!exchangeUserName ||
            !exchangePassword ||
            !exchangeUrl ||
            !googleClientSecretJsonFilePath ||
            !googleCalendarName ||
            !totalSyncDaysString ||
            !totalSyncDaysString.isInteger() ||
            totalSyncDaysString.toInteger() <= 0 ||

            !nextSyncInMinutesString ||
            !nextSyncInMinutesString.isInteger()
        ) {
            throw new CalSyncException("""
The following environment variables must exist with valid values:
- CALSYNC_EXCHANGE_USERNAME
- CALSYNC_EXCHANGE_PASSWORD
- CALSYNC_EXCHANGE_URL
- CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH
- CALSYNC_GOOGLE_CALENDAR_NAME
- CALSYNC_TOTAL_SYNC_DAYS
- CALSYNC_NEXT_SYNC_IN_MINUTES
""")
        }

        totalSyncDays = totalSyncDaysString.toInteger()
        nextSyncInMinutes = nextSyncInMinutesString.toInteger()
    }
}
