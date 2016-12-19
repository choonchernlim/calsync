package com.github.choonchernlim.calsync.ui.controller

import com.github.choonchernlim.calsync.ui.core.FxmlEnum
import com.github.choonchernlim.calsync.ui.core.FxmlLoaderService
import com.github.choonchernlim.calsync.ui.event.ShowHideEvent
import com.google.common.eventbus.Subscribe
import javafx.fxml.FXML
import javafx.scene.Parent
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
            Parent parent = fxmlLoaderService.load(FxmlEnum.RIGHT_PANE)
            main.setRight(parent)
        }
        else {
            main.setRight(null)
        }
    }

}
