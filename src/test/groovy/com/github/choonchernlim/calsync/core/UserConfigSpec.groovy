package com.github.choonchernlim.calsync.core

import spock.lang.Specification
import spock.lang.Unroll

class UserConfigSpec extends Specification {

    static String CALSYNC_EXCHANGE_USERNAME = 'username'
    static String CALSYNC_EXCHANGE_PASSWORD = 'password'
    static String CALSYNC_EXCHANGE_URL = 'url'
    static String CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH = 'filepath'
    static String CALSYNC_GOOGLE_CALENDAR_NAME = 'calendar'
    static String CALSYNC_TOTAL_SYNC_DAYS = '1'

    def setEnvs() {
        System.metaClass.'static'.getenv = { String var ->
            switch (var) {
                case Constant.ENV_CALSYNC_EXCHANGE_USERNAME: return CALSYNC_EXCHANGE_USERNAME
                case Constant.ENV_CALSYNC_EXCHANGE_PASSWORD: return CALSYNC_EXCHANGE_PASSWORD
                case Constant.ENV_CALSYNC_EXCHANGE_URL: return CALSYNC_EXCHANGE_URL
                case Constant.ENV_CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH: return CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH
                case Constant.ENV_CALSYNC_GOOGLE_CALENDAR_NAME: return CALSYNC_GOOGLE_CALENDAR_NAME
                case Constant.ENV_CALSYNC_TOTAL_SYNC_DAYS: return CALSYNC_TOTAL_SYNC_DAYS
            }
        }
    }

    def 'userConfig - given valid envs, should return object'() {
        given:
        setEnvs()

        when:
        def userConfig = new UserConfig()

        then:
        userConfig != null
        userConfig.exchangeUserName == CALSYNC_EXCHANGE_USERNAME
        userConfig.exchangePassword == CALSYNC_EXCHANGE_PASSWORD
        userConfig.exchangeUrl == CALSYNC_EXCHANGE_URL
        userConfig.googleClientSecretJsonFilePath == CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH
        userConfig.googleCalendarName == CALSYNC_GOOGLE_CALENDAR_NAME
        userConfig.totalSyncDays == CALSYNC_TOTAL_SYNC_DAYS.toInteger()
    }

    @Unroll
    def 'userConfig - #var - given #label, should thrown exception'() {
        given:
        this."${var}" = value
        setEnvs()

        when:
        new UserConfig()

        then:
        thrown CalSyncException

        where:
        label         | var                                           | value
        'null'        | 'CALSYNC_EXCHANGE_USERNAME'                   | null
        'blank'       | 'CALSYNC_EXCHANGE_USERNAME'                   | ' '
        'null'        | 'CALSYNC_EXCHANGE_PASSWORD'                   | null
        'blank'       | 'CALSYNC_EXCHANGE_PASSWORD'                   | ' '
        'null'        | 'CALSYNC_EXCHANGE_URL'                        | null
        'blank'       | 'CALSYNC_EXCHANGE_URL'                        | ' '
        'null'        | 'CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH' | null
        'blank'       | 'CALSYNC_GOOGLE_CLIENT_SECRET_JSON_FILE_PATH' | ' '
        'null'        | 'CALSYNC_GOOGLE_CALENDAR_NAME'                | null
        'blank'       | 'CALSYNC_GOOGLE_CALENDAR_NAME'                | ' '
        'null'        | 'CALSYNC_TOTAL_SYNC_DAYS'                     | null
        'blank'       | 'CALSYNC_TOTAL_SYNC_DAYS'                     | ' '
        'not integer' | 'CALSYNC_TOTAL_SYNC_DAYS'                     | 'val'
        'zero'        | 'CALSYNC_TOTAL_SYNC_DAYS'                     | '0'
    }
}
