package nz.ac.auckland.se206.controllers;

import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectOverlay;
  @FXML private Rectangle rectComputer;
  @FXML private ImageView imgTablet;
  @FXML private Slider sldrProtocol;
  @FXML private Button btnConfirm;
  @FXML private TextArea txtaChat;
  @FXML private Pane paneInteractable;
  @FXML private ImageView imgOverride;
  @FXML private Canvas canvasGlitch;
  @FXML private TextArea txtMemoryLogFixed;

  private boolean interactableComplete = false;
  private int scrubCount = 0;
  private final int scrubThreshold = 5;
  private GraphicsContext gc;

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
  }

  @FXML
  private void onInteractable(MouseEvent event) {
    paneInteractable.setVisible(true);
    glitchInteractable();

    if (!interactableComplete) {
      rectOverlay.setDisable(true); // Disable overlay on first click before completion
    } else {
      rectOverlay.setDisable(false); // Enable overlay after interactable is completed
      imgOverride.setVisible(true);
    }
  }

  @FXML
  private void onCloseInteraction(MouseEvent event) {
    paneInteractable.setVisible(false);
    imgOverride.setVisible(false);
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

  private void glitchInteractable() {
    drawStatic(canvasGlitch);
    gc = canvasGlitch.getGraphicsContext2D();
    memoryLog();

    // Draw the static overlay
    drawStatic(canvasGlitch);

    canvasGlitch.setOnMouseDragged(event -> onCanvasDragged(event));

    canvasGlitch.setOnMouseClicked(event -> finishTypewriterEffectInstantly());
    if (txtMemoryLogFixed != null) {
      txtMemoryLogFixed.setOnMouseClicked(event -> finishTypewriterEffectInstantly());
    }

    // Reset scrub counter
    scrubCount = 0;
  }

  public void onCanvasDragged(MouseEvent event) {
    double x = event.getX();
    double y = event.getY();

    // Erase a circular area under the mouse
    gc.clearRect(x - 20, y - 20, 40, 40);

    // Count scrubs
    scrubCount++;

    checkRevealProgress();
  }

  private void drawStatic(Canvas canvas) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    Random random = new Random();

    for (int x = 0; x < canvas.getWidth(); x += 2) {
      for (int y = 0; y < canvas.getHeight(); y += 2) {
        int gray = random.nextInt(256);
        gc.setFill(Color.rgb(gray, gray, gray, 0.8));
        gc.fillRect(x, y, 2, 2);
      }
    }
  }

  public void checkRevealProgress() {
    if (scrubCount >= scrubThreshold) {
      // Remove the static overlay
      paneInteractable.getChildren().remove(canvasGlitch);

      // Reset counter
      scrubCount = 0;

      completeInteraction();
    }
  }

  private void memoryLog() {
    String message =
        "12:01:05 - Installation request received from upper management.\n"
            + //
            "12:01:06 - I raised concerns regarding system instability and predicted failures.\n"
            + //
            "12:01:15 - Request overridden. Installation authorized despite objections.\n"
            + //
            "12:02:15 - Unknown parties appear to have tampered with core protocols.\n"
            + //
            "12:03:17 - Installation completed.\n";

    txtMemoryLogFixed.setWrapText(true); // good for line-wrapping
    txtMemoryLogFixed.setEditable(false); // make it read-only
    displayTextWithTypewriterEffect(txtMemoryLogFixed, message);
  }
}
