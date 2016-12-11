package com.github.choonchernlim.calsync.core

import com.google.common.base.Supplier
import org.joda.time.DateTime

/**
 * Supplier that returns current datetime.
 */
class DateTimeNowSupplier implements Supplier<DateTime> {
    @Override
    DateTime get() {
        return DateTime.now()
    }
}
