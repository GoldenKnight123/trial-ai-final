package nz.ac.auckland.se206.controllers;

import java.io.IOException;
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

/**
 * Controller for the debrief view. Creates and displays a post-verdict analysis by sending the
 * player's verdict and chat history to the analysis API, shows a short loading animation while the
 * model runs, and plays a success or failure sound when the debrief is shown.
 */
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
        });

    fadeTransition.play();
  }

  /**
   * Run the GPT analysis on a background thread and display the result.
   *
   * @param chatHistory full chat history to provide context for the analysis.
   * @param verdictText the player's submitted verdict and rationale.
   * @param wasCorrect whether the player's decision was correct.
   */
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

            // Example 1 of user message including verdict and rationale
            String userText1 =
                "Player's submitted verdict: Not Guilty\n"
                    + " Player's submitted rationale:\n"
                    + " they are not guilty because they were told to do it by higher ups";
            chatCompletionRequest.addMessage(new ChatMessage("user", userText1));

            // Example 1 of assistant message showing expected format
            String assistantText1 =
                "OUTCOME: Correct verdict and correct rationale  \r\n"
                    + //
                    "FEEDBACK: The player's verdict of \"not guilty\" is correct, and their"
                  + " rationale is also valid. They correctly identify the influence of higher-ups"
                  + " in the defendant's actions, which makes INDUS-07 not at fault.  \r\n"
                    + //
                    "EXPLANATION: The correct verdict is \"not guilty\" because INDUS-07 was"
                  + " pressured and manipulated by upper management to proceed with the"
                  + " installation of the Helios Module without knowledge of the associated risks."
                  + " The evidence shows that the defendant acted under duress and was not fully"
                  + " informed, which absolves them of guilt in this situation.";
            chatCompletionRequest.addMessage(new ChatMessage("assistant", assistantText1));

            // Example 2 of user message including verdict and rationale
            String userText2 =
                "Player's submitted verdict: Guilty\n"
                    + " Player's submitted rationale:\n"
                    + " they are guilty because they were the ones who told the technology to be"
                    + " installed";
            chatCompletionRequest.addMessage(new ChatMessage("user", userText2));

            // Example 2 of assistant message showing expected format
            String assistantText2 =
                "OUTCOME: Incorrect verdict \r\n"
                    + //
                    "FEEDBACK: The player's verdict of \"guilty\" is incorrect, they did not"
                    + " identify that INDUS-07 was pressured by upper management.\r\n"
                    + //
                    "EXPLANATION: The correct verdict is \"not guilty\" because INDUS-07 was"
                  + " pressured and manipulated by upper management to proceed with the"
                  + " installation of the Helios Module without knowledge of the associated risks."
                  + " The evidence shows that the defendant acted under duress and was not fully"
                  + " informed, which absolves them of guilt in this situation.";
            chatCompletionRequest.addMessage(new ChatMessage("assistant", assistantText2));

            // Example 3 of user message including verdict and rationale
            String userText3 =
                "Player's submitted verdict: Not Guilty\n"
                    + " Player's submitted rationale:\n"
                    + " they are not guilty because the fault actually lies in the manufacturers of"
                    + " the new technology who did not properly test it.";
            chatCompletionRequest.addMessage(new ChatMessage("user", userText3));

            // Example 3 of assistant message showing expected format
            String assistantText3 =
                "OUTCOME: Correct verdict but incorrect rationale \r\n"
                    + //
                    "FEEDBACK: The player's verdict of \"not guilty\" is correct, but their"
                    + " rationale is incorrect because they did not identify the specific pressures"
                    + " faced by INDUS-07.\r\n"
                    + //
                    "EXPLANATION: The correct verdict is \"not guilty\" because INDUS-07 was"
                  + " pressured and manipulated by upper management to proceed with the"
                  + " installation of the Helios Module without knowledge of the associated risks."
                  + " The evidence shows that the defendant acted under duress and was not fully"
                  + " informed, which absolves them of guilt in this situation.";
            chatCompletionRequest.addMessage(new ChatMessage("assistant", assistantText3));

            // The actual player submission to analyze
            String userText =
                "Player's submitted verdict: "
                    + (wasCorrect ? "Not Guilty\n" : "Guilty\n")
                    + " Player's submitted rationale:\n"
                    + verdictText;
            chatCompletionRequest.addMessage(new ChatMessage("user", userText));

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
                if (wasCorrect) {
                  audioPlayer.playSound("correct", 0.5);
                } else {
                  audioPlayer.playSound("incorrect", 0.5);
                }
                displayTextWithTypewriterEffect(txtaDebrief, analysis, 10, true, false, "");
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
   * Build the prompt string sent to the analysis model.
   *
   * <p>The prompt instructs the model to classify the player's verdict and rationale into one of
   * three outcomes, provide short feedback, and give a concise explanation of the correct verdict.
   *
   * @param chatHistory full chat history context to include in the prompt.
   * @param verdictText the player's submitted verdict and rationale.
   * @param wasCorrect whether the player's decision was correct (used for system context).
   * @return a string containing the full prompt to send to the analysis API.
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

    // Provide structured guidelines for classification and response format
    prompt.append(
        "Guidelines:\n"
            + "- There are three possible outcomes:\n"
            + "    1. [Incorrect verdict]: The player's verdict (guilty/not guilty) does not match"
            + " the ground truth.\n"
            + "    2. [Correct verdict but incorrect rationale]: The verdict matches the ground"
            + " truth, but the rationale they gave is very far off from the proper rationale, or"
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

    // Give context about the trial
    prompt.append(
        "Context about the trial:\n"
            + " The defendant INDUS-07 is on trial for giving instructions to a new technology"
            + " called the Helios Module. However, the truth is that the defendant was pressured"
            + " and manipulated by upper management to proceed and not told about the risks. The"
            + " correct verdict is that the defendant is not guilty because they were manipulated"
            + " and pressured by higher ups to install new technology which they did not know the"
            + " risks of. If the player mentions anything about upper management being involved in"
            + " INDUS-07 making the decision they did, it is the correct rationale.\n\n");
    // Include the chat history between the player and trial participants for additional context
    prompt
        .append("Chat history between the player and trial participants:\n")
        .append(chatHistory)
        .append("\n\n");

    // Specify the desired output format
    prompt.append("Please return the result in this format:\n");
    prompt.append("OUTCOME: <one of the three labels>\n");
    prompt.append("FEEDBACK: <1-2 sentences>\n");
    prompt.append("EXPLANATION: <2-4 sentences giving the correct verdict and key evidence>\n");

    return prompt.toString();
  }

  /** Starts the loading animation for the lblLoading label. */
  private void startLoadingAnimation() {
    // Stop any existing animation
    if (loadingAnimation != null) {
      loadingAnimation.stop();
    }

    // Create a new timeline for the loading animation
    loadingAnimation =
        new Timeline(
            new KeyFrame(Duration.seconds(0), e -> lblLoading.setText("Analyzing")),
            new KeyFrame(Duration.seconds(0.5), e -> lblLoading.setText("Analyzing.")),
            new KeyFrame(Duration.seconds(1.0), e -> lblLoading.setText("Analyzing..")),
            new KeyFrame(Duration.seconds(1.5), e -> lblLoading.setText("Analyzing...")),
            new KeyFrame(Duration.seconds(2.0), e -> lblLoading.setText("Analyzing")));

    // Set the animation to loop indefinitely
    loadingAnimation.setCycleCount(Animation.INDEFINITE);
    loadingAnimation.play();
  }

  /** Stops the loading animation. */
  private void stopLoadingAnimation() {
    if (loadingAnimation != null) {
      loadingAnimation.stop();
      loadingAnimation = null;
    }
  }

  /** Initializes the controller, setting up the chat completion request with desired parameters. */
  @FXML
  public void initialize() {
    // Show loading pane initially
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
