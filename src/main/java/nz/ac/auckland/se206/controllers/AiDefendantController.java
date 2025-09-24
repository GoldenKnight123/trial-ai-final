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
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

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
    } else if (start == 100) {
      //
    }
  }
}
