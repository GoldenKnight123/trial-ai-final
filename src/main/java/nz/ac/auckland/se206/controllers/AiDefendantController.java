package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectOverlay;
  @FXML private Rectangle rectComputer;
  @FXML private ImageView imgTablet;
  @FXML private Slider sldrProtocol;
  @FXML private Button btnConfirm;
  @FXML private Pane paneInteractable;
  @FXML private ImageView imgOverride;
  @FXML private TextArea txtaEmail;
  @FXML private Label lblRecipient;
  @FXML private Pane panePasswordScreen;
  @FXML private Rectangle rectOverlay1;
  @FXML private TextField txtFieldPassword;
  @FXML private Label lblHint;
  @FXML private TextField txtaInstruction;

  private boolean interactableComplete = false;
  private boolean passwordScreenShown = false;

  // private boolean interactionDialoguePlayed = false;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Create a highlight effect for hover
    ColorAdjust highlight = new ColorAdjust();
    highlight.setBrightness(0.5);

    rectComputer.setOnMouseEntered(
        event -> {
          if (getCurrentSpeaker() == "") {
            rectComputer.setEffect(highlight);
          }
        });
    rectComputer.setOnMouseExited(
        event -> {
          rectComputer.setEffect(null);
        });

    if (txtFieldPassword != null) {
      txtFieldPassword.setOnAction((ActionEvent e) -> handlePassword());
      txtFieldPassword.setOnKeyPressed(
          ke -> {
            if (ke.getCode() == KeyCode.ENTER) {
              handlePassword();
            }
          });
    }
  }

  @FXML
  private void onInteractable(MouseEvent event) {
    if (!passwordScreenShown) {
      if (panePasswordScreen != null) {
        panePasswordScreen.setVisible(true);
      }
      passwordScreenShown = true;
      return;
    }

    paneInteractable.setVisible(true);

    if (!interactableComplete) {
      rectOverlay.setDisable(true); // Disable overlay on first click before completion
    } else {
      rectOverlay.setDisable(false); // Enable overlay after interactable is completed
      imgOverride.setVisible(true);
    }
  }

  @FXML
  private void onCloseInteraction(MouseEvent event) {
    // If password screen is visible, close it and do not trigger completion logic
    if (panePasswordScreen != null && panePasswordScreen.isVisible()) {
      panePasswordScreen.setVisible(false);
      return;
    }

    paneInteractable.setVisible(false);
    imgOverride.setVisible(false);
    completeInteraction();
  }

  private void handlePassword() {
    if (txtFieldPassword == null || panePasswordScreen == null) {
      return;
    }

    String entered = txtFieldPassword.getText() == null ? "" : txtFieldPassword.getText().trim();

    final String CORRECT = "indus";

    if (entered.equals(CORRECT)) {
      panePasswordScreen.setVisible(false);
      passwordScreenShown = true;
      paneInteractable.setVisible(true);
      if (rectOverlay != null) {
        rectOverlay.setDisable(true);
      }
    } else {
      txtFieldPassword.clear();
      txtFieldPassword.setPromptText("Incorrect password — try again");
    }
  }

  private void completeInteraction() {
    if (interactableComplete) {
      paneInteractable.setVisible(false);
      imgOverride.setVisible(false);

      getChatCompletionRequest()
          .addMessage(
              new ChatMessage(
                  "system",
                  "The judge is in the AI defendant's memory where the judge themselves is asked to"
                      + " use a slider, choosing between the Safety Protocol which will hold the"
                      + " current power output or the Efficiency Protocol which will increase the"
                      + " power output. This places them in Indus-07 shoes when it was instructed"
                      + " to deploy the new technology. However, when the user confirms their"
                      + " choice, a message from the upper management that says (Management"
                      + " Override: Proceed to 100% Efficiency) which gives the illusion that the"
                      + " judge had a choice which relates to the fact that INDUS-07 was pressured "
                      + " to implement the new technology. Convey that despite of your concerns of"
                      + " about the risks of the new technology, management has issued a direct"
                      + " override."));

      // Add to chat history
      // getChatCompletionRequest().addMessage(new ChatMessage("assistant", message));
      // ChatHistoryManager.getInstance().addMessage(getTarget(), new ChatMessage("user", message));

      displayInteractionDialogue();

      // Display to chatbox
      // displayTextWithTypewriterEffect(txtaChat, message);
    }
  }

  @FXML
  private void onRectClick(MouseEvent event) {
    Object source = event.getSource();

    if (source instanceof Rectangle rect) {
      String id = rect.getId();
      switch (id) {
        case "rectAiCore":
          rect.setOpacity(1.0);
          lblRecipient.setText("From: AI Core");
          txtaEmail.setText(
              "Preliminary models predict instability of new Helios Module at 37%.\n\n"
                  + //
                  "Requesting further testing authorization before deployment.");
          break;
        case "rectManager":
          rect.setOpacity(1.0);
          lblRecipient.setText("From: Manager");
          txtaEmail.setText(
              "Your role is to comply with operational directives.\n\n"
                  + //
                  "Installation proceeds tomorrow.\n\n"
                  + //
                  "OVERRIDE AUTHORISATION: Upper Management Approval ID 4237-A.");
          break;
        case "rectSystem":
          rect.setOpacity(1.0);
          lblRecipient.setText("From: System");
          txtaEmail.setText(
              "Installation of the Helios Module has been authorized by Upper Management.\n\n"
                  + //
                  "Your objections are logged but overridden. Proceed with deployment as"
                  + " instructed.");
          break;
        default:
      }
    } else {
      System.out.println("Clicked source is not a Rectangle: " + source);
    }
  }
}
