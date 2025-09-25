package nz.ac.auckland.se206.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatHistoryManager;

public class AiDefendantController extends ChatController {

  @FXML private Rectangle rectInteractable;
  @FXML private Rectangle rectComputer;
  @FXML private ImageView imgTablet;
  @FXML private Slider sldrProtocol;
  @FXML private Button btnConfirm;
  @FXML private Text txtTablet1;
  @FXML private Text txtTablet2;
  @FXML private Text txtTablet3;
  @FXML private Text txtTablet4;
  @FXML private Pane paneInteractable;
  @FXML private ImageView imgOverride;

  private boolean interactableComplete = false;

  // @FXML private AnchorPane paneInteract;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();
  }

  @FXML
  private void onCompClick(MouseEvent event) {
    System.out.println("Computer Clicked");
    paneInteractable.setVisible(true);
  }

  @FXML
  private void onConfirm(ActionEvent event) {
    System.out.println("Confirm button clicked!");
    double start = sldrProtocol.getValue();
    double end = 100; // move to "Efficiency" side

    // Move slider to max
    if (start < 100) {
      imgOverride.setVisible(true);
      Timeline timeline =
          new Timeline(
              new KeyFrame(Duration.ZERO, new KeyValue(sldrProtocol.valueProperty(), start)),
              new KeyFrame(
                  Duration.seconds(2),
                  new KeyValue(sldrProtocol.valueProperty(), end, Interpolator.EASE_BOTH)));
      timeline.play();
      interactableComplete = true;
      completeInteraction();
    } else if (start == 100) {
      //
    }
  }

  private void completeInteraction() {
    if (interactableComplete) {
      getChatCompletionRequest()
          .addMessage(
              new ChatMessage(
                  "user",
                  "The judge is in the AI defendant's memory where the judge themselves is asked to"
                      + " use a slider, choosing between the Safety Protocol which will hold the"
                      + " curret ppower output or the Efficiency Protocol which will increase the"
                      + " power output. This places them in Indus-07 shoes when it was instructed"
                      + " to deploy the new technology. However, when the user confirms their"
                      + " choice, a message from the upper management that says (Management"
                      + " Override: Proceed to 100% Efficiency) which gives the illusion that the"
                      + " judge had a choice which relates to the fact that INDUS-07 was pressured"
                      + " to implement the new technology"));

      String message =
          "[Management Override: Proceed to 100% Efficiency] \n"
              + "After the call with managament, I was instructed to deploy the new technology.";

      // Add to chat history
      getChatCompletionRequest().addMessage(new ChatMessage("assistant", message));
      ChatHistoryManager.getInstance()
          .addMessage(getTarget(), new ChatMessage("assistant", message));

      // Display to chatbox
      // displayTextWithTypewriterEffect(txtaChat, message);
    }
  }
}
