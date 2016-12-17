package com.github.choonchernlim.testJavaFx.event

final class ShowHideEvent {
    private final boolean show

    ShowHideEvent(final boolean show) {
        this.show = show
    }

    boolean isShow() {
        return show
    }
}
