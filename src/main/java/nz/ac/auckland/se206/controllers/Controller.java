package nz.ac.auckland.se206.controllers;

import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.util.Duration;
import nz.ac.auckland.se206.AudioPlayer;

/**
 * Abstract base class for all controllers. Provides common functionality including typewriter
 * effects for text display.
 */
public abstract class Controller {

  private Timeline typewriterTimeline; // Timeline for typewriter effect
  private boolean isTyping = false;
  private String currentFullText = ""; // Store the full text being displayed
  private TextArea currentTextArea = null; // Store the current TextArea being used
  private Random random = new Random();
  private AudioPlayer audioPlayer;

  public Controller() {
    // Get the universal audio player instance
    audioPlayer = AudioPlayer.getInstance();
  }

  /**
   * Creates a typewriter effect for displaying text in a TextArea.
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   * @param clearFirst whether to clear the TextArea before starting the effect
   * @param addSound whether to play the user talk sound effect
   * @param speaker the current speaker (for sound effect control)
   */
  protected void displayTextWithTypewriterEffect(
      TextArea textArea,
      String textToDisplay,
      double delayPerCharacter,
      boolean clearFirst,
      boolean addSound,
      String speaker) {
    // Stop any existing typewriter animation
    if (typewriterTimeline != null) {
      typewriterTimeline.stop();
    }

    // Store current typewriter state
    currentTextArea = textArea;
    currentFullText = textToDisplay;

    // Clear or preserve existing text
    if (clearFirst) {
      textArea.clear();
    }

    isTyping = true;

    String existingText = textArea.getText();

    // Create the typewriter animation
    typewriterTimeline = new Timeline();

    for (int i = 0; i <= textToDisplay.length(); i++) {
      final int charIndex = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(charIndex * delayPerCharacter),
              e -> {
                if (clearFirst) {
                  textArea.setText(textToDisplay.substring(0, charIndex));
                } else {
                  textArea.setText(existingText + textToDisplay.substring(0, charIndex));
                }

                // Auto-scroll to bottom after each character is added
                textArea.setScrollTop(Double.MAX_VALUE);

                // Play sound for non-space characters
                if (addSound && charIndex > 0 && charIndex <= textToDisplay.length()) {
                  char currentChar = textToDisplay.charAt(charIndex - 1);
                  if (!Character.isWhitespace(currentChar)) {
                    playTalkingSound(speaker);
                  }
                }

                // Check if this is the last character being displayed
                if (charIndex == textToDisplay.length()) {
                  isTyping = false;
                  onTypewriterEffectFinish();
                }
              });
      typewriterTimeline.getKeyFrames().add(keyFrame);
    }

    typewriterTimeline.play();
  }

  /**
   * Plays the appropriate talking sound for the given speaker.
   *
   * @param speaker the current speaker
   */
  private void playTalkingSound(String speaker) {
    // Randomize the playback rate slightly for variation
    float randomRate = random.nextFloat() * 0.2f + 0.9f;
    audioPlayer.setRate("playerTalk", randomRate);
    double volume = 0.5;

    // Play sound based on speaker
    switch (speaker) {
      case "user":
        audioPlayer.playSound("playerTalk", volume);
        break;
      case "LOGOS-09":
        audioPlayer.playSound("logos09Talk", volume);
        break;
      case "INDUS-07":
        audioPlayer.playSound("indus07Talk", volume);
        break;
      case "Evan":
        audioPlayer.playSound("evanTalk", volume);
        break;
      case "computer":
        audioPlayer.playSound("computerTalk", volume);
        break;
      case "computer2":
        audioPlayer.playSound("computerTalk2", volume);
        break;
      default:
        // No sound for empty or unrecognized speaker
        break;
    }
  }

  /**
   * Creates a typewriter effect for displaying text in a TextArea with default speed (50ms per
   * character).
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   */
  protected void displayTextWithTypewriterEffect(TextArea textArea, String textToDisplay) {
    displayTextWithTypewriterEffect(textArea, textToDisplay, 50, true, false, "");
  }

  /**
   * Appends text to a TextArea with typewriter effect.
   *
   * @param textArea the TextArea to append text to
   * @param textToAppend the text to append with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   */
  protected void appendTextWithTypewriterEffect(
      TextArea textArea, String textToAppend, double delayPerCharacter) {
    displayTextWithTypewriterEffect(textArea, textToAppend, delayPerCharacter, false, false, "");
  }

  /**
   * Appends text to a TextArea with typewriter effect using default speed (30ms per character).
   *
   * @param textArea the TextArea to append text to
   * @param textToAppend the text to append with typewriter effect
   */
  protected void appendTextWithTypewriterEffect(TextArea textArea, String textToAppend) {
    appendTextWithTypewriterEffect(textArea, textToAppend, 30);
  }

  /** Stops any currently running typewriter effect. */
  protected void stopTypewriterEffect() {
    if (typewriterTimeline != null) {
      typewriterTimeline.stop();
    }
  }

  /** Instantly finishes the current typewriter effect by displaying the full text. */
  protected void finishTypewriterEffectInstantly() {
    if (isTyping && currentTextArea != null && currentFullText != null) {
      // Stop the current animation
      if (typewriterTimeline != null) {
        typewriterTimeline.stop();
      }

      // Set the full text immediately
      currentTextArea.setText(currentFullText);

      // Auto-scroll to bottom when finishing instantly
      currentTextArea.setScrollTop(Double.MAX_VALUE);

      isTyping = false;
      onTypewriterEffectFinish();
    }
  }

  /** Called when the typewriter effect finishes. */
  protected void onTypewriterEffectFinish() {
    // Override this method in subclasses to handle the end of the typewriter effect
  }

  public boolean isTyping() {
    return isTyping;
  }
}
