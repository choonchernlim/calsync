package com.github.choonchernlim.calsync.exchange

import com.github.choonchernlim.calsync.core.UserConfig
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
        service.getEvents(startDateTime, endDateTime, true, true)

        then:
        0 * _
        thrown AssertionError

        where:
        label                         | startDateTime  | endDateTime
        'startDateTime == null'       | null           | DateTime.now().minusDays(1)
        'endDateTime == null'         | DateTime.now() | null
        'startDateTime > endDateTime' | DateTime.now() | DateTime.now().minusDays(1)
    }

    def 'getEvents - given includeCanceledEvents == null, should throw exception'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        service.getEvents(startDateTime, endDateTime, null, true)

        then:
        0 * _
        thrown AssertionError
    }

    def 'getEvents - given includeEventBody == null, should throw exception'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        service.getEvents(startDateTime, endDateTime, true, null)

        then:
        0 * _
        thrown AssertionError
    }


    def 'getEvents - given no events, should return empty list'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime, true, true)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> null
        0 * _

        events.isEmpty()
    }

    def 'getEvents - given includeCanceledEvent == true and includeEventBody == true, should return all events with body'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime, true, true)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> [
                createExchangeEvent(startDateTime, endDateTime, 'summary1', 'location1', 15, true, 'body1'),
                createExchangeEvent(startDateTime, endDateTime, 'summary2', 'location2', 15, false, 'body2')
        ]
        0 * _

        events.size() == 2
        events*.subject == ['summary1', 'summary2']
        events*.body == ['body1', 'body2']
    }

    def 'getEvents - given includeCanceledEvent == false and includeEventBody == true, should return all non-canceled events with body'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime, false, true)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> [
                createExchangeEvent(startDateTime, endDateTime, 'summary1', 'location1', 15, true, 'body1'),
                createExchangeEvent(startDateTime, endDateTime, 'summary2', 'location2', 15, false, 'body2')
        ]
        0 * _

        events.size() == 1
        events*.subject == ['summary2']
        events*.body == ['body2']
    }

    def 'getEvents - given includeCanceledEvent == true and includeEventBody == false, should return all events with no body'() {
        given:
        def startDateTime = DateTime.now()
        def endDateTime = startDateTime.plusDays(1)

        when:
        def events = service.getEvents(startDateTime, endDateTime, true, false)

        then:
        1 * exchangeClient.getEvents(startDateTime, endDateTime) >> [
                createExchangeEvent(startDateTime, endDateTime, 'summary1', 'location1', 15, true, 'body1'),
                createExchangeEvent(startDateTime, endDateTime, 'summary2', 'location2', 15, false, 'body2')
        ]
        0 * _

        events.size() == 2
        events*.subject == ['summary1', 'summary2']
        events*.body == [null, null]
    }

    private static ExchangeEvent createExchangeEvent(
            DateTime startDateTime,
            DateTime endDateTime,
            String subject,
            String location,
            Integer reminderMinutesBeforeStart,
            Boolean isCancelled,
            String body) {


        return new ExchangeEvent(
                startDateTime: startDateTime,
                endDateTime: endDateTime,
                subject: subject,
                location: location,
                reminderMinutesBeforeStart: reminderMinutesBeforeStart,
                isCanceled: isCancelled,
                body: body

        )
    }


}
