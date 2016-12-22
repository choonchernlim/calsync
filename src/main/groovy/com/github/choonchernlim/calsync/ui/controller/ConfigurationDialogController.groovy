package com.github.choonchernlim.calsync.ui.controller

import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.ChoiceBox
import javafx.scene.control.Control
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import rx.Observable
import rx.functions.Action1
import rx.schedulers.Schedulers

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

    @FXML
    HBox exchangePending

    @FXML
    HBox exchangeSuccess

    @FXML
    HBox exchangeFailed

    @Override
    void initialize(final URL location, final ResourceBundle resources) {
        def envs = System.getenv().keySet().sort()

        exchangeUserEnv.items.addAll(envs)
        exchangePasswordEnv.items.addAll(envs)

        exchangeUserEnv.selectionModel.selectedItemProperty().addListener(
                {
                    observable, oldValue, newValue -> validateExchangeInfo()
                } as ChangeListener<String>
        )

        exchangePasswordEnv.selectionModel.selectedItemProperty().addListener(
                {
                    observable, oldValue, newValue -> validateExchangeInfo()

                } as ChangeListener<String>
        )

        exchangeServer.focusedProperty().addListener(
                {
                    observable, offFocus, onFocus ->
                        if (offFocus) {
                            validateExchangeInfo()
                        }
                } as ChangeListener<Boolean>
        )

        validate(false, exchangeUserEnv, exchangePasswordEnv, exchangeServer)
    }

    void validateExchangeInfo() {
        boolean isAllValid = validate(exchangeUserEnv.value, exchangeUserEnv)
        isAllValid &= validate(exchangePasswordEnv.value, exchangePasswordEnv)
        isAllValid &= validate(exchangeServer.text, exchangeServer)

        if (!isAllValid) {
            return
        }

        exchangeFailed.visible = false
        exchangeSuccess.visible = false
        exchangePending.visible = true

        isValid().
                subscribeOn(Schedulers.newThread()).
                subscribe(new Action1<Boolean>() {
                    @Override
                    void call(final Boolean isValid) {
                        // TODO mock for now to test flow
                        if (isValid) {
                            println 'all okay!'
                            exchangePending.visible = false
                            exchangeFailed.visible = false
                            exchangeSuccess.visible = true
                        }
                        else {
                            println 'not okay!'
                            exchangePending.visible = false
                            exchangeSuccess.visible = false
                            exchangeFailed.visible = true
                        }
                    }
                })
    }


    Observable<Boolean> isValid() {
        return Observable.fromCallable {
            sleep(2000)
            return true
        }
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

    private static boolean validate(truthy, Control... formFields) {
        if (truthy) {
            removeErrorStyleClass(formFields)
        }
        else {
            addErrorStyleClass(formFields)
        }

        return truthy
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
