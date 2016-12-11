package com.github.choonchernlim.calsync.google

import com.github.choonchernlim.calsync.core.CalSyncEvent
import groovy.transform.PackageScope


@PackageScope
class EventAction {
    enum Action {
        INSERT, DELETE
    }

    Action action
    CalSyncEvent event
}
