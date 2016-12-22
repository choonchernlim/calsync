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

    enum MessageTypeEnum {
        NONE, PENDING, SUCCESS, ERROR
    }

    @FXML
    ChoiceBox<String> exchangeUserEnv

    @FXML
    ChoiceBox<String> exchangePasswordEnv

    @FXML
    TextField exchangeServer

    @FXML
    TextField clientSecretFile

    @FXML
    HBox exchangeMessage

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

        // hide all messages
        showMessage(exchangeMessage, MessageTypeEnum.NONE)
    }

    private void showMessage(HBox hBox, MessageTypeEnum messageTypeEnum) {
        assert hBox
        assert messageTypeEnum

        // - hide all message components first
        // - find matching message type, use `findAll` instead of `find` for chain-ability
        // - show matched message
        //
        // `managed` has to be set to `false` to ensure the non-visible component doesn't take up space.
        // See http://stackoverflow.com/questions/28558165/javafx-setvisible-doesnt-hide-the-element
        hBox.getChildren().
                each {
                    it.visible = false
                    it.managed = false
                }.
                findAll { it.userData == messageTypeEnum.toString() }.
                each {
                    it.visible = true
                    it.managed = true
                }
    }

    void validateExchangeInfo() {
        boolean isAllValid = validate(exchangeUserEnv.value, exchangeUserEnv)
        isAllValid &= validate(exchangePasswordEnv.value, exchangePasswordEnv)
        isAllValid &= validate(exchangeServer.text, exchangeServer)

        if (!isAllValid) {
            return
        }

        showMessage(exchangeMessage, MessageTypeEnum.PENDING)

        isValid().
                subscribeOn(Schedulers.newThread()).
                subscribe(new Action1<Boolean>() {
                    @Override
                    void call(final Boolean isValid) {
                        if (isValid) {
                            println 'all okay!'
                            showMessage(exchangeMessage, MessageTypeEnum.SUCCESS)
                        }
                        else {
                            println 'not okay!'
                            showMessage(exchangeMessage, MessageTypeEnum.ERROR)
                        }
                    }
                })
    }

    // TODO replace with real API
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
