package com.github.choonchernlim.calsync.ui.core

import com.google.inject.Guice
import com.google.inject.Injector
import javafx.application.Application
import javafx.scene.control.Dialog
import javafx.stage.Stage

final class App extends Application {
    static void main(String[] args) {
        launch(App, args)
    }

    @Override
    void start(final Stage primaryStage) throws Exception {
        final Injector injector = Guice.createInjector(new GuiceModule())
        final FxmlLoaderService fxmlLoaderService = injector.getInstance(FxmlLoaderService.class)

        final Dialog root = fxmlLoaderService.load(FxmlEnum.CONFIGURATION_DIALOG)
        root.showAndWait()

//        final Parent root = fxmlLoaderService.load(FxmlEnum.MAIN)
//
//        primaryStage.setTitle('FXML Welcome')
//        primaryStage.setScene(new Scene(root))
//        primaryStage.setMinWidth(800)
//        primaryStage.setMinHeight(600)
//        primaryStage.show()
    }
}
