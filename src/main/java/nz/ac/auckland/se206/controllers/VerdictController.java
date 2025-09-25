package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.ChatHistoryManager;

/** Controller for the verdict input screen. */
public class VerdictController extends Controller {

  @FXML private TextField verdictTxt;
  @FXML private Pane paneGameOver;
  @FXML private Button btnRestart;
  @FXML private Pane paneBackground;

  @FXML
  private void onGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()) {
      paneGameOver.setVisible(true);
    } else {
      String text = verdictTxt.getText() == null ? "" : verdictTxt.getText();
      boolean wasCorrect = false; // guilty means incorrect in current game wiring
      if (text.trim().isEmpty()) {
        // default to incorrect if no rationale provided
        wasCorrect = false;
      }

      // Switch to debrief scene and call controller directly with verdict text
      App.setRoot("debrief");
      nz.ac.auckland.se206.controllers.DebriefController debriefController =
          (nz.ac.auckland.se206.controllers.DebriefController) App.getController("debrief");
      debriefController.setCorrectLabel(wasCorrect ? "You were CORRECT!" : "You were INCORRECT!");
      debriefController.runGptDebrief(text, wasCorrect);

      if (!wasCorrect) {
        paneBackground.setStyle("-fx-background-color: #FF5757;");
      }
    }
  }

  @FXML
  private void onNotGuilty(ActionEvent event) throws IOException {
    // Check if the player has asked each participant a question
    if (!ChatHistoryManager.getInstance().haveSpokenToAll()) {
      paneGameOver.setVisible(true);
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
    }
  }

  @FXML
  private void onRestart(ActionEvent event) throws IOException {
    App.setRoot("startScreen");
  }
}
