package com.github.choonchernlim.calsync.core

import org.joda.time.DateTime
import spock.lang.Specification

class DateTimeNowSupplierSpec extends Specification {

    def 'get - when invoked, should be after current datetime'() {
        given:
        def givenDateTime = DateTime.now().minusMillis(1)

        when:
        def datetime = new DateTimeNowSupplier().get()

        then:
        datetime.isAfter(givenDateTime)
    }
}
