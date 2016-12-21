package com.github.choonchernlim.calsync.ui.controller

import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Control
import javafx.scene.control.TextField
import javafx.stage.FileChooser

final class ConfigurationDialogController implements Initializable {
    private static final String FORM_FIELD_ERROR_STYLE = 'form-field-error'

    @FXML
    ChoiceBox<String> exchangeUserEnv

    @FXML
    ChoiceBox<String> exchangePasswordEnv

    @FXML
    TextField exchangeServer

    @FXML
    TextField clientSecretFile

    @Override
    void initialize(final URL location, final ResourceBundle resources) {
        def envs = System.getenv().keySet().sort()

        exchangeUserEnv.items.addAll(envs)
        exchangePasswordEnv.items.addAll(envs)

        exchangeUserEnv.selectionModel.selectedItemProperty().addListener(
                {
                    observable, oldValue, newValue ->
                        handleFieldErrorStyle(newValue, exchangeUserEnv)
                } as ChangeListener<String>
        )

        exchangePasswordEnv.selectionModel.selectedItemProperty().addListener(
                {
                    observable, oldValue, newValue ->
                        handleFieldErrorStyle(newValue, exchangePasswordEnv)
                } as ChangeListener<String>
        )

        exchangeServer.focusedProperty().addListener(
                {
                    observable, offFocus, onFocus ->
                        if (offFocus) {
                            handleFieldErrorStyle(exchangeServer.text, exchangeServer)
                        }
                } as ChangeListener<Boolean>
        )

        handleFieldErrorStyle(false, exchangeUserEnv, exchangePasswordEnv, exchangeServer)
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void handleClientSecretFileChooser() {
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

    private static void handleFieldErrorStyle(truthy, Control... formFields) {
        if (truthy) {
            removeErrorStyleClass(formFields)
        }
        else {
            addErrorStyleClass(formFields)
        }
    }

    private static void addErrorStyleClass(Control... formFields) {
        assert formFields

        formFields.
                findAll { !it.styleClass.contains(FORM_FIELD_ERROR_STYLE) }.
                each { it.styleClass.add(FORM_FIELD_ERROR_STYLE) }
    }

    private static void removeErrorStyleClass(Control... formFields) {
        assert formFields

        formFields.each { it.styleClass.remove(FORM_FIELD_ERROR_STYLE) }
    }


}
