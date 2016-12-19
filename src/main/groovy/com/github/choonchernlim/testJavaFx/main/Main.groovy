package com.github.choonchernlim.testJavaFx.main

import com.github.choonchernlim.testJavaFx.constant.FxmlEnum
import com.github.choonchernlim.testJavaFx.guice.GuiceModule
import com.github.choonchernlim.testJavaFx.service.FxmlLoaderService
import com.google.inject.Guice
import com.google.inject.Injector
import javafx.application.Application
import javafx.scene.control.Dialog
import javafx.stage.Stage

final class Main extends Application {
    static void main(String[] args) {
        launch(Main, args)
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
