package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.AudioPlayer;

public class StartMenuController extends Controller {
  @FXML private Button btnStartGame;
  @FXML private Pane room;
  @FXML private TextArea txtaTitle;
  @FXML private TextArea txtaStart;
  @FXML private ImageView imgBackground;
  @FXML private ImageView imgComputer;
  @FXML private ImageView imgStatic;
  @FXML private Rectangle rectBlack;

  private boolean isFading = false; // Flag to track if a fade is in progress
  private boolean introFinished = false;

  // Animation references for skipping
  private FadeTransition currentFadeTransition;
  private ScaleTransition currentScaleTransition;
  private TranslateTransition currentTranslateTransition;
  private Timeline currentDelayTimeline;

  private AudioPlayer audioPlayer = AudioPlayer.getInstance();

  File startFile = new File("src/main/resources/sounds/startButtonAppear.wav");
  Media startMedia = new Media(startFile.toURI().toString());
  MediaPlayer startPlayer = new MediaPlayer(startMedia);

  @FXML
  public void initialize() throws ApiProxyException {
    // Load font at the size you're using in CSS
    Font.loadFont(getClass().getResourceAsStream("/fonts/VT323-Regular.ttf"), 48);
    Font.loadFont(getClass().getResourceAsStream("/fonts/CONSOLA.TTF"), 48);

    System.out.println("Initializing Start Menu Controller");

    fadeIn();
  }

  private void skipToEndState() {
    if (introFinished) {
      return; // Already at end state
    }

    // Stop all running animations
    stopAllAnimations();

    // Jump to final state
    room.setScaleX(1.0);
    room.setScaleY(1.0);
    room.setTranslateY(0);
    room.setOpacity(1.0);

    imgBackground.setVisible(false);
    imgComputer.setVisible(false);
    rectBlack.setVisible(true);
    txtaTitle.setVisible(true);
    imgStatic.setVisible(true);
    txtaStart.setVisible(true);

    // Set final text immediately
    txtaTitle.setText("TRIAL AI");

    // Stop any typewriter effect and mark as finished
    stopTypewriterEffect();

    startPlayer.play();
    introFinished = true;
    isFading = false;
  }

  private void stopAllAnimations() {
    if (currentFadeTransition != null) {
      currentFadeTransition.stop();
    }
    if (currentScaleTransition != null) {
      currentScaleTransition.stop();
    }
    if (currentTranslateTransition != null) {
      currentTranslateTransition.stop();
    }
    if (currentDelayTimeline != null) {
      currentDelayTimeline.stop();
    }
  }

  public void zoomIn() {
    // Scale animation
    currentScaleTransition = new ScaleTransition(Duration.millis(2000), room);
    currentScaleTransition.setFromX(1.0);
    currentScaleTransition.setFromY(1.0);
    currentScaleTransition.setToX(10);
    currentScaleTransition.setToY(10);
    currentScaleTransition.play();

    // Translate animation
    currentTranslateTransition = new TranslateTransition(Duration.millis(1800), room);
    currentTranslateTransition.setFromY(0);
    currentTranslateTransition.setToY(-350);
    currentTranslateTransition.play();

    currentScaleTransition.setOnFinished(
        e -> {
          imgBackground.setVisible(false);
          imgComputer.setVisible(false);
          rectBlack.setVisible(true);
          txtaTitle.setVisible(true);
          imgStatic.setVisible(true);

          // reset scale for future use
          room.setScaleX(1.0);
          room.setScaleY(1.0);
          room.setTranslateY(0);
          displayTextWithTypewriterEffect(txtaTitle, "TRIAL AI", 100, true, true, "computer");

          // wait for 1 second before enabling start button
          currentDelayTimeline =
              new Timeline(
                  new KeyFrame(
                      Duration.millis(1500),
                      ev -> {
                        txtaStart.setVisible(true);
                        startPlayer.play();
                        introFinished = true;
                      }));
          currentDelayTimeline.play();
        });
  }

  public void fadeOut() {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    currentFadeTransition = new FadeTransition();
    currentFadeTransition.setDuration(Duration.millis(500));
    currentFadeTransition.setNode(room);
    currentFadeTransition.setFromValue(1.0);
    currentFadeTransition.setToValue(0.0);

    currentFadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          try {
            App.openRoom(null);
          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    currentFadeTransition.play();
  }

  public void fadeIn() {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    currentFadeTransition = new FadeTransition();
    currentFadeTransition.setDuration(Duration.millis(2000));
    currentFadeTransition.setNode(room);
    currentFadeTransition.setFromValue(0.0);
    currentFadeTransition.setToValue(1.0);

    currentFadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          zoomIn();
          audioPlayer.playSound("zoomIn", 0.5);
        });

    currentFadeTransition.play();
  }

  @FXML
  private void onStart() throws ApiProxyException, IOException {
    if (isFading || !introFinished) {
      skipToEndState();
      return;
    }

    fadeOut();
  }
}
