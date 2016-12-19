package com.github.choonchernlim.calsync.ui.controller

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.FileChooser

final class ConfigurationDialogController {
    @FXML
    TextField clientSecretFile

    @SuppressWarnings("GrMethodMayBeStatic")
    void handleClientSecretFileChooser(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser(initialDirectory: new File(System.getProperty('user.home')))
        fileChooser.extensionFilters.add(new FileChooser.ExtensionFilter('JSON', '*.json'))

        final File selectedFile = fileChooser.showOpenDialog(null)

        if (selectedFile != null) {
            println 'selected ' + selectedFile
            clientSecretFile.text = selectedFile.toString()
        }
        else {
            println 'canceled'
        }
    }
}
