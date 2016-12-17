package com.github.choonchernlim.testJavaFx.controller

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.PasswordField
import javafx.scene.text.Text

final class CenterPaneController {
    @FXML
    private PasswordField passwordField

    @FXML
    private Text actiontarget

    @FXML
    protected void handleSubmitButtonAction(ActionEvent event) {
        actiontarget.setText("Sign in button pressed")
    }
}
