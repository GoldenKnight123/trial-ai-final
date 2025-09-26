package nz.ac.auckland.se206.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectOverlay;
  @FXML private Rectangle rectComputer;
  @FXML private ImageView imgTablet;
  @FXML private Slider sldrProtocol;
  @FXML private Button btnConfirm;
  @FXML private TextArea txtaChat;
  @FXML private Pane paneInteractable;
  @FXML private ImageView imgOverride;

  private boolean interactableComplete = false;

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

  @FXML
  private void onConfirm(ActionEvent event) {
    double start = sldrProtocol.getValue();
    double end = 100; // move to "Efficiency" side

    // Move slider to max
    if (start <= 100) {
      imgOverride.setVisible(true);
      Timeline timeline =
          new Timeline(
              new KeyFrame(Duration.ZERO, new KeyValue(sldrProtocol.valueProperty(), start)),
              new KeyFrame(
                  Duration.seconds(2),
                  new KeyValue(sldrProtocol.valueProperty(), end, Interpolator.EASE_BOTH)));
      timeline.play();
      interactableComplete = true;

      // Mark ineteractable as complete
      ChatHistoryManager.getInstance().markInteractableComplete("INDUS-07");

      // Delay before executing completeInteraction()
      Timeline delayTimeline =
          new Timeline(new KeyFrame(Duration.seconds(3), e -> completeInteraction()));
      delayTimeline.play();
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
}
