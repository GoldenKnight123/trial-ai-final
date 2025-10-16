package nz.ac.auckland.se206.controllers;

import java.io.File;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

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

  File playerTalkFile = new File("src/main/resources/sounds/playerTalk.wav");
  Media playerTalkMedia = new Media(playerTalkFile.toURI().toString());
  MediaPlayer playerTalk = new MediaPlayer(playerTalkMedia);

  File logos09File = new File("src/main/resources/sounds/logos09Talk.wav");
  Media logos09Media = new Media(logos09File.toURI().toString());
  MediaPlayer logos09Talk = new MediaPlayer(logos09Media);

  File indus07File = new File("src/main/resources/sounds/indus07Talk.wav");
  Media indus07Media = new Media(indus07File.toURI().toString());
  MediaPlayer indus07Talk = new MediaPlayer(indus07Media);

  File evanTalkFile = new File("src/main/resources/sounds/evanTalk.wav");
  Media evanTalkMedia = new Media(evanTalkFile.toURI().toString());
  MediaPlayer evanTalk = new MediaPlayer(evanTalkMedia);

  File computerTalkFile = new File("src/main/resources/sounds/computerTalk.wav");
  Media computerTalkMedia = new Media(computerTalkFile.toURI().toString());
  MediaPlayer computerTalk = new MediaPlayer(computerTalkMedia);

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
                    switch (speaker) {
                      case "user":
                        playerTalk.stop();
                        playerTalk.setVolume(0.5);
                        playerTalk.setRate(random.nextFloat() * 0.1f + 0.95f);
                        playerTalk.play();
                        break;
                      case "LOGOS-09":
                        logos09Talk.stop();
                        logos09Talk.setVolume(0.5);
                        logos09Talk.setRate(random.nextFloat() * 0.2f + 0.9f);
                        logos09Talk.play();
                        break;
                      case "INDUS-07":
                        indus07Talk.stop();
                        indus07Talk.setVolume(0.5);
                        indus07Talk.setRate(random.nextFloat() * 0.2f + 0.9f);
                        indus07Talk.play();
                        break;
                      case "Evan":
                        evanTalk.stop();
                        evanTalk.setVolume(0.5);
                        evanTalk.setRate(random.nextFloat() * 0.1f + 0.95f);
                        evanTalk.play();
                        break;
                      case "computer":
                        computerTalk.stop();
                        computerTalk.setVolume(0.5);
                        computerTalk.play();
                        break;
                      default:
                        // No sound for empty or unrecognized speaker
                        break;
                    }
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
   * Creates a typewriter effect for displaying text in a TextArea, clearing it first.
   *
   * @param textArea the TextArea to display text in
   * @param textToDisplay the text to display with typewriter effect
   * @param delayPerCharacter delay between each character in milliseconds
   */
  protected void displayTextWithTypewriterEffect(
      TextArea textArea, String textToDisplay, double delayPerCharacter) {
    displayTextWithTypewriterEffect(textArea, textToDisplay, delayPerCharacter, true, false, "");
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
