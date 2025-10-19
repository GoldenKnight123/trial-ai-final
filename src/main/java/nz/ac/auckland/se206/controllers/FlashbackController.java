package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameTimer;

public class FlashbackController extends Controller {

  // Custom tangent interpolator - fast start, slow end
  private static final Interpolator POWER_INTERPOLATOR =
      new Interpolator() {
        @Override
        protected double curve(double t) {
          // Using a tangent-based curve that starts fast and ends slow
          // This creates a steep curve at the beginning and gradual at the end
          return 1 - Math.pow(1 - t, 3);
        }
      };

  @FXML private AnchorPane flashbackRoom;
  @FXML private StackPane imgScene1;
  @FXML private StackPane imgScene2;
  @FXML private StackPane imgScene3;
  @FXML private ImageView imgView1;
  @FXML private ImageView imgView2;
  @FXML private ImageView imgView3;
  @FXML private Label lblSceneName;
  @FXML private Label lblTimer;
  @FXML private Label lblInstruction;
  @FXML private Arc arcTimer;
  @FXML private ImageView topClouds;
  @FXML private ImageView botClouds;

  private boolean isFading = false; // Flag to track if a fade operation is in progress
  private int slideProgress = 0;
  private boolean canSlide = false;
  private boolean canExit = false;
  private String currentTarget = "";

  public void setTarget(String target) {
    currentTarget = target;

    canExit = false;

    // Dynamically set slide positions
    imgScene1.setTranslateX(0);
    imgScene2.setTranslateX(1280);
    imgScene3.setTranslateX(2560);

    // Set the scene name based on target
    lblSceneName.setText("Flashback: " + target);
    lblInstruction.setText("Click anywhere to continue.");

    setSlideshow(currentTarget);

    // Reset slide progress
    slideProgress = 0;
    lblInstruction.setVisible(false);
    lblInstruction.setLayoutX(916);

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

  public void setSlideshow(String target) {
    // Set the images for the slideshow based on the target
    imgView1.setImage(new Image(getClass().getResourceAsStream(("/images/" + target + "(1).png"))));
    imgView2.setImage(new Image(getClass().getResourceAsStream(("/images/" + target + "(2).png"))));
    imgView3.setImage(new Image(getClass().getResourceAsStream(("/images/" + target + "(3).png"))));
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
            // When flashback ends, open the correlated memory scene for the current target
            // Use App.openChat so the correct memory view (AI/Human/Defendant) is shown
            App.openChat(null, currentTarget);
          } catch (IOException ex) {
            ex.printStackTrace();
            try {
              // Fallback to room if navigation to memory fails
              App.openRoom(event);
            } catch (IOException ex2) {
              ex2.printStackTrace();
            }
          }
        });
    fadeTransition.play();
  }

  @FXML
  public void initialize() throws ApiProxyException {
    // Register timer label with GameTimer
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);

    initializeCloudAnimations();

    String borderStyle = "-fx-border-color: black; -fx-border-width: 6px; -fx-border-style: solid;";
    imgScene1.setStyle(borderStyle);
    imgScene2.setStyle(borderStyle);
    imgScene3.setStyle(borderStyle);
  }

  /**
   * Initializes the cloud animations for the top and bottom cloud layers. Uses two independent
   * Timelines to animate translateX and translateY for a more natural, parallax-like motion
   * (different speeds and amplitudes).
   */
  public void initializeCloudAnimations() {
    // Top clouds: gentle vertical bobbing only
    Timeline topTimeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(topClouds.translateYProperty(), 0, Interpolator.EASE_BOTH)),
            new KeyFrame(
                Duration.seconds(2),
                new KeyValue(topClouds.translateYProperty(), 3, Interpolator.EASE_BOTH)));
    topTimeline.setCycleCount(Timeline.INDEFINITE);
    topTimeline.setAutoReverse(true);
    topTimeline.play();

    // Bottom clouds: slightly faster vertical bobbing
    Timeline botTimeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(botClouds.translateYProperty(), 0, Interpolator.EASE_BOTH)),
            new KeyFrame(
                Duration.seconds(3),
                new KeyValue(botClouds.translateYProperty(), 6, Interpolator.EASE_BOTH)));
    botTimeline.setCycleCount(Timeline.INDEFINITE);
    botTimeline.setAutoReverse(true);
    botTimeline.play();
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

      // Animate all scenes with overshoot effect
      createSlideAnimation(imgScene1);
      createSlideAnimation(imgScene2);
      createSlideAnimation(imgScene3);

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
                      lblInstruction.setText("Click anywhere to go to memory.");
                      lblInstruction.setLayoutX(857);
                      lblInstruction.setVisible(true);
                    }
                  }));
      cooldownTimer.play();
    }
  }

  /**
   * Creates a slide animation with overshoot effect for a given scene. The scene slides left with
   * an overshoot and then snaps back to the target position.
   *
   * @param scene the StackPane to animate
   */
  private void createSlideAnimation(StackPane scene) {
    // Slide with overshoot
    TranslateTransition sceneSlide = new TranslateTransition(Duration.millis(500), scene);
    sceneSlide.setFromX(scene.getTranslateX());
    sceneSlide.setToX(scene.getTranslateX() - 1330); // Overshoot by 50px (1280 + 50)
    sceneSlide.setInterpolator(POWER_INTERPOLATOR); // Add easing

    // Snap back to target
    TranslateTransition sceneSlideBack = new TranslateTransition(Duration.millis(150), scene);
    sceneSlideBack.setFromX(scene.getTranslateX() - 1330);
    sceneSlideBack.setToX(scene.getTranslateX() - 1280); // Final target position
    sceneSlideBack.setInterpolator(Interpolator.EASE_BOTH); // Add easing

    // Chain the animations
    sceneSlide.setOnFinished(e -> sceneSlideBack.play());
    sceneSlide.play();
  }
}
