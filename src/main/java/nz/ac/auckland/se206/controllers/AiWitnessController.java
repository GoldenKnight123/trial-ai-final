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
import javafx.scene.text.Font;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiWitnessController extends ChatController {

  @FXML ImageView imgInteractable;
  @FXML ImageView imgOverlay;
  @FXML Rectangle rectOverlay;
  @FXML TextField txtTerminal;
  @FXML TextField txtTerminalTemplate;
  @FXML TextArea txtaTerminalFixed;
  @FXML Label lblTerminalError;

  private boolean interactableState = false;
  private boolean interactableComplete = false;
  private boolean interactionDialoguePlayed = false;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Load font at the size you're using in CSS
    Font.loadFont(getClass().getResourceAsStream("/fonts/VT323-Regular.ttf"), 48);

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
      } else {
        lblTerminalError.setVisible(true);
      }
    }
  }
}
