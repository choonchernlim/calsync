package com.github.choonchernlim.calsync.ui.core

import com.google.common.eventbus.EventBus
import com.google.inject.AbstractModule

final class GuiceModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(EventBus.class).asEagerSingleton()
    }
}
