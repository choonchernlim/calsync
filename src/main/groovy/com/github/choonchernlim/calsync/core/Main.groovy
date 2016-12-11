package com.github.choonchernlim.calsync.core

import com.google.inject.Guice
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Runner class.
 */
class Main {
    private static Logger LOGGER = LoggerFactory.getLogger(Main)

    static void main(String[] args) {
        try {
            Guice.createInjector(new AppModule()).
                    getInstance(ExchangeToGoogleService).
                    run()
        }
        catch (e) {
            LOGGER.error('Unexpected error occurred', e)
        }
    }
}
