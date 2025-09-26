package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiWitnessController extends ChatController {

  @FXML private ImageView imgInteractable;
  @FXML private ImageView imgOverlay;
  @FXML private Rectangle rectOverlay;
  @FXML private TextField txtTerminal;
  @FXML private TextField txtTerminalTemplate;
  @FXML private TextArea txtaTerminalFixed;
  @FXML private Label lblTerminalError;

  private boolean interactableState = false;
  private boolean interactableComplete = false;
  private boolean interactionDialoguePlayed = false;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Cap txtTerminal to 26 characters
    txtTerminal
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              if (newValue.length() > 26) {
                txtTerminal.setText(oldValue);
              }
            });

    // Create a highlight effect for hover
    ColorAdjust highlight = new ColorAdjust();
    highlight.setBrightness(0.3);

    // Add hover effects to the interactable image
    imgInteractable.setOnMouseEntered(
        event -> {
          if (getCurrentSpeaker() == "") {
            imgInteractable.setEffect(highlight);
          }
        });

    // Remove effect when mouse leaves
    imgInteractable.setOnMouseExited(
        event -> {
          imgInteractable.setEffect(null);
        });
  }

  @FXML
  public void onInteractableClick(MouseEvent event) {
    // Triggers when the interactable image is clicked
    if (getCurrentSpeaker() == "") {
      rectOverlay.setVisible(true);
      imgOverlay.setVisible(true);
      if (!interactableComplete) {
        txtTerminalTemplate.setVisible(true);
        txtTerminal.setVisible(true);
        txtTerminal.requestFocus();
      }
      txtaTerminalFixed.setVisible(true);
      interactableState = true;
    }
  }

  /**
   * Function that handles when the user clicks outside the interaction area while it is open (black
   * areas on the edge) and closes the interaction overlay.
   *
   * @param event the mouse event that triggered the method
   */
  @FXML
  public void onCloseInteraction(MouseEvent event) {
    // Closes the overlay when clicked
    rectOverlay.setVisible(false);
    imgOverlay.setVisible(false);
    txtTerminalTemplate.setVisible(false);
    txtTerminal.setVisible(false);
    txtaTerminalFixed.setVisible(false);
    lblTerminalError.setVisible(false);
    interactableState = false;
    if (interactableComplete && !interactionDialoguePlayed) {
      getChatCompletionRequest()
          .addMessage(
              new ChatMessage(
                  "user",
                  "The judge uses the terminal to decrypt a message. It states: Manager (sent to"
                      + " INDUS-07): \"Remember to install the new Helios system before the end of"
                      + " the week.\""));
      ChatHistoryManager.getInstance()
          .addMessage(
              getTarget(),
              new ChatMessage(
                  "user",
                  "The judge uses the terminal to decrypt a message. It states: Manager (sent to"
                      + " INDUS-07): \"Remember to install the new Helios system before the end of"
                      + " the week.\""));
      displayInteractionDialogue();
      interactionDialoguePlayed = true;
    }
  }

  /**
   * Function that handles when the user presses enter in the terminal while the interaction overlay
   * is open.
   *
   * @param event the key event that triggered the method
   */
  @FXML
  public void onTerminalEnter(KeyEvent event) {
    if (!interactableState) {
      return;
    }

    if (event.getCode() == KeyCode.ENTER) {
      if (txtTerminal.getText().equals("decrypt -r message.private")) {
        txtaTerminalFixed.setText(
            "Message Received at 6/7/2055 12:36PM\n"
                + "[Status: Viewable]\n"
                + "--------------------------------------------\n");
        appendTextWithTypewriterEffect(
            txtaTerminalFixed,
            "Manager (sent to INDUS-07): \"Remember to install the new Helios system before the end"
                + " of the week.\"");
        txtTerminal.setText("");
        txtTerminal.setEditable(false);
        txtTerminalTemplate.setVisible(false);
        txtTerminal.setVisible(false);
        lblTerminalError.setVisible(false);
        interactableComplete = true;

        // Mark interactable as complete
        ChatHistoryManager.getInstance().markInteractableComplete("LOGOS-09");
      } else {
        lblTerminalError.setVisible(true);
      }
    }
  }
}
