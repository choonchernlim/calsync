package com.github.choonchernlim.testJavaFx.controller

import com.github.choonchernlim.testJavaFx.constant.FxmlEnum
import com.github.choonchernlim.testJavaFx.event.ShowHideEvent
import com.github.choonchernlim.testJavaFx.service.FxmlLoaderService
import com.google.common.eventbus.Subscribe
import javafx.fxml.FXML
import javafx.scene.layout.BorderPane
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.inject.Inject

final class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class)
    private final FxmlLoaderService fxmlLoaderService

    @FXML
    private BorderPane main

    @Inject
    MainController(final FxmlLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService
    }

    @Subscribe
    void toggleRightContainerVisibility(final ShowHideEvent event) {
        LOGGER.debug("hiding right side from main controller....")

        if (event.isShow()) {
            main.setRight(fxmlLoaderService.load(FxmlEnum.RIGHT_PANE))
        }
        else {
            main.setRight(null)
        }
    }

}
