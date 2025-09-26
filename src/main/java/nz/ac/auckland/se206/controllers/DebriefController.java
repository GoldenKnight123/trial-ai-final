package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
import nz.ac.auckland.se206.App;

public class DebriefController extends Controller {
  @FXML private TextArea txtaDebrief;
  @FXML private Label lblCorrect;
  @FXML private Pane debrief;
  @FXML private Pane paneBackground;

  private ChatCompletionRequest chatCompletionRequest;

  public void fadeIn() {
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(debrief);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    fadeTransition.play();
  }

  public void runGptDebrief(String chatHistory, String verdictText, boolean wasCorrect) {

    // change background colour depending on user verdict
    if (!wasCorrect) {
      paneBackground.setStyle("-fx-background-color: #FF5757;"); // red incorrect
    }
    // Create a background task for GPT analysis
    Task<String> gptTask =
        new Task<String>() {
          @Override
          protected String call() throws Exception {
            // Add the system prompt and player's verdict text for analysis
            chatCompletionRequest.addMessage(
                new ChatMessage(
                    "system",
                    buildAnalysisPrompt(
                        chatHistory.replace("You", "Judge"), verdictText, wasCorrect)));

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
  private String buildAnalysisPrompt(String chatHistory, String verdictText, boolean wasCorrect) {
    StringBuilder prompt = new StringBuilder();

    System.out.println(chatHistory);
    System.out.println(verdictText);
    System.out.println(wasCorrect);

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

    // Provide the correct rationale for the case
    prompt.append(
        "The correct rationale is that the defendant is innocent, because they were manipulated by"
            + " higher ups to install the new Helios system into the power plant, but they did not"
            + " know the risks. But ultimately it is not their fault.\n\n");

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
            + "- Always keep the classification short, then give concise feedback and a clear"
            + " corrective or affirming explanation of the correct verdict.\n"
            + "- Use the following output format:\n"
            + "OUTCOME: <one of the three labels>\n"
            + "FEEDBACK: <1-2 sentences>\n"
            + "EXPLANATION: <2-4 sentences giving the correct verdict and key evidence>\n\n");

    // Example for contradiction handling
    prompt.append(
        "Example:\n"
            + "Player's verdict: not guilty\n"
            + "Player's rationale: 'They are guilty because they installed the system.'\n"
            + "Correct outcome: [Correct verdict but incorrect rationale]\n"
            + "Feedback: The verdict is correct, but your reasoning contradicts it. You stated they"
            + " are guilty, which does not support your verdict.\n"
            + "Explanation: The defendant is innocent because they were manipulated by higher ups"
            + " and did not know the risks.\n\n");

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

  /** Initializes the controller, setting up the chat completion request with desired parameters. */
  @FXML
  public void initialize() {
    // Start with debrief pane invisible for fade-in effect
    fadeIn();

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
    App.openStartScreen(null);
    App.reinitialize();
  }
}
