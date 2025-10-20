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
import nz.ac.auckland.se206.GameTimer;

/** Controller for the verdict input screen. */
public class VerdictController extends Controller {

  @FXML private TextArea verdictTxt;
  @FXML private Button btnNotGuilty;
  @FXML private Button btnGuilty;
  @FXML private Button btnConfirm;
  @FXML private Label lblGameOverMessage;
  @FXML private Label lblTimer;
  @FXML private Label lblInstruction;
  @FXML private Rectangle rectFade;

  private AudioPlayer audioPlayer = AudioPlayer.getInstance();
  private boolean wasCorrect = false;

  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);

    lblTimer.setVisible(true);

    // Hide verdict text and instruction label initially
    verdictTxt.setVisible(false);
    lblInstruction.setVisible(false);

    // Disable confirm button until user enters non-whitespace text
    if (btnConfirm != null) {
      btnConfirm.setDisable(true);
    }

    // Listen for text changes and enable buttons only when there's non-whitespace content
    if (verdictTxt != null) {
      verdictTxt
          .textProperty()
          .addListener(
              (obs, oldText, newText) -> {
                boolean hasText = newText != null && !newText.trim().isEmpty();
                if (btnConfirm != null) {
                  btnConfirm.setDisable(!hasText);
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

    btnConfirm.setOnMouseEntered(
        event -> {
          btnConfirm.setText("> Confirm");
          audioPlayer.playSound("computerTalk2", 0.5);
        });
    btnConfirm.setOnMouseExited(
        event -> {
          btnConfirm.setText("Confirm");
        });
  }

  public void fadeIn() {
    // Show and animate the fade rectangle
    rectFade.setVisible(true);
    TranslateTransition tt = new TranslateTransition();
    tt.setNode(rectFade);
    tt.setFromY(0);
    tt.setToY(740);
    tt.setDuration(javafx.util.Duration.seconds(2));
    tt.play();
    audioPlayer.playSound("scan", 0.5);
  }

  private void switchToRationale() {
    audioPlayer.playSound("computerTalk", 0.5);

    btnGuilty.setVisible(false);
    btnNotGuilty.setVisible(false);
    verdictTxt.setVisible(true);
    lblInstruction.setVisible(true);
    btnConfirm.setVisible(true);

    // Change timer timeout actions to auto-confirm rationale
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              try {
                onConfirmRationale(null);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }

  @FXML
  private void onSelectGuilty(ActionEvent event) throws IOException {
    wasCorrect = false; // guilty means incorrect in current game wiring

    switchToRationale();
  }

  @FXML
  private void onSelectNotGuilty(ActionEvent event) throws IOException {
    wasCorrect = true; // not guilty means correct in current game wiring

    switchToRationale();
  }

  /**
   * Bind a timer-expired callback so when the GameTimer expires while on the verdict scene we
   * display an explicit message.
   */
  public void bindTimerExpired() {
    // Set the timer expired callback
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

  @FXML
  private void onConfirmRationale(ActionEvent event) throws IOException {
    // Stop the game timer
    GameTimer.getInstance().stop();
    lblTimer.setVisible(false);

    audioPlayer.playSound("computerTalk2", 0.5);

    App.openDebrief(event, wasCorrect);
  }
}
