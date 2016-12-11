package com.github.choonchernlim.calsync.core

import com.google.inject.AbstractModule

class AppModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(UserConfig)
        bind(GoogleClient)
        bind(GoogleService)
        bind(ExchangeToGoogleService)
    }
}
