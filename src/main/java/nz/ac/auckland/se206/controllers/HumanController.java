package nz.ac.auckland.se206.controllers;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.ChatHistoryManager;

public class HumanController extends ChatController {

  @FXML private Slider slider;
  @FXML private Text tempTxt;
  @FXML private Text pressureTxt;
  @FXML private Text flowTxt;
  @FXML private Text stressTxt;

  @FXML private Canvas sinGraphCanvas;
  @FXML private ImageView errorImage;

  private GraphicsContext gc;

  private AnimationTimer waveTimer;

  private double amplitude = 20;
  private double frequency = 0.05;
  private double x = 0;

  private boolean waveStarted = false;

  // Track whether we've notified the AI that the tablet is unscrambled
  private boolean tabletUnscrambledNotified = false;

  // Lock timer and state: if slider stays in sweet spot for this duration, lock it
  private PauseTransition lockTimer;
  private boolean sliderLocked = false;

  // Real values
  private final String tempValue = "512";
  private final String pressureValue = "2870";
  private final String flowValue = "15.3";
  private final String stressValue = "92.4";

  // Slider sweet spot
  private final double sweetSpot = 50;
  private final double tolerance = 2;

  @FXML
  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();
    gc = sinGraphCanvas.getGraphicsContext2D();

    // Initialize lock timer (1 second)
    lockTimer = new PauseTransition(Duration.millis(1000));
    lockTimer.setOnFinished(
        e -> {
          if (!sliderLocked) {
            slider.setDisable(true);
            sliderLocked = true;

            // Notify AI that the tablet is unscrambled (only when locked)
            if (!tabletUnscrambledNotified && getChatCompletionRequest() != null) {
              ChatMessage sysMsg =
                  new ChatMessage(
                      "system", "The tablet interface is now unscrambled and readable.");
              displayInteractionDialogue();
              if (getTarget() != null) {
                ChatHistoryManager.getInstance().addMessage(getTarget(), sysMsg);
              }
              getChatCompletionRequest().addMessage(sysMsg);
              tabletUnscrambledNotified = true;
            }
          }
        });

    // AnimationTimer for sine wave
    waveTimer =
        new AnimationTimer() {
          @Override
          public void handle(long now) {
            double width = sinGraphCanvas.getWidth();
            double height = sinGraphCanvas.getHeight();

            gc.setStroke(javafx.scene.paint.Color.LIME);
            gc.setLineWidth(2);

            double y = height / 2 + amplitude * Math.sin(x * frequency);

            if (x > 0) {
              double prevY = height / 2 + amplitude * Math.sin((x - 1) * frequency);
              gc.strokeLine(x - 1, prevY, x, y);
            }

            x += 1;

            if (x >= width) {
              stop(); // Stop animation when the wave finishes
              waveStarted = false;
            }
          }
        };

    slider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double distance = Math.abs(newVal.doubleValue() - sweetSpot);

              if (distance <= tolerance) {
                // Fully reveal values
                errorImage.setVisible(true);
                tempTxt.setText(tempValue);
                pressureTxt.setText(pressureValue);
                flowTxt.setText(flowValue);
                stressTxt.setText(stressValue);
                if (!waveStarted) {
                  resetWave();
                  waveTimer.start();
                  waveStarted = true;
                }
                // Start lock timer: only lock the slider and notify the AI if the slider
                // remains in the sweet spot for 1 second.
                if (!sliderLocked) {
                  lockTimer.playFromStart();
                }
              } else {
                // Gradually scramble values
                double scrambleFactor = Math.min(distance / (slider.getMax() / 2), 1.0);
                tempTxt.setText(scramble(tempValue, scrambleFactor));
                pressureTxt.setText(scramble(pressureValue, scrambleFactor));
                flowTxt.setText(scramble(flowValue, scrambleFactor));
                stressTxt.setText(scramble(stressValue, scrambleFactor));
                errorImage.setVisible(false);
                if (waveStarted) {
                  resetWave();
                  waveTimer.stop();
                  waveStarted = false;
                }
                // Cancel lock timer and unlock slider if they move away before it fires
                lockTimer.stop();
                if (sliderLocked) {
                  slider.setDisable(false);
                  sliderLocked = false;
                }
                // Reset notification flag so we can notify again when the user returns to
                // the sweet spot later
                tabletUnscrambledNotified = false;
              }
            });
  }

  private void resetWave() {
    gc.clearRect(0, 0, sinGraphCanvas.getWidth(), sinGraphCanvas.getHeight());
    x = 0;
  }

  private String scramble(String input, double scrambleFactor) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isWhitespace(c)
          || Character.isDigit(c)
          || c == '.'
          || c == '°'
          || c == '/'
          || c == 'k'
          || c == 'P'
          || c == 'a'
          || c == 'm') {
        if (Math.random() < scrambleFactor) {
          sb.append(randomChar());
        } else {
          sb.append(c);
        }
      } else {
        sb.append(randomChar());
      }
    }
    return sb.toString();
  }

  private char randomChar() {
    String symbols = "#$%&*@?";
    return symbols.charAt((int) (Math.random() * symbols.length()));
  }
}
