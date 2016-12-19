package com.github.choonchernlim.calsync.ui.core

enum FxmlEnum {
    MAIN("fxml/main.fxml"),
    RIGHT_PANE("fxml/right-pane.fxml"),
    CONFIGURATION_DIALOG("fxml/configuration-dialog.fxml")

    private final String path

    FxmlEnum(final String path) {
        this.path = path
    }

    String getPath() {
        return path
    }
}
