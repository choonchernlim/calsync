package com.github.choonchernlim.calsync.ui.controller

import com.github.choonchernlim.calsync.ui.core.FxmlEnum
import com.github.choonchernlim.calsync.ui.core.FxmlLoaderService
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Dialog
import javafx.scene.control.PasswordField
import javafx.scene.layout.BorderPane
import javafx.scene.text.Text

import javax.inject.Inject

final class CenterPaneController {
    @FXML
    private PasswordField passwordField

    @FXML
    private Text actiontarget

    @FXML
    private BorderPane main

    private final FxmlLoaderService fxmlLoaderService

    @Inject
    CenterPaneController(final FxmlLoaderService fxmlLoaderService) {
        this.fxmlLoaderService = fxmlLoaderService
    }

    @FXML
    protected void handleSubmitButtonAction(ActionEvent actionEvent) {
        actiontarget.setText("Sign in button pressed")
    }

    void handleOpenDialog(ActionEvent actionEvent) {
        Dialog dialog = fxmlLoaderService.load(FxmlEnum.CONFIGURATION_DIALOG)
        dialog.showAndWait()
    }
}
