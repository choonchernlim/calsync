package com.github.choonchernlim.calsync.ui.controller

import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import rx.Observable
import rx.schedulers.Schedulers

final class ConfigurationDialogController implements Initializable {
    private static final String FORM_FIELD_ERROR_STYLE = 'form-field-error'

    enum MessageTypeEnum {
        NONE, PENDING, SUCCESS, ERROR
    }

    enum ValueEnum {
        YES, NO
    }

    @FXML
    ChoiceBox<String> exchangeUserEnv

    @FXML
    ChoiceBox<String> exchangePasswordEnv

    @FXML
    TextField exchangeServer

    @FXML
    TextField calendarName

    @FXML
    TextField clientSecretFile

    @FXML
    HBox exchangeMessage

    @FXML
    HBox googleMessage

    @FXML
    ToggleGroup includeEventBodyToggleGroup

    @FXML
    ToggleGroup includeCanceledEventsToggleGroup

    boolean isExchangeValid = false
    boolean isGoogleValid = false

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

        clientSecretFile.textProperty().addListener(
                {
                    observable, oldValue, newValue ->
                        if (newValue) {
                            validateGoogleInfo()
                        }

                } as ChangeListener<String>
        )

        calendarName.focusedProperty().addListener(
                {
                    observable, offFocus, onFocus ->
                        if (offFocus) {
                            validateGoogleInfo()
                        }
                } as ChangeListener<Boolean>
        )

        setupToggleGroupAsRadioButtons(setValue(includeCanceledEventsToggleGroup, ValueEnum.NO))
        setupToggleGroupAsRadioButtons(setValue(includeEventBodyToggleGroup, ValueEnum.NO))

        validateFormFields(false, exchangeUserEnv, exchangePasswordEnv, exchangeServer)
        validateFormFields(false, calendarName, clientSecretFile)

        // hide all messages
        showMessage(exchangeMessage, MessageTypeEnum.NONE)
        showMessage(googleMessage, MessageTypeEnum.NONE)
    }

    void validateExchangeInfo() {
        boolean isAllValid = validateFormFields(exchangeUserEnv.value, exchangeUserEnv)
        isAllValid &= validateFormFields(exchangePasswordEnv.value, exchangePasswordEnv)
        isAllValid &= validateFormFields(exchangeServer.text, exchangeServer)

        if (!isAllValid) {
            return
        }

        showMessage(exchangeMessage, MessageTypeEnum.PENDING)

        isExchangeValid().subscribe { isValid ->
            isExchangeValid = isValid
            showMessage(exchangeMessage, isValid ? MessageTypeEnum.SUCCESS : MessageTypeEnum.ERROR)
        }
    }

    void validateGoogleInfo() {
        boolean isAllValid = validateFormFields(calendarName.text, calendarName)
        isAllValid &= validateFormFields(clientSecretFile.text, clientSecretFile)

        if (!isAllValid) {
            return
        }

        showMessage(googleMessage, MessageTypeEnum.PENDING)

        isGoogleValid().subscribe { isValid ->
            isGoogleValid = isValid
            showMessage(googleMessage, isValid ? MessageTypeEnum.SUCCESS : MessageTypeEnum.ERROR)
        }
    }

    // TODO replace with real API
    static Observable<Boolean> isExchangeValid() {
        return Observable.fromCallable {
            sleep(2000)
            return true
        }.subscribeOn(Schedulers.newThread())
    }

    // TODO replace with real API
    static Observable<Boolean> isGoogleValid() {
        return Observable.fromCallable {
            sleep(2000)
            return false
        }.subscribeOn(Schedulers.newThread())
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    void handleClientSecretFileChooser() {
        final FileChooser fileChooser = new FileChooser(initialDirectory: new File(System.getProperty('user.home')))
        fileChooser.extensionFilters.add(new FileChooser.ExtensionFilter('JSON', '*.json'))

        final File selectedFile = fileChooser.showOpenDialog(null)

        if (selectedFile != null) {
            clientSecretFile.text = selectedFile.toString()
        }
    }

    /**
     * Sets a value for Toogle Group.
     *
     * @param toggleGroup Toggle Group
     * @param valueEnum Value to be selected
     * @return Toggle Group
     */
    private static ToggleGroup setValue(ToggleGroup toggleGroup, ValueEnum valueEnum) {
        assert toggleGroup
        assert valueEnum

        toggleGroup.toggles.find { it.userData == valueEnum.toString() }.selected = true

        return toggleGroup
    }

    /**
     * Prevents user from deselecting the same selected toggle button.
     *
     * @param toggleGroup Toggle group
     * @return Toggle group
     */
    private static ToggleGroup setupToggleGroupAsRadioButtons(ToggleGroup toggleGroup) {
        assert toggleGroup

        toggleGroup.selectedToggleProperty().addListener(
                {
                    observable, oldToggleButton, newToggleButton ->
                        if (!newToggleButton && oldToggleButton) {
                            oldToggleButton.selected = true
                        }
                } as ChangeListener<Toggle>
        )

        return toggleGroup
    }

    /**
     * Displays the intended message from the container.
     *
     * @param hBox Container
     * @param messageTypeEnum Message to be displayed
     */
    private static void showMessage(HBox hBox, MessageTypeEnum messageTypeEnum) {
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

    /**
     * Sets error styling on form fields if not truthy. Otherwise, remove error styling.
     *
     * @param truthy Truthy condition
     * @param formFields Form fields
     * @return Truthy
     */
    private static boolean validateFormFields(truthy, Control... formFields) {
        if (truthy) {
            removeErrorStyleClass(formFields)
        }
        else {
            addErrorStyleClass(formFields)
        }

        return truthy
    }

    /**
     * Adds error styling on form fields if they don't have one applied.
     *
     * @param formFields Form fields
     */
    private static void addErrorStyleClass(Control... formFields) {
        assert formFields

        formFields.
                findAll { !it.styleClass.contains(FORM_FIELD_ERROR_STYLE) }.
                each { it.styleClass.add(FORM_FIELD_ERROR_STYLE) }
    }

    /**
     * Removes error styling on form fields.
     *
     * @param formFields Form fields
     */
    private static void removeErrorStyleClass(Control... formFields) {
        assert formFields

        formFields.each { it.styleClass.remove(FORM_FIELD_ERROR_STYLE) }
    }


}
