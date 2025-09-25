package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;

public class StartMenuController {
  @FXML private Button btnStartGame;
  @FXML private Pane room;

  private boolean isFading = false; // Flag to track if a fade is in progress

  public void fadeOut(ActionEvent event) {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(500));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          try {
            App.openRoom(null);
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    fadeTransition.play();
  }

  public void fadeIn() {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(500));
    fadeTransition.setNode(room);
    System.out.println("Fading in start menu");
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);
    fadeTransition.play();
  }

  @FXML
  private void onStart(ActionEvent event) throws ApiProxyException, IOException {
    if (isFading) {
      return; // Exit if a fade is already in progress
    }

    fadeOut(event);
  }
}
