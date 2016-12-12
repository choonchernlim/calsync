package com.github.choonchernlim.calsync.core

import com.google.inject.Guice
import com.google.inject.Injector
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main runner class. Based on the user config, configure the task to either run just once or at fixed rate.
 */
class Main {
    private static Logger LOGGER = LoggerFactory.getLogger(Main)

    static void main(String[] args) {
        Injector injector = Guice.createInjector()

        UserConfigReader userConfigReader = injector.getInstance(UserConfigReader)

        UserConfig userConfig = userConfigReader.getUserConfig()

        ExchangeToGoogleService service = injector.getInstance(ExchangeToGoogleService)

        if (userConfig.nextSyncInMinutes > 0) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                void run() {
                    service.run(userConfig)

                    LOGGER.info("Next run in ${userConfig.nextSyncInMinutes} minutes...")
                }
            }, 0, userConfig.nextSyncInMinutes * 60000)
        }
        else {
            service.run(userConfig)
        }
    }
}
