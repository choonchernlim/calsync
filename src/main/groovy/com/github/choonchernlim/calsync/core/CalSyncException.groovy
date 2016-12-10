package com.github.choonchernlim.calsync.core

class CalSyncException extends Exception {

    CalSyncException(final String msg) {
        super(msg)
    }

    CalSyncException(final String msg, final Throwable ex) {
        super(msg, ex)
    }
}
