package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectOverlay;
  @FXML private Rectangle rectComputer;
  @FXML private Pane paneInteractable;
  @FXML private ImageView imgOverride;
  @FXML private TextArea txtaEmail;
  @FXML private Label lblRecipient;
  @FXML private Rectangle rectAiCore;
  @FXML private Rectangle rectManager;
  @FXML private Rectangle rectSystem;
  @FXML private Pane panePasswordScreen;
  @FXML private Rectangle rectOverlay1;
  @FXML private TextField txtFieldPassword;
  @FXML private Label lblEmailInstruct;
  @FXML private Label lblHint;

  private boolean interactableComplete = false;
  private int interactableCompleteCount = 0;
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
    lblEmailInstruct.setText("Review the emails regarding the Helios Module deployment.");
  }

  @FXML
  private void onCloseInteraction(MouseEvent event) {
    // If password screen is visible, close it and do not trigger completion logic
    if (panePasswordScreen != null && panePasswordScreen.isVisible()) {
      panePasswordScreen.setVisible(false);
      return;
    }

    paneInteractable.setVisible(false);
    completeInteraction();
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
          interactableCompleteCount++;
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
          interactableCompleteCount++;
          break;
        case "rectSystem":
          rect.setOpacity(1.0);
          lblRecipient.setText("From: System");
          txtaEmail.setText(
              "Installation of the Helios Module has been authorized by Upper Management.\n\n"
                  + //
                  "Your objections are logged but overridden. Proceed with deployment as"
                  + " instructed.");
          interactableCompleteCount++;
          break;
        default:
      }
    } else {
      System.out.println("Clicked source is not a Rectangle: " + source);
    }
  }

  /** Handle password submission from the password TextField. */
  private void handlePassword() {
    if (txtFieldPassword == null || panePasswordScreen == null) {
      return;
    }

    String entered =
        txtFieldPassword.getText() == null ? "" : txtFieldPassword.getText().trim().toLowerCase();
    final String CORRECT = "indus-07";

    if (entered.equals(CORRECT)) {
      panePasswordScreen.setVisible(false);
      passwordScreenShown = true;
      paneInteractable.setVisible(true);
      if (lblEmailInstruct != null) {
        lblEmailInstruct.setText("Review the emails regarding the Helios Module deployment.");
      }
      if (rectOverlay != null) {
        rectOverlay.setDisable(true);
      }
    } else {
      // Clear the field and show an explicit hint label so the user sees the error
      txtFieldPassword.clear();
      txtFieldPassword.setPromptText("Incorrect password — try again");

      // Move focus away from the text field so the prompt text (if set) is visible
      if (panePasswordScreen != null) {
        panePasswordScreen.requestFocus();
      }
    }
  }

  /** Called when the interactable has been completed (all emails viewed). */
  private void completeInteraction() {
    // Only run completion once when all three email rectangles have been viewed
    if (interactableCompleteCount >= 3 && !interactableComplete) {
      interactableComplete = true;

      // Mark this interactable as completed
      ChatHistoryManager.getInstance().markInteractableComplete("INDUS-07");

      paneInteractable.setVisible(false);
      if (imgOverride != null) {
        imgOverride.setVisible(false);
      }

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

      displayInteractionDialogue();
    }
  }
}
