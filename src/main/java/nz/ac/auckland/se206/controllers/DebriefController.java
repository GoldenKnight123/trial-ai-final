package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URL;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.AudioPlayer;

public class DebriefController extends Controller {
  @FXML private TextArea txtaDebrief;
  @FXML private Label lblCorrect;
  @FXML private Pane debrief;
  @FXML private Pane paneLoading;
  @FXML private Label lblLoading;
  @FXML private Button btnReturn;

  // Tracks whether the decision was correct; set when runGptDebrief is invoked
  private Boolean wasCorrectFlag = null;

  // Whether the fade-in animation has finished
  private boolean fadeCompleted = false;

  // MediaPlayer used to play the result sound; keep a reference so we can stop/dispose
  private MediaPlayer resultPlayer = null;

  // Loading animation timeline
  private Timeline loadingAnimation;

  private ChatCompletionRequest chatCompletionRequest;

  private AudioPlayer audioPlayer = AudioPlayer.getInstance();

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(debrief);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    // When fade-in finishes, attempt to play the result sound if we already know correctness
    fadeTransition.setOnFinished(
        e -> {
          fadeCompleted = true;
          if (wasCorrectFlag != null) {
            playResultSound(wasCorrectFlag);
          }
        });

    fadeTransition.play();
  }

  public void runGptDebrief(String chatHistory, String verdictText, boolean wasCorrect) {
    // store correctness so sound can be played when fade completes
    wasCorrectFlag = wasCorrect;

    // change background colour depending on user verdict
    if (!wasCorrect) {
      // Set incorrect color for incorrect answers
      lblCorrect.setStyle("-fx-text-fill: #FF5757;");

    } else {
      // Set correct color for correct answers
      lblCorrect.setStyle("-fx-text-fill: #00FF00;");
    }

    // Create a background task for GPT analysis
    Task<String> gptTask =
        new Task<String>() {
          @Override
          protected String call() throws Exception {
            // Add the system prompt and player's verdict text for analysis
            String prompt = buildAnalysisPrompt(chatHistory, verdictText, wasCorrect);
            System.out.println(prompt);
            chatCompletionRequest.addMessage(new ChatMessage("system", prompt));

            try {
              ChatCompletionResult chatCompletionResult = chatCompletionRequest.execute();
              Choice result = chatCompletionResult.getChoices().iterator().next();
              chatCompletionRequest.addMessage(result.getChatMessage());

              return result.getChatMessage().getContent();
            } catch (ApiProxyException e) {
              e.printStackTrace();
              return null;
            }
          }
        };

    // Handle success
    gptTask.setOnSucceeded(
        e -> {
          String analysis = gptTask.getValue();
          Platform.runLater(
              () -> {
                paneLoading.setVisible(false);
                audioPlayer.stopSound("scanLong");
                stopLoadingAnimation();
                displayTextWithTypewriterEffect(txtaDebrief, analysis, 10);
              });
        });

    // Handle failure
    gptTask.setOnFailed(
        e -> {
          Platform.runLater(
              () -> {
                setDebriefText("Analysis failed. Please try again.");
              });
        });

    // Start the task in a new thread
    Thread gptThread = new Thread(gptTask);
    gptThread.setDaemon(true);
    gptThread.start();

    // If fade already finished, play the result sound immediately
    if (fadeCompleted) {
      playResultSound(wasCorrectFlag);
    }
  }

  /**
   * Play the result sound (correct/incorrect) located in resources/sounds. Uses JavaFX MediaPlayer
   * and stops any existing player first.
   */
  private void playResultSound(boolean wasCorrect) {
    // Stop previous player if playing
    try {
      if (resultPlayer != null) {
        resultPlayer.stop();
        resultPlayer.dispose();
        resultPlayer = null;
      }

      String fileName = wasCorrect ? "correct.mp3" : "incorrect.mp3";

      // Load from resources (classpath) under /sounds/
      URL resource = App.class.getResource("/sounds/" + fileName);
      if (resource == null) {
        System.err.println("Result sound not found: /sounds/" + fileName);
        return;
      }

      Media media = new Media(resource.toString());
      resultPlayer = new MediaPlayer(media);
      resultPlayer.setVolume(0.8);
      resultPlayer.play();
    } catch (Exception ex) {
      // Non-fatal: log and continue
      ex.printStackTrace();
    }
  }

  /**
   * Creates a prompt to feed into GPT using the chat history and whether or not the user chose the
   * correct verdict between guilty and not guilty. It's designed to use specific quotes and
   * evidence to give detailed feedback
   */
  private String buildAnalysisPrompt(String chatHistory, String verdictText, boolean wasCorrect) {
    StringBuilder prompt = new StringBuilder();

    // Give the model context about its role, the user's input, and the correct rationale
    prompt.append(
        "You are an expert trial analyst. The player has just submitted a verdict and an"
            + " explanation. Your task is to: (1) classify the player's submission into one of"
            + " three outcomes: [Incorrect verdict], [Correct verdict but incorrect rationale], or"
            + " [Correct verdict with correct rationale]; (2) provide a brief (1-2 sentence)"
            + " feedback explaining why the classification was chosen; and (3) provide a short"
            + " explanation (2-4 sentences) describing the correct verdict and the evidence that"
            + " supports it.\n\n");

    // Include the player's verdict and rationale
    prompt.append("Player's submitted verdict and rationale:\n").append(verdictText).append("\n\n");

    // Include the chat history between the player and trial participants for additional context
    prompt
        .append("Chat history between the player and trial participants:\n")
        .append(chatHistory)
        .append("\n\n");

    // Provide structured guidelines for classification and response format
    prompt.append(
        "Guidelines:\n"
            + "- There are three possible outcomes:\n"
            + "    1. [Incorrect verdict]: The player's verdict (guilty/not guilty) does not match"
            + " the ground truth.\n"
            + "    2. [Correct verdict but incorrect rationale]: The verdict matches the ground"
            + " truth, but the rationale either misses key evidence, misattributes causation, or"
            + " directly contradicts the verdict (e.g., verdict is 'not guilty' but rationale says"
            + " 'they are guilty', or vice versa).\n"
            + "    3. [Correct verdict with correct rationale]: Both verdict and rationale align"
            + " with the evidence and do not contradict each other.\n"
            + "- If you detect a contradiction between the verdict and rationale, classify as"
            + " 'Correct verdict but incorrect rationale' and explain the contradiction in your"
            + " feedback.\n"
            + " You can be lenient with minor errors in the rationale, as long as they got the main"
            + " points right it is acceptable.\n"
            + "- Always keep the classification short, then give concise feedback and a clear"
            + " corrective or affirming explanation of the correct verdict.\n"
            + "- Use the following output format:\n"
            + "OUTCOME: <one of the three labels>\n"
            + "FEEDBACK: <1-2 sentences>\n"
            + "EXPLANATION: <2-4 sentences giving the correct verdict and key evidence>\n\n");

    // Provide the correct verdict and rationale for reference
    prompt.append(
        "The correct verdict and rationale should be something along the lines of:\n"
            + " The defendant is not guilty because they were manipulated and pressured by higher"
            + " ups to install new technology which they did not know the risks of.\n\n");

    prompt.append(
        "As long as the user mentions the manipulation and pressure from higher ups, it should be"
            + " considered a valid rationale.\n\n");

    // Specify the desired output format
    prompt.append("Please return the result in this format:\n");
    prompt.append("OUTCOME: <one of the three labels>\n");
    prompt.append("FEEDBACK: <1-2 sentences>\n");
    prompt.append("EXPLANATION: <2-4 sentences giving the correct verdict and key evidence>\n");

    // Include whether the player was correct to help the model (keeps backwards compatibility)
    prompt.append("(Note: the system was told that the decision was: ");
    prompt.append(wasCorrect ? "CORRECT" : "INCORRECT");
    prompt.append(").\n");

    return prompt.toString();
  }

  /** Starts the loading animation for the lblLoading label */
  private void startLoadingAnimation() {
    if (loadingAnimation != null) {
      loadingAnimation.stop();
    }

    loadingAnimation =
        new Timeline(
            new KeyFrame(Duration.seconds(0), e -> lblLoading.setText("Analyzing")),
            new KeyFrame(Duration.seconds(0.5), e -> lblLoading.setText("Analyzing.")),
            new KeyFrame(Duration.seconds(1.0), e -> lblLoading.setText("Analyzing..")),
            new KeyFrame(Duration.seconds(1.5), e -> lblLoading.setText("Analyzing...")),
            new KeyFrame(Duration.seconds(2.0), e -> lblLoading.setText("Analyzing")));

    loadingAnimation.setCycleCount(Animation.INDEFINITE);
    loadingAnimation.play();
  }

  /** Stops the loading animation */
  private void stopLoadingAnimation() {
    if (loadingAnimation != null) {
      loadingAnimation.stop();
      loadingAnimation = null;
    }
  }

  /** Initializes the controller, setting up the chat completion request with desired parameters. */
  @FXML
  public void initialize() {
    paneLoading.setVisible(true);

    startLoadingAnimation();

    audioPlayer.playSound("scanLong", 0.5);

    btnReturn.setOnMouseEntered(
        event -> {
          btnReturn.setText("> Restart");
          audioPlayer.playSound("computerTalk2", 0.5);
        });

    btnReturn.setOnMouseExited(
        event -> {
          btnReturn.setText("Restart");
        });

    // Initialize the chat completion request for GPT analysis
    try {
      ApiProxyConfig config = ApiProxyConfig.readConfig();
      chatCompletionRequest =
          new ChatCompletionRequest(config)
              .setN(1)
              .setTemperature(0.2)
              .setTopP(0.5)
              .setModel(Model.GPT_4o_MINI)
              .setMaxTokens(2000);
    } catch (ApiProxyException e) {
      e.printStackTrace();
    }
  }

  public void setDebriefText(String text) {
    txtaDebrief.setText(text);
  }

  public void setCorrectLabel(String text) {
    lblCorrect.setText(text);
  }

  @FXML
  private void onRestart(ActionEvent event) throws IOException {
    audioPlayer.playSound("computerTalk", 0.5);
    App.reinitialize();
    App.openStartScreen(null);
  }
}
