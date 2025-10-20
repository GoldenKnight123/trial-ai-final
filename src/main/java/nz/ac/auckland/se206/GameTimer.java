package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

/** This class manages a game timer that can be displayed on multiple UI labels. */
public class GameTimer {

  /** Callback interface for when the timer runs out. */
  public interface TimerExpiredCallback {
    void onTimerExpired();
  }

  private static GameTimer instance;

  /**
   * Gets the singleton instance of the GameTimer.
   *
   * @return the GameTimer instance
   */
  public static GameTimer getInstance() {
    if (instance == null) {
      instance = new GameTimer();
    }
    return instance;
  }

  private Timeline timeline;
  private int secondsLeft = 300;
  private int maxTime = 300;
  private boolean isRunning = false;
  // Guard to ensure the expired callback only fires once per timer run
  private boolean expiredFired = false;
  private List<Label> timerLabels = new ArrayList<>();
  private List<Arc> timerArcs = new ArrayList<>();
  private TimerExpiredCallback expiredCallback;

  /** Private constructor for singleton pattern. */
  private GameTimer() {
    initializeTimer();
  }

  /** Initializes the JavaFX Timeline for the timer. */
  private void initializeTimer() {
    timeline =
        new Timeline(
            new KeyFrame(
                Duration.seconds(1),
                e -> {
                  secondsLeft--;
                  updateTimerDisplay();
                  if (secondsLeft <= 0) {
                    stop();
                    // Call the callback when timer expires, but only once per run
                    if (expiredCallback != null && !expiredFired) {
                      expiredFired = true;
                      Platform.runLater(() -> expiredCallback.onTimerExpired());
                    }
                  }
                }));
    timeline.setCycleCount(Timeline.INDEFINITE);
  }

  /** Starts the timer. */
  public void start() {
    if (!isRunning) {
      timeline.play();
      // Allow expiry callback to be fired again for this run
      expiredFired = false;
      isRunning = true;
    }
  }

  /** Stops the timer. */
  public void stop() {
    if (isRunning) {
      timeline.stop();
      isRunning = false;
    }
  }

  /** Get the time left. */
  public int getTimeLeft() {
    return secondsLeft;
  }

  /**
   * Sets the time left on the timer. If seconds is 0 or less this will invoke the same expiry
   * behaviour as the natural countdown (stop timeline, update UI, and invoke the expired callback).
   *
   * @param seconds the number of seconds to set as remaining time
   */
  public void setTimeLeft(int seconds) {
    this.secondsLeft = Math.max(0, seconds);
    // Update UI immediately
    updateTimerDisplay();

    if (this.secondsLeft <= 0) {
      // Stop the timeline if running
      if (timeline != null && isRunning) {
        timeline.stop();
        isRunning = false;
      }

      // Trigger expiry callback on JavaFX thread, but only once per run
      if (expiredCallback != null && !expiredFired) {
        expiredFired = true;
        Platform.runLater(() -> expiredCallback.onTimerExpired());
      }
    }
  }

  /** Get the maximum time. */
  public int setMaxTime(int maxTime) {
    this.maxTime = maxTime;
    secondsLeft = maxTime;
    return maxTime;
  }

  /**
   * Sets the callback to be called when the timer expires.
   *
   * @param callback the callback to execute when timer runs out
   */
  public void setOnTimerExpired(TimerExpiredCallback callback) {
    this.expiredCallback = callback;
  }

  /**
   * Registers a Label to display the timer. Multiple labels can be registered to show the timer in
   * different scenes.
   *
   * @param label the Label to display the timer
   */
  public void registerTimerLabel(Label label) {
    if (!timerLabels.contains(label)) {
      timerLabels.add(label);
      // Update the label immediately with current time (formatted with fixed width)
      Platform.runLater(() -> label.setText(formatTime(secondsLeft)));
    }
  }

  /**
   * Registers a Label to display the timer. Multiple labels can be registered to show the timer in
   * different scenes.
   *
   * @param arc the Arc to display the timer
   */
  public void registerTimerArc(Arc arc) {
    if (!timerArcs.contains(arc)) {
      timerArcs.add(arc);
      // Update the arc immediately with current time
      Platform.runLater(() -> arc.setLength(360.0 * secondsLeft / maxTime));
    }
  }

  /* Updates all registered timer labels within each scene with the current time. */
  public void updateTimerDisplay() {
    // Update all registered labels and arcs on the JavaFX Application Thread
    Platform.runLater(
        () -> {
          // Update each label with formatted time
          for (Label label : timerLabels) {
            label.setText(formatTime(secondsLeft));
          }
          // Update each arc to reflect remaining time
          for (Arc arc : timerArcs) {
            arc.setLength(360.0 * secondsLeft / maxTime);
          }
        });
  }

  /* Resets the timer to the maximum time and updates displays. */
  public void reset() {
    stop();
    maxTime = 300;
    secondsLeft = maxTime;
    updateTimerDisplay();
  }

  private String formatTime(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;
    return String.format("%01d:%02d", minutes, seconds);
  }
}
