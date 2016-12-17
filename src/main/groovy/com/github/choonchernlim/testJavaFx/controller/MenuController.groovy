package com.github.choonchernlim.testJavaFx.controller

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.MenuBar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class MenuController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuController.class)

    @FXML
    private MenuBar menuBar

    @FXML
    private void handleAboutAction(ActionEvent actionEvent) {
        LOGGER.debug("about action!")
    }
}
