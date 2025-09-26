package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ChatHistoryManager;
import nz.ac.auckland.se206.GameTimer;

/** Controller for the verdict input screen. */
public class VerdictController extends Controller {

  @FXML private TextArea verdictTxt;
  @FXML private Button btnNotGuilty;
  @FXML private Button btnGuilty;
  @FXML private Pane paneGameOver;
  @FXML private Label lblGameOverMessage;
  @FXML private Button btnRestart;
  @FXML private Pane paneBackground;
  @FXML private ImageView imgTimer;
  @FXML private Label lblTimer;
  @FXML private Arc arcTimer;

  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);

    imgTimer.setVisible(true);
    lblTimer.setVisible(true);
    arcTimer.setVisible(true);

    // Disable verdict buttons until user enters non-whitespace text
    if (btnGuilty != null) {
      btnGuilty.setDisable(true);
    }
    if (btnNotGuilty != null) {
      btnNotGuilty.setDisable(true);
    }

    // Listen for text changes and enable buttons only when there's non-whitespace content
    if (verdictTxt != null) {
      verdictTxt
          .textProperty()
          .addListener(
              (obs, oldText, newText) -> {
                boolean hasText = newText != null && !newText.trim().isEmpty();
                if (btnGuilty != null) {
                  btnGuilty.setDisable(!hasText);
                }
                if (btnNotGuilty != null) {
                  btnNotGuilty.setDisable(!hasText);
                }
              });
    }
  }

  @FXML
  private void onSelectGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question and has completed all interactables
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()
        && !ChatHistoryManager.getInstance().interactablesComplete()) {
      paneGameOver.setVisible(true); // game over
    } else {
      String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
      boolean wasCorrect = false; // guilty means incorrect in current game wiring
      if (text.trim().isEmpty()) {
        // default to incorrect if no rationale provided
        wasCorrect = false;
      }

      if (!wasCorrect) {
        paneBackground.setStyle("-fx-background-color: #FF5757;");
      }

      GameTimer.getInstance().stop();
      App.openDebrief(event, wasCorrect);
    }
  }

  @FXML
  private void onSelectNotGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()
        && !ChatHistoryManager.getInstance().interactablesComplete()) {
      paneGameOver.setVisible(true); // game over
    } else {
      String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
      boolean wasCorrect = true; // not guilty means correct in current game wiring
      if (text.trim().isEmpty()) {
        // default to incorrect if no rationale provided
        wasCorrect = false;
      }

      if (!wasCorrect) {
        paneBackground.setStyle("-fx-background-color: #FF5757;");
      }

      GameTimer.getInstance().stop();
      App.openDebrief(event, wasCorrect);
    }
  }

  public void setGameOverVisible() {
    paneGameOver.setVisible(true);
    GameTimer.getInstance().stop();
    imgTimer.setVisible(false);
    lblTimer.setVisible(false);
    arcTimer.setVisible(false);
  }

  /**
   * Bind a timer-expired callback so when the GameTimer expires while on the verdict scene we
   * display an explicit message.
   */
  public void bindTimerExpired() {
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              // Show message indicating player didn't choose in time
              paneGameOver.setVisible(true);
              lblGameOverMessage.setText("You didn't choose in time");
              GameTimer.getInstance().stop();
              imgTimer.setVisible(false);
              lblTimer.setVisible(false);
              arcTimer.setVisible(false);
            });
  }

  public String getVerdictText() {
    String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
    System.out.println(text);
    return text;
  }

  @FXML
  private void onRestart(ActionEvent event) throws IOException {
    App.openStartScreen(null);
    App.reinitialize();
  }
}
