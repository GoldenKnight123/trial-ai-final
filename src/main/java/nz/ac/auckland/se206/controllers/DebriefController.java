package nz.ac.auckland.se206.controllers;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionRequest.Model;
import nz.ac.auckland.apiproxy.chat.openai.ChatCompletionResult;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.chat.openai.Choice;
import nz.ac.auckland.apiproxy.config.ApiProxyConfig;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class DebriefController extends Controller {
  @FXML private TextArea txtaDebrief;
  @FXML private Label lblCorrect;
  @FXML private Pane debrief;

  private ChatCompletionRequest chatCompletionRequest;

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(debrief);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    fadeTransition.play();
  }

  public void runGptDebrief(String chatHistory, boolean wasCorrect) {
    // Create a background task for GPT analysis
    Task<String> gptTask =
        new Task<String>() {
          @Override
          protected String call() throws Exception {
            // Add the system prompt and player's verdict text for analysis
            chatCompletionRequest.addMessage(
                new ChatMessage(
                    "system",
                    buildAnalysisPrompt(chatHistory.replace("You", "Judge"), wasCorrect)));

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
  }

  /**
   * Creates a prompt to feed into GPT using the chat history and whether or not the user chose the
   * correct verdict between guilty and not guilty. It's designed to use specific quotes and
   * evidence to give detailed feedback
   */
  private String buildAnalysisPrompt(String verdictText, boolean wasCorrect) {
    StringBuilder prompt = new StringBuilder();

    prompt.append(
        "You are an expert trial analyst. The player has just submitted a verdict and an"
            + " explanation. Your task is to: (1) classify the player's submission into one of"
            + " three outcomes: [Incorrect verdict], [Correct verdict but incorrect rationale], or"
            + " [Correct verdict with correct rationale]; (2) provide a brief (1-2 sentence)"
            + " feedback explaining why the classification was chosen; and (3) provide a short"
            + " explanation (2-4 sentences) describing the correct verdict and the evidence that"
            + " supports it.\n\n");

    prompt.append("Player's submitted verdict and rationale:\n").append(verdictText).append("\n\n");

    prompt.append(
        "Guidelines:\n"
            + "- If the player's verdict (guilty/not guilty) matches the ground truth, but their"
            + " reasoning misses key evidence or misattributes causation, classify as 'Correct"
            + " verdict but incorrect rationale'.\n"
            + "- If the verdict itself is wrong, classify as 'Incorrect verdict'.\n"
            + "- If both verdict and reasoning align with the evidence, classify as 'Correct"
            + " verdict with correct rationale'.\n"
            + "- Keep the classification short, then give concise feedback and a clear corrective"
            + " or affirming explanation of the correct verdict.\n\n");

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

  @FXML
  public void initialize() {
    fadeIn();
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
}
