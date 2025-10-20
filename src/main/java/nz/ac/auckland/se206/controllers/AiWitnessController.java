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
import nz.ac.auckland.se206.AudioPlayer;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiWitnessController extends ChatController {

  @FXML private ImageView imgInteractable;
  @FXML private ImageView imgOverlay;
  @FXML private ImageView imgClose;
  @FXML private Rectangle rectOverlay;
  @FXML private TextField txtTerminal;
  @FXML private TextField txtTerminalTemplate;
  @FXML private TextArea txtaTerminalFixed;
  @FXML private Label lblTerminalError;

  private boolean interactableState = false;
  private boolean interactableComplete = false;
  private boolean interactionDialoguePlayed = false;

  private AudioPlayer audioPlayer;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Get the universal audio player instance
    audioPlayer = AudioPlayer.getInstance();

    // Cap txtTerminal to 26 characters
    txtTerminal
        .textProperty()
        .addListener(
            (observable, oldValue, newValue) -> {
              // Play a typing sound
              if (newValue.length() > oldValue.length()) {
                audioPlayer.playRandomKeyboardSound(0.5);
              }
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
      imgClose.setVisible(true);
      if (!interactableComplete) {
        txtTerminalTemplate.setVisible(true);
        txtTerminal.setVisible(true);
        txtTerminal.requestFocus();
      }
      txtaTerminalFixed.setVisible(true);
      interactableState = true;

      // Play computer open sound
      audioPlayer.playSound("computerOpen", 0.5);
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
    // Stop any ongoing typewriter effect and display full text instantly
    if (interactableComplete) {
      stopTypewriterEffect();
      txtaTerminalFixed.setText(
          "Message Received at 6/7/2055 12:36PM\n"
              + "[Status: Viewable]\n"
              + "--------------------------------------------\n"
              + "Manager (sent to INDUS-07): \"Remember to install the new Helios system as soon as"
              + " possible, this is top priority.\"");
    }

    // Closes the overlay when clicked
    rectOverlay.setVisible(false);
    imgOverlay.setVisible(false);
    imgClose.setVisible(false);

    // Hide terminal elements and exit interaction state
    txtTerminalTemplate.setVisible(false);
    txtTerminal.setVisible(false);
    txtaTerminalFixed.setVisible(false);
    lblTerminalError.setVisible(false);
    interactableState = false;

    // Play computer close sound
    audioPlayer.playSound("computerClose", 0.5);

    // Display interaction if interaction is complete and dialogue if not already played
    if (interactableComplete && !interactionDialoguePlayed) {
      getChatCompletionRequest()
          .addMessage(
              new ChatMessage(
                  "system",
                  "The judge uses the terminal to decrypt a message. It states: Manager (sent to"
                      + " INDUS-07): \"Remember to install the new Helios system as soon as"
                      + " possible, this is top priority.\""));
      ChatHistoryManager.getInstance()
          .addMessage(
              getTarget(),
              new ChatMessage(
                  "system",
                  "The judge uses the terminal to decrypt a message. It states: Manager (sent to"
                      + " INDUS-07): \"Remember to install the new Helios system as soon as"
                      + " possible, this is top priority.\""));

      // Display interaction dialogue and mark as played
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

    // Only process if enter key is pressed
    if (event.getCode() == KeyCode.ENTER) {
      // Check if the command is correct
      if (txtTerminal.getText().equals("decrypt -r message.private")) {
        // Display decrypted message
        txtaTerminalFixed.setText(
            "Message Received at 6/7/2055 12:36PM\n"
                + "[Status: Viewable]\n"
                + "--------------------------------------------\n");
        displayTextWithTypewriterEffect(
            txtaTerminalFixed,
            "Manager (sent to INDUS-07): \"Remember to install the new Helios system as soon as"
                + " possible, this is top priority.\"",
            50,
            false,
            true,
            "computer2");
        // Clear and disable input field
        txtTerminal.setText("");
        txtTerminal.setEditable(false);

        // Hide terminal elements
        txtTerminalTemplate.setVisible(false);
        txtTerminal.setVisible(false);
        lblTerminalError.setVisible(false);
        interactableComplete = true;

        // Mark interactable as complete
        ChatHistoryManager.getInstance().markInteractableComplete("LOGOS-09");
      } else {
        audioPlayer.playSound("error", 0.5);
        lblTerminalError.setVisible(true);
      }
    }
  }
}
