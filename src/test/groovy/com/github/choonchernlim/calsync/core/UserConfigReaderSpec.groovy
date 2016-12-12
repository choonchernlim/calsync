package com.github.choonchernlim.calsync.core

import spock.lang.Specification
import spock.lang.Unroll

class UserConfigReaderSpec extends Specification {

    UserConfigReader reader = new UserConfigReader()
    Properties properties = new Properties()

    // construct valid properties
    def setup() {
        properties.setProperty(UserConfigReader.EXCHANGE_USERNAME_ENV_KEY, 'EXCHANGE_USERNAME_ENV')
        properties.setProperty(UserConfigReader.EXCHANGE_PASSWORD_ENV_KEY, 'EXCHANGE_PASSWORD_ENV')
        properties.setProperty(UserConfigReader.EXCHANGE_URL_KEY, 'URL')
        properties.setProperty(UserConfigReader.GOOGLE_CLIENT_SECRET_JSON_KEY, 'CLIENT_JSON')
        properties.setProperty(UserConfigReader.GOOGLE_CALENDAR_NAME_KEY, 'CALENDAR_NAME')
        properties.setProperty(UserConfigReader.TOTAL_SYNC_IN_DAYS_KEY, '1')
        properties.setProperty(UserConfigReader.NEXT_SYNC_IN_MINUTES_KEY, '15')

        System.metaClass.'static'.getenv = { String var ->
            switch (var) {
                case 'EXCHANGE_USERNAME_ENV': return 'EXCHANGE_USERNAME'
                case 'EXCHANGE_PASSWORD_ENV': return 'EXCHANGE_PASSWORD'
            }
        }

        File file = new File(Constant.CONFIG_FILE_PATH)
        if (file.exists()) {
            file.delete()
        }

        assert !file.exists()
    }

    def 'getUserConfig - given missing conf file, should create one and throw exception'() {
        given:
        File file = new File(Constant.CONFIG_FILE_PATH)
        assert !file.exists()

        when:
        reader.getUserConfig()

        then:
        assert file.exists()
        thrown CalSyncException

        cleanup:
        assert file.delete()
    }

    def 'getUserConfig - given valid conf file, should return user config'() {
        given:
        File file = new File(Constant.CONFIG_FILE_PATH)
        assert !file.exists()
        file.write('''
exchange.username.env=EXCHANGE_USERNAME_ENV
exchange.password.env=EXCHANGE_PASSWORD_ENV
exchange.url=URL
google.client.secret.json.file.path=CLIENT_JSON
google.calendar.name=CALENDAR_NAME
total.sync.in.days=1
next.sync.in.minutes=15
''')

        when:
        def userConfig = reader.getUserConfig()

        then:
        userConfig.exchangeUserName == 'EXCHANGE_USERNAME'
        userConfig.exchangePassword == 'EXCHANGE_PASSWORD'
        userConfig.exchangeUrl == 'URL'
        userConfig.googleClientSecretJsonFilePath == 'CLIENT_JSON'
        userConfig.googleCalendarName == 'CALENDAR_NAME'
        userConfig.totalSyncDays == 1
        userConfig.nextSyncInMinutes == 15

        cleanup:
        assert file.delete()
    }

    def 'validate - given missing conf file, should create one and throw exception'() {
        given:
        File file = new File(Constant.CONFIG_FILE_PATH)
        assert !file.exists()

        when:
        reader.getUserConfig()

        then:
        assert file.exists()
        thrown CalSyncException

        cleanup:
        assert file.delete()
    }

    @Unroll
    def 'validate - #key - given #label, should thrown exception'() {
        given:
        properties.setProperty(key, value)

        when:
        reader.validate(properties)

        then:
        thrown CalSyncException

        where:
        label         | key                                            | value
        'blank'       | UserConfigReader.EXCHANGE_USERNAME_ENV_KEY     | ' '
        'blank'       | UserConfigReader.EXCHANGE_PASSWORD_ENV_KEY     | ' '
        'blank'       | UserConfigReader.EXCHANGE_URL_KEY              | ' '
        'blank'       | UserConfigReader.GOOGLE_CLIENT_SECRET_JSON_KEY | ' '
        'blank'       | UserConfigReader.GOOGLE_CALENDAR_NAME_KEY      | ' '
        'blank'       | UserConfigReader.TOTAL_SYNC_IN_DAYS_KEY        | ' '
        'not integer' | UserConfigReader.TOTAL_SYNC_IN_DAYS_KEY        | 'val'
        'zero'        | UserConfigReader.TOTAL_SYNC_IN_DAYS_KEY        | '0'
        'blank'       | UserConfigReader.NEXT_SYNC_IN_MINUTES_KEY      | ' '
        'not integer' | UserConfigReader.NEXT_SYNC_IN_MINUTES_KEY      | 'val'
    }

    @Unroll
    def 'validate - #key - given non-existence env, should thrown exception'() {
        given:
        properties.setProperty(key, value)

        when:
        reader.validate(properties)

        then:
        thrown CalSyncException

        where:
        key                                        | value
        UserConfigReader.EXCHANGE_USERNAME_ENV_KEY | 'INVALID_EXCHANGE_USERNAME_ENV_KEY'
        UserConfigReader.EXCHANGE_PASSWORD_ENV_KEY | 'INVALID_EXCHANGE_PASSWORD_ENV_KEY'
    }
}