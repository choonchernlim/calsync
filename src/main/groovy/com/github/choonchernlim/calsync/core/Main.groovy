package com.github.choonchernlim.calsync.core

import com.google.inject.Guice
import com.google.inject.Injector

/**
 * Main runner class. Based on the user config, configure the task to either run just once or at fixed rate.
 */
class Main {
    static void main(String[] args) {
        Injector injector = Guice.createInjector()

        UserConfig userConfig = injector.getInstance(UserConfig)
        ExchangeToGoogleService service = injector.getInstance(ExchangeToGoogleService)

        if (userConfig.nextSyncInMinutes > 0) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                void run() {
                    service.run()
                }
            }, 0, userConfig.nextSyncInMinutes * 60000)
        }
        else {
            service.run()
        }
    }
}
