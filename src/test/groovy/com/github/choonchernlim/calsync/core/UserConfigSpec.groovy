package com.github.choonchernlim.calsync.core

import spock.lang.Specification

class UserConfigSpec extends Specification {

    def 'userConfig - given no params, should return no values'() {
        when:
        def userConfig = new UserConfig(
                exchangeUserName: null,
                exchangePassword: null,
                exchangeUrl: null,
                googleClientSecretJsonFilePath: null,
                googleCalendarName: null,
                totalSyncDays: null,
                nextSyncInMinutes: null,
                includeCanceledEvents: null
        )

        then:
        userConfig.exchangeUserName == null
        userConfig.exchangePassword == null
        userConfig.exchangeUrl == null
        userConfig.googleClientSecretJsonFilePath == null
        userConfig.googleCalendarName == null
        userConfig.totalSyncDays == null
        userConfig.nextSyncInMinutes == null
        userConfig.includeCanceledEvents == null
    }

    def 'userConfig - given valid params, should return valid values'() {
        when:
        def userConfig = new UserConfig(
                exchangeUserName: 'exchangeUserName',
                exchangePassword: 'exchangePassword',
                exchangeUrl: 'exchangeUrl',
                googleClientSecretJsonFilePath: 'googleClientSecretJsonFilePath',
                googleCalendarName: 'googleCalendarName',
                totalSyncDays: 1,
                nextSyncInMinutes: 2,
                includeCanceledEvents: true
        )

        then:
        userConfig.exchangeUserName == 'exchangeUserName'
        userConfig.exchangePassword == 'exchangePassword'
        userConfig.exchangeUrl == 'exchangeUrl'
        userConfig.googleClientSecretJsonFilePath == 'googleClientSecretJsonFilePath'
        userConfig.googleCalendarName == 'googleCalendarName'
        userConfig.totalSyncDays == 1
        userConfig.nextSyncInMinutes == 2
        userConfig.includeCanceledEvents
    }
}
