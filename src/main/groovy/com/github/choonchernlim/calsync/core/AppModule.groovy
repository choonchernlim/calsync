package com.github.choonchernlim.calsync.core

import com.github.choonchernlim.calsync.exchange.ExchangeClient
import com.github.choonchernlim.calsync.exchange.ExchangeService
import com.github.choonchernlim.calsync.google.GoogleClient
import com.github.choonchernlim.calsync.google.GoogleService
import com.google.inject.AbstractModule

class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserConfig)

        bind(ExchangeClient)
        bind(ExchangeService)

        bind(GoogleClient)
        bind(GoogleService)
        bind(ExchangeToGoogleService)
    }
}
