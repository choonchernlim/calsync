package com.github.choonchernlim.calsync.core

class UserConfig {
    String exchangeUserName
    String exchangePassword
    String exchangeUrl
    String googleClientSecretJsonFilePath
    String googleCalendarName
    Integer totalSyncDays

    UserConfig() {
        this.exchangeUserName = System.getenv('CALSYNC_EXCHANGE_USERNAME')
        this.exchangePassword = System.getenv('CALSYNC_EXCHANGE_PASSWORD')
        this.exchangeUrl = System.getenv('CALSYNC_EXCHANGE_URL')
        this.googleClientSecretJsonFilePath = System.getenv('CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH')
        this.googleCalendarName = System.getenv('CALSYNC_GOOGLE_CALENDAR_NAME')
        this.totalSyncDays = System.getenv('CALSYNC_TOTAL_SYNC_DAYS')?.toInteger() ?: 0

        if (!this.googleClientSecretJsonFilePath?.trim() ||
            !this.googleCalendarName?.trim() ||
            !this.exchangeUserName?.trim() ||
            !this.exchangePassword?.trim() ||
            !this.exchangeUrl?.trim() ||
            this.totalSyncDays <= 0) {

            System.err.println """
The following environment variables must exist with valid values:
- CALSYNC_EXCHANGE_USERNAME
- CALSYNC_EXCHANGE_PASSWORD
- CALSYNC_EXCHANGE_URL
- CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH
- CALSYNC_GOOGLE_CALENDAR_NAME
- CALSYNC_TOTAL_SYNC_DAYS
"""
            System.exit(1)
        }
    }
}
