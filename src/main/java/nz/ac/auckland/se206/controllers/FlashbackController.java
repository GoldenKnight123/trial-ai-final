package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Arc;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.AudioPlayer;
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
  @FXML private ImageView imgScene1;
  @FXML private ImageView imgScene2;
  @FXML private ImageView imgScene3;
  @FXML private Label lblSceneName;
  @FXML private Label lblWhoSpeaking;
  @FXML private Label lblTimer;
  @FXML private ImageView imgContinueArrow;
  @FXML private Arc arcTimer;
  @FXML private TextArea txtaChat; // Add this FXML field for your dialogue text area

  private boolean isFading = false; // Flag to track if a fade operation is in progress
  private int slideProgress = 0;
  private int dialogueIndex = 0; // Track current dialogue line
  private boolean canSlide = false;
  private boolean canExit = false;
  private boolean isTypewriterActive = false; // Track if typewriter effect is running
  private String currentTarget = "";
  private ArrayList<String> currentDialogue; // Store current dialogue lines

  private HashMap<String, ArrayList<String>> flashbackDialogue = new HashMap<>();

  // Initialize flashback dialogue data
  public void initializeFlashbackDialogue() {
    // Initialize dialogue for LOGOS-09
    // Each character has an array of dialogue lines for the flashback
    // LOGOS-09 sees the call and notices suspicious activity
    flashbackDialogue.put(
        "LOGOS-09",
        new ArrayList<>(
            Arrays.asList(
                "It looks like the system has picked up a call between upper management and"
                    + " INDUS-07.",
                "Following that call I noticed some abnormal activities and commands being executed"
                    + " by INDUS-07",
                "These actions are quite suspicious, because the call is encrypted I shall flag it"
                    + " for further investigation.")));

    // Initialize dialogue for INDUS-07
    // INDUS-07 receives the call and sends instructions to Evan to install the module
    flashbackDialogue.put(
        "INDUS-07",
        new ArrayList<>(
            Arrays.asList(
                "I got a call from upper management regarding some important matters.",
                "I was instructed to prepare for the installation of a new module in the power"
                    + " plant.",
                "I was concerned about the safety implications, but upper management assured me"
                    + " that there would be no issues.")));

    // Initialize dialogue for Evan
    // Evan installs the module as per INDUS-07's instructions
    flashbackDialogue.put(
        "Evan",
        new ArrayList<>(
            Arrays.asList(
                "It was a quiet day at work until I got an urgent work order from my manager to"
                    + " install a new module in the reactor.",
                "I swiftly prepared the necessary equipment and headed to the power plant to carry"
                    + " out the installation.",
                "This was a new module I never worked with before, but I simply followed the"
                    + " instructions given to me by my manager.")));
  }

  public void setTarget(String target) {
    currentTarget = target;

    canExit = false;
    dialogueIndex = 0; // Reset dialogue index
    isTypewriterActive = false; // Reset typewriter state

    // Get dialogue for current target
    currentDialogue = flashbackDialogue.get(target);
    if (currentDialogue == null) {
      currentDialogue = new ArrayList<>(Arrays.asList("No dialogue available for this character."));
    }

    // Dynamically set slide positions
    imgScene1.setTranslateX(0);
    imgScene2.setTranslateX(1280);
    imgScene3.setTranslateX(2560);

    // Set the scene name based on target
    lblSceneName.setText("Flashback: " + target);
    lblWhoSpeaking.setText(target + ":");

    setSlideshow(currentTarget);

    // Reset slide progress
    slideProgress = 0;
    imgContinueArrow.setVisible(false);

    // Clear text area and display first dialogue line with typewriter effect
    txtaChat.clear();
    displayCurrentDialogue();

    // Don't set canSlide to true until typewriter effect is done
    canSlide = false;
  }

  /** Displays the current dialogue line in the text area with typewriter effect. */
  private void displayCurrentDialogue() {
    if (currentDialogue != null && dialogueIndex < currentDialogue.size()) {
      // Clear the text area first
      txtaChat.clear();
      isTypewriterActive = true;
      canSlide = false;
      imgContinueArrow.setVisible(false);

      // Use the typewriter effect from parent Controller class
      displayTextWithTypewriterEffect(
          txtaChat,
          currentDialogue.get(dialogueIndex),
          50,
          true,
          true,
          currentTarget); // 50ms delay between characters
    }
  }

  /** Called when typewriter effect completes. */
  private void onTypewriterComplete() {
    isTypewriterActive = false;
    canSlide = true;
    imgContinueArrow.setVisible(true);
  }

  @Override
  protected void onTypewriterEffectFinish() {
    // This method is called by the parent Controller when typewriter effect finishes
    onTypewriterComplete();
  }

  /** Finishes the typewriter effect immediately. */
  private void finishTypewriterEffect() {
    if (isTypewriterActive) {
      // Use the parent class method to finish typewriter effect instantly
      finishTypewriterEffectInstantly();

      // Mark typewriter as complete
      onTypewriterComplete();
    }
  }

  /** Advances to the next dialogue line and corresponding image. */
  private void advanceDialogue() {
    if (dialogueIndex < currentDialogue.size() - 1) {
      dialogueIndex++;
      displayCurrentDialogue();

      // Advance image if we haven't reached the last slide
      if (slideProgress < 2) {
        advanceSlideshow();
      }
    } else {
      // All dialogue finished, allow exit immediately
      canExit = true;
      // Exit immediately instead of waiting for another click
      fadeOut(null);
    }
  }

  /** Advances the slideshow to the next image. */
  private void advanceSlideshow() {
    if (slideProgress < 2) {
      // Disable sliding until current slide is done
      canSlide = false;
      imgContinueArrow.setVisible(false);

      // slide image scenes to left with animation
      slideProgress++;

      // Animate all scenes with overshoot effect
      createSlideAnimation(imgScene1);
      createSlideAnimation(imgScene2);
      createSlideAnimation(imgScene3);

      // Don't automatically re-enable sliding - wait for typewriter to complete
    }
  }

  public void setSlideshow(String target) {
    // Set the images for the slideshow based on the target
    imgScene1.setImage(
        new Image(getClass().getResourceAsStream(("/images/" + target + "(1).png"))));
    imgScene2.setImage(
        new Image(getClass().getResourceAsStream(("/images/" + target + "(2).png"))));
    imgScene3.setImage(
        new Image(getClass().getResourceAsStream(("/images/" + target + "(3).png"))));
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

    // Transition back to background music when leaving flashback
    AudioPlayer.getInstance().transitionToBackgroundMusic();

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

    initializeFlashbackDialogue(); // Initialize dialogue data

    String borderStyle = "-fx-border-color: black; -fx-border-width: 6px; -fx-border-style: solid;";
    imgScene1.setStyle(borderStyle);
    imgScene2.setStyle(borderStyle);
    imgScene3.setStyle(borderStyle);

    // Style the dialogue text area
    if (txtaChat != null) {
      txtaChat.setEditable(false);
      txtaChat.setWrapText(true);
    }
  }

  /**
   * Handles mouse clicks anywhere on the screen.
   *
   * @param event the mouse event triggered by clicking
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleClick(MouseEvent event) throws IOException {
    if (canExit) {
      fadeOut(null);
      return;
    }

    // If typewriter effect is active, finish it immediately
    if (isTypewriterActive) {
      finishTypewriterEffect();
      return;
    }

    // If typewriter is complete and we can slide, advance to next dialogue
    if (canSlide) {
      advanceDialogue();
    }
  }

  /**
   * Creates a slide animation with overshoot effect for a given scene. The scene slides left with
   * an overshoot and then snaps back to the target position.
   *
   * @param scene the ImageView to animate
   */
  private void createSlideAnimation(ImageView scene) {
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
