package nz.ac.auckland.se206.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.AudioPlayer;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectOverlay;
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
  @FXML private ImageView imgUnread;
  @FXML private ImageView imgUnread1;
  @FXML private ImageView imgUnread2;
  @FXML private ImageView imgInteractable;

  private AudioPlayer audioPlayer;
  private boolean interactableComplete = false;
  private int interactableCompleteCount = 0;
  private boolean passwordAccepted = false;
  private boolean reactAiCoreViewed = false;
  private boolean reactManagerViewed = false;
  private boolean reactSystemViewed = false;

  // private boolean interactionDialoguePlayed = false;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    audioPlayer = AudioPlayer.getInstance();

    // Create a subtle red tint and glows: a persistent base glow and a stronger hover glow
    ColorAdjust tint = new ColorAdjust();
    tint.setBrightness(0.04);
    tint.setSaturation(0.12);

    // Base (always-on) glow
    DropShadow baseGlow =
        new DropShadow(BlurType.GAUSSIAN, Color.web("#ff6b6b", 0.55), 10, 0.15, 0, 0);
    baseGlow.setInput(tint);

    // Hover (intensified) glow
    DropShadow hoverGlow =
        new DropShadow(BlurType.GAUSSIAN, Color.web("#ff6b6b", 0.85), 26, 0.45, 0, 0);
    hoverGlow.setInput(tint);

    // Apply the base glow by default so it always appears
    imgInteractable.setEffect(baseGlow);

    // Intensify glow on hover, but only if there's no current speaker (typing)
    imgInteractable.setOnMouseEntered(
        event -> {
          if (getCurrentSpeaker().isEmpty()) {
            imgInteractable.setEffect(hoverGlow);
          }
        });

    // Restore base glow when mouse leaves
    imgInteractable.setOnMouseExited(
        event -> {
          imgInteractable.setEffect(baseGlow);
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

    audioPlayer.playSound("computerOpen", 0.5);

    // Only allow opening the interactable if the correct password has been entered.
    if (!passwordAccepted) {
      if (panePasswordScreen != null) {
        panePasswordScreen.setVisible(true);
      }
      return;
    }

    paneInteractable.setVisible(true);
    lblEmailInstruct.setText("Review the emails regarding the Helios Module deployment.");
  }

  @FXML
  private void onCloseInteraction(MouseEvent event) {

    audioPlayer.playSound("computerClose", 0.5);

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
    System.out.println("Rectangle clicked");
    Object source = event.getSource();
    audioPlayer.playSound("emailOpen", 0.5);

    if (source instanceof Rectangle) {
      Rectangle rect = (Rectangle) source;
      String id = rect.getId();
      switch (id) {
        case "rectAiCore":
          reactAiCoreViewed = markFirstView(reactAiCoreViewed, imgUnread);
          rect.setOpacity(1.0);
          lblRecipient.setText("From: AI Core");
          txtaEmail.setText(
              "Preliminary models predict instability of new Helios Module at 37%.\n\n"
                  + //
                  "Requesting further testing authorization before deployment.");
          interactableCompleteCount++;
          break;
        case "rectManager":
          // Hide the unread icon the first time this email is viewed
          reactManagerViewed = markFirstView(reactManagerViewed, imgUnread1);
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
          // Hide the unread icon the first time this email is viewed
          reactSystemViewed = markFirstView(reactSystemViewed, imgUnread2);
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
      passwordAccepted = true;
      paneInteractable.setVisible(true);
      audioPlayer.playSound("passwordAccepted", 0.5);
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
      audioPlayer.playSound("error", 0.5);

      // Move focus away from the text field so the prompt text (if set) is visible
      if (panePasswordScreen != null) {
        panePasswordScreen.requestFocus();
      }
    }
  }

  /**
   * Marks an item as viewed (hides unread icon) and returns the new viewed state.
   *
   * @param hasBeenViewed current viewed state
   * @param unreadIcon icon to hide when first viewed
   * @return true if the item is now considered viewed
   */
  private boolean markFirstView(boolean hasBeenViewed, ImageView unreadIcon) {
    if (!hasBeenViewed) {
      if (unreadIcon != null) {
        try {
          unreadIcon.setVisible(false);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return true;
    }
    return hasBeenViewed;
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

      displayInteractionDialogue();
    }
  }
}
