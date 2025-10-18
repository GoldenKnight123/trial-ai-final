package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.AudioPlayer;
import nz.ac.auckland.se206.ChatHistoryManager;
import nz.ac.auckland.se206.GameTimer;

/** Controller for the verdict input screen. */
public class VerdictController extends Controller {

  @FXML private TextArea verdictTxt;
  @FXML private Button btnNotGuilty;
  @FXML private Button btnGuilty;
  @FXML private Label lblGameOverMessage;
  @FXML private Label lblTimer;
  @FXML private Rectangle rectFade;

  private AudioPlayer audioPlayer = AudioPlayer.getInstance();

  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);

    // Unfocus text area to avoid auto-focusing keyboard on scene load
    lblTimer.requestFocus();

    lblTimer.setVisible(true);

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

    btnGuilty.setOnMouseEntered(
        event -> {
          btnGuilty.setText("> Guilty");
          audioPlayer.playSound("computerTalk2", 0.5);
        });

    btnGuilty.setOnMouseExited(
        event -> {
          btnGuilty.setText("Guilty");
        });

    btnNotGuilty.setOnMouseEntered(
        event -> {
          btnNotGuilty.setText("> Not Guilty");
          audioPlayer.playSound("computerTalk2", 0.5);
        });
    btnNotGuilty.setOnMouseExited(
        event -> {
          btnNotGuilty.setText("Not Guilty");
        });
  }

  public void fadeIn() {
    rectFade.setVisible(true);
    TranslateTransition tt = new TranslateTransition();
    tt.setNode(rectFade);
    tt.setFromY(0);
    tt.setToY(740);
    tt.setDuration(javafx.util.Duration.seconds(2));
    tt.play();
    audioPlayer.playSound("scan");
  }

  @FXML
  private void onSelectGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question and has completed all interactables
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()
        && !ChatHistoryManager.getInstance().interactablesComplete()) {
    } else {
      String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
      boolean wasCorrect = false; // guilty means incorrect in current game wiring
      if (text.trim().isEmpty()) {
        // default to incorrect if no rationale provided
        wasCorrect = false;
      }

      audioPlayer.playSound("computerTalk", 0.5);

      GameTimer.getInstance().stop();
      App.openDebrief(event, wasCorrect);
    }
  }

  @FXML
  private void onSelectNotGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()
        && !ChatHistoryManager.getInstance().interactablesComplete()) {
    } else {
      String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
      boolean wasCorrect = true; // not guilty means correct in current game wiring
      if (text.trim().isEmpty()) {
        // default to incorrect if no rationale provided
        wasCorrect = false;
      }

      audioPlayer.playSound("computerTalk", 0.5);

      GameTimer.getInstance().stop();
      App.openDebrief(event, wasCorrect);
    }
  }

  /**
   * Bind a timer-expired callback so when the GameTimer expires while on the verdict scene we
   * display an explicit message.
   */
  public void bindTimerExpired() {
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              try {
                App.openGameOver(null, "Failed to make a verdict in time.");

              } catch (IOException e) {
                e.printStackTrace();
              }
              GameTimer.getInstance().stop();
              lblTimer.setVisible(false);
            });
  }

  public String getVerdictText() {
    String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
    System.out.println(text);
    return text;
  }

  @FXML
  private void onKeyTyped() {
    audioPlayer.playRandomKeyboardSound(0.5);
  }
}
