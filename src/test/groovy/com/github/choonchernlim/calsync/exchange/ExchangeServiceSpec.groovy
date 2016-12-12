package com.github.choonchernlim.calsync.exchange

import com.github.choonchernlim.calsync.core.UserConfig
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion
import microsoft.exchange.webservices.data.core.service.item.Appointment
import org.joda.time.DateTime
import spock.lang.Specification
import spock.lang.Unroll

class ExchangeServiceSpec extends Specification {

    def exchangeClient = Mock ExchangeClient
    def service = new ExchangeService(exchangeClient)

    def 'init - given null config, should throw exception'() {
        when:
        service.init(null)

        then:
        0 * _

        thrown AssertionError
    }

    def 'init - given config, should pass config to client'() {
        given:
        def userConfig = new UserConfig()

        when:
        service.init(userConfig)

        then:
        1 * exchangeClient.init(userConfig)
        0 * _
    }

    @Unroll
    def 'getEvents - given #label, should throw exception'() {
        when:
        service.getEvents(startDateTime, endDateTime)

        then:
        0 * _
        thrown AssertionError

        where:
        label                         | startDateTime  | endDateTime
        'startDateTime == null'       | null           | DateTime.now().minusDays(1)
        'endDateTime == null'         | DateTime.now() | null
        'startDateTime > endDateTime' | DateTime.now() | DateTime.now().minusDays(1)
    }

    def 'getEvents - given no events, should return empty list'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> null
        0 * _

        events.isEmpty()
    }

    def 'getEvents - given events found, should return events'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> {
            def appointment = new Appointment(Mock(microsoft.exchange.webservices.data.core.ExchangeService) {
                getRequestedServerVersion() >> ExchangeVersion.Exchange2010_SP2
            })
            appointment.start = startDateTime.toDate()
            appointment.end = endDateTime.toDate()
            appointment.subject = 'summary'
            appointment.location = 'location'
            appointment.reminderMinutesBeforeStart = 15

            return [appointment]
        }
        0 * _

        events.size() == 1
        events*.subject == ['summary']
    }


}
