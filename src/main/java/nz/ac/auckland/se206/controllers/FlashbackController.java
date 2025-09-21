package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameTimer;

public class FlashbackController extends Controller {
  @FXML private AnchorPane flashbackRoom;
  @FXML private ImageView imgScene1;
  @FXML private ImageView imgScene2;
  @FXML private ImageView imgScene3;
  @FXML private Label lblSceneName;
  @FXML private Label lblTimer;
  @FXML private Label lblInstruction;
  @FXML private Arc arcTimer;

  private boolean isFading = false; // Flag to track if a fade operation is in progress
  private int slideProgress = 0;
  private boolean canSlide = false;
  private boolean canExit = false;
  private String currentTarget = "";

  // Custom tangent interpolator - fast start, slow end
  private static final Interpolator TANGENT_INTERPOLATOR =
      new Interpolator() {
        @Override
        protected double curve(double t) {
          // Using a tangent-based curve that starts fast and ends slow
          // This creates a steep curve at the beginning and gradual at the end
          return 1 - Math.pow(1 - t, 3);
        }
      };

  public void setTarget(String target) {
    currentTarget = target;

    canExit = false;

    // Reset image positions
    imgScene1.setTranslateX(0);
    imgScene2.setTranslateX(1280);
    imgScene3.setTranslateX(2560);

    // Set the scene name based on target
    lblSceneName.setText("Flashback - " + target);
    lblInstruction.setText("Click anywhere to continue.");

    // Reset slide progress
    slideProgress = 0;
    lblInstruction.setVisible(false);
    lblInstruction.setLayoutX(1049);

    // Set 500ms cooldown before allowing first slide
    canSlide = false;
    Timeline cooldownTimer =
        new Timeline(
            new KeyFrame(
                Duration.millis(2000),
                e -> {
                  canSlide = true;
                  lblInstruction.setVisible(true);
                }));
    cooldownTimer.play();
  }

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(flashbackRoom);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);
    fadeTransition.play();
  }

  public void fadeOut(ActionEvent event) {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(flashbackRoom);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          try {
            App.openRoom(event);
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    fadeTransition.play();
  }

  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);
  }

  /**
   * Handles mouse clicks anyone on the screen.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleClick(MouseEvent event) throws IOException {
    if (canExit) {
      fadeOut(null);
    }

    if (slideProgress < 2 && canSlide) {
      // Disable sliding until current slide is done
      canSlide = false;
      lblInstruction.setVisible(false);

      // slide image scenes to left with animation
      slideProgress++;

      // Scene 1: slide from 0 to -1000 with overshoot
      TranslateTransition sceneSlide = new TranslateTransition(Duration.millis(500), imgScene1);
      sceneSlide.setFromX(0);
      sceneSlide.setToX(-1330); // Overshoot by 100px
      sceneSlide.setInterpolator(TANGENT_INTERPOLATOR); // Add easing

      TranslateTransition sceneSlideBack = new TranslateTransition(Duration.millis(150), imgScene1);
      sceneSlideBack.setFromX(-1330);
      sceneSlideBack.setToX(-1280); // Snap back to target
      sceneSlideBack.setInterpolator(Interpolator.EASE_BOTH); // Add easing

      sceneSlide.setOnFinished(e -> sceneSlideBack.play());
      sceneSlide.play();

      // Scene 2: slide from 1280 to 0 with overshoot
      TranslateTransition sceneSlide2 = new TranslateTransition(Duration.millis(500), imgScene2);
      sceneSlide2.setFromX(1280);
      sceneSlide2.setToX(-50); // Overshoot by 100px
      sceneSlide2.setInterpolator(TANGENT_INTERPOLATOR); // Add easing

      TranslateTransition sceneSlideBack2 =
          new TranslateTransition(Duration.millis(150), imgScene2);
      sceneSlideBack2.setFromX(-50);
      sceneSlideBack2.setToX(0); // Snap back to target
      sceneSlideBack2.setInterpolator(Interpolator.EASE_BOTH); // Add easing

      sceneSlide2.setOnFinished(e -> sceneSlideBack2.play());
      sceneSlide2.play();

      // Scene 3: slide from 2560 to 1000 with overshoot
      TranslateTransition sceneSlide3 = new TranslateTransition(Duration.millis(500), imgScene3);
      sceneSlide3.setFromX(2560);
      sceneSlide3.setToX(1210); // Overshoot by 100px
      sceneSlide3.setInterpolator(TANGENT_INTERPOLATOR); // Add easing

      TranslateTransition sceneSlideBack3 =
          new TranslateTransition(Duration.millis(150), imgScene3);
      sceneSlideBack3.setFromX(1210);
      sceneSlideBack3.setToX(1280); // Snap back to target
      sceneSlideBack3.setInterpolator(Interpolator.EASE_BOTH); // Add easing

      sceneSlide3.setOnFinished(e -> sceneSlideBack3.play());
      sceneSlide3.play();

      // Restrict sliding to once every 500ms
      Timeline cooldownTimer =
          new Timeline(
              new KeyFrame(
                  Duration.millis(2000),
                  e -> {
                    if (slideProgress < 2) {
                      canSlide = true;
                      lblInstruction.setVisible(true);
                    } else {
                      canExit = true;
                      lblInstruction.setText("Click anywhere to return to courtroom.");
                      lblInstruction.setLayoutX(964);
                      lblInstruction.setVisible(true);
                    }
                  }));
      cooldownTimer.play();
    }
  }
}
