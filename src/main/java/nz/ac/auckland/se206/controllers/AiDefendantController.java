package nz.ac.auckland.se206.controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

  // @FXML private AnchorPane paneInteract;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();
  }

  @FXML
  private void onCompClick(MouseEvent event) {
    // paneInteract.setVisible(true);
    rectComputer.setVisible(false);
    rectInteractable.setVisible(true);
    txtTablet1.setVisible(true);
    txtTablet2.setVisible(true);
    txtTablet3.setVisible(true);
    txtTablet4.setVisible(true);
    imgTablet.setVisible(true);
    btnConfirm.setVisible(true);
    sldrProtocol.setVisible(true);
  }

  @FXML
  protected void onConfirm() {
    double start = sldrProtocol.getValue();
    double end = 100; // move to "Efficiency" side

    Timeline timeline =
        new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(sldrProtocol.valueProperty(), start)),
            new KeyFrame(
                Duration.seconds(2),
                new KeyValue(sldrProtocol.valueProperty(), end, Interpolator.EASE_BOTH)));
    timeline.play();
  }
}
