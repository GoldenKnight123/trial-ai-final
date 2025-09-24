package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class AiWitnessController extends ChatController {

  @FXML ImageView imgInteractable;
  @FXML ImageView imgOverlay;
  @FXML Rectangle rectOverlay;

  private boolean interactableState = false;

  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Create a highlight effect for hover
    ColorAdjust highlight = new ColorAdjust();
    highlight.setBrightness(0.3);

    // Add hover effects to the interactable image
    imgInteractable.setOnMouseEntered(
        event -> {
          if (currentSpeaker == "") {
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
    if (currentSpeaker == "") {
      rectOverlay.setVisible(true);
      imgOverlay.setVisible(true);
      interactableState = true;
    }
  }

  @FXML
  public void onCloseInteraction(MouseEvent event) {
    // Closes the overlay when clicked
    rectOverlay.setVisible(false);
    imgOverlay.setVisible(false);
    interactableState = false;
  }
}
