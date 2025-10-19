package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;

public class GameOverController {
  @FXML private Pane paneGameOver;
  @FXML private Label lblGameOverMessage;

  @FXML
  public void fadeIn() {
    System.out.println("Fading in game over screen");

    // Ensure the game over screen itself is fully visible first
    paneGameOver.setOpacity(1.0);

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(paneGameOver);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);
    fadeTransition.play();
  }

  @FXML
  public void initialize() {
    fadeIn();
  }

  @FXML
  private void onRestart(ActionEvent event) throws IOException {
    App.reinitialize();
    App.openStartScreen(null);
  }

  public void setGameOverMessage(String message) {
    lblGameOverMessage.setText(message);
  }
}
