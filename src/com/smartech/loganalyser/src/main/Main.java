package com.smartech.loganalyser.src.main;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	@Override
	public void start(final Stage stage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource(
				"../view/Main.fxml"));

		Scene scene = new Scene(root);

		stage.setScene(scene);
		stage.setTitle("Log Analyser");
		stage.setMaximized(true);
		stage.setHeight(1050);
		stage.show();
		stage.addEventHandler(KeyEvent.KEY_PRESSED,
				new EventHandler<KeyEvent>() {

					public void handle(KeyEvent ev) {
						if (ev.isControlDown() && ev.getCode() == KeyCode.F) {
							stage.setFullScreen(true);
						}
					}

				});
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
            }

        });
	}

	public static void main(String[] args) {
		launch(args);
	}
}
