package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameStateContext;
import nz.ac.auckland.se206.GameTimer;

/**
 * Controller class for the room view. Handles user interactions within the room where the user can
 * chat with customers and guess their profession.
 */
public class RoomController extends Controller {

  private static boolean isFirstTimeInit = true;
  private static List<String> fixedDialogue = new ArrayList<>();
  private static int currentDialogueIndex = 0;
  private static GameStateContext context = new GameStateContext();

  /**
   * Initializes introductory dialogue for the trial. Each line is a separate item in the list. It
   * will be displayed one by one and is handled by the displayNextLine method
   */
  private static void initializeFixedDialogue() {
    // Introduction - Introducing defendant - Explaining the incident
    fixedDialogue.add(
        "Members of the jury - human and artificial. We shall now commence the trial of"
            + " INDUS-07.\n\n"
            + "The defendant is an industrial supervision AI, which the prosecution claims failed"
            + " to prevent the Greenhill Power Plant disaster.");

    // Explaining the incident - Incident consequences
    fixedDialogue.add(
        "On the morning of June 17th 2027, an explosion occurred at the Greenhill Power Plant."
            + " Fortunately, no one was killed in the incident. But two human workers who were on"
            + " duty at the time suffered injuries. Furthermore, the site suffered severe damage to"
            + " its infrastructure and technology systems.");

    // What the defendant is accused of in regards to the incident
    fixedDialogue.add(
        "INDUS-07, the defendant, is accused of negligence, as it decided to increase the power"
            + " plant's output level to unsafe levels, causing the explosion.");

    // Introducing witnesses - Introducing LOGOS-09 - Introducing Evan
    fixedDialogue.add(
        "We have two witnesses with us today.\n\n"
            + "LOGOS-09, the AI responsible for managing the plant system message and operation"
            + " logs. And Evan, one of the human workers who were present at the time of the"
            + " incident.");

    // Explaining how we will analyse the memories
    fixedDialogue.add(
        "Using the Omni-View-02, I shall analyse the memories of the witnesses and defendant to"
            + " come to a conclusion of whether or not the defendant is guilty or innocent.");
  }

  @FXML private ImageView imgWitnessAi;
  @FXML private ImageView imgDefendant;
  @FXML private ImageView imgWitnessHuman;
  @FXML private Rectangle rectFade;
  @FXML private TextArea txtaDialogue;
  @FXML private ImageView imgContinueArrow;
  @FXML private ImageView imgTextArea;
  @FXML private ImageView imgInstructionBox;
  @FXML private Label lblWhoSpeaking;
  @FXML private Label lblInstructions;
  @FXML private Label lblTimer;
  @FXML private Button btnGuilty;
  @FXML private Button btnNotGuilty;
  @FXML private Arc arcTimer;
  @FXML private Pane room;

  private boolean isFading = false;
  private boolean isGuessing = false;

  private HashMap<String, Boolean> flashbackMap =
      new HashMap<>() {
        {
          put("LOGOS-09", true);
          put("Evan", false);
          put("INDUS-07", false);
        }
      };

  public void initializeGuessingState() {
    txtaDialogue.setVisible(true);
    imgTextArea.setVisible(true);
    imgInstructionBox.setVisible(false);
    lblWhoSpeaking.setVisible(true);
    imgContinueArrow.setVisible(false);
    lblInstructions.setVisible(false);
    lblTimer.setVisible(true);
    arcTimer.setVisible(true);
    GameTimer.getInstance().setMaxTime(100);
    GameTimer.getInstance().updateTimerDisplay();
    GameTimer.getInstance().start();
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              try {
                onChooseGuilty(null);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    fixedDialogue.clear();
    fixedDialogue.add("I have finished analysing the memories of the witness and defendants.");
    fixedDialogue.add("I shall now decide if defendant is GUILTY or NOT GUILTY.");
    currentDialogueIndex = 0;
    displayTextWithTypewriterEffect(
        txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
    currentDialogueIndex++;
    isGuessing = true;
  }

  public void fadeIn() {
    System.out.println("Fading in room");
    isFading = true; // Set flag to prevent interactions during fade

    // Ensure the room itself is fully visible first
    room.setOpacity(1.0);

    rectFade.setVisible(true);
    rectFade.setOpacity(1.0); // Ensure it starts fully opaque

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(rectFade);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          rectFade.setVisible(false); // Hide the fade rectangle when done
          if (GameTimer.getInstance().getTimeLeft() <= 0) {
            context.setState(context.getGuessingState());
          }
        });

    fadeTransition.play();
  }

  public void fadeOut(MouseEvent event, String target) {
    // Prevent starting a new fade if one is already in progress
    if (isFading) {
      return;
    }

    isFading = true; // Set flag to prevent further fade operations

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          try {
            if (flashbackMap.get(target)) {
              // If the flashback for this target has already been viewed, send them to their memory
              App.openChat(event, target);
              return;
            } else {
              // If not, mark it as viewed and open the flashback
              flashbackMap.put(target, true);
              App.openFlashback(event, target);
              return;
            }

          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    fadeTransition.play();
  }

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    context.setRoomController(this); // Set reference to this controller

    lblTimer.setVisible(true);
    lblTimer.setAlignment(Pos.CENTER);
    GameTimer.getInstance().start();

    // Setup hover effects of images
    Image logos09 = imgWitnessAi.getImage();
    Image logos09Hover = new Image("images/logos-09-hover.png");

    imgWitnessAi.setOnMouseEntered(
        event -> {
          if (currentDialogueIndex >= fixedDialogue.size()) {
            imgWitnessAi.setImage(logos09Hover);
          }
        });

    imgWitnessAi.setOnMouseExited(
        event -> {
          imgWitnessAi.setImage(logos09);
        });

    Image indus07 = imgDefendant.getImage();
    Image indus07Hover = new Image("images/indus-07-hover.png");

    imgDefendant.setOnMouseEntered(
        event -> {
          if (currentDialogueIndex >= fixedDialogue.size()) {
            imgDefendant.setImage(indus07Hover);
          }
        });

    imgDefendant.setOnMouseExited(
        event -> {
          imgDefendant.setImage(indus07);
        });

    Image evan = imgWitnessHuman.getImage();
    Image evanHover = new Image("images/evan-hover.png");

    imgWitnessHuman.setOnMouseEntered(
        event -> {
          if (currentDialogueIndex >= fixedDialogue.size()) {
            imgWitnessHuman.setImage(evanHover);
          }
        });

    imgWitnessHuman.setOnMouseExited(
        event -> {
          imgWitnessHuman.setImage(evan);
        });

    txtaDialogue.setVisible(true);
    txtaDialogue.setEditable(false);
    txtaDialogue.setFocusTraversable(false);
    txtaDialogue.setMouseTransparent(true);

    imgTextArea.setVisible(true);
    lblWhoSpeaking.setVisible(true);

    btnGuilty.setVisible(false);
    btnNotGuilty.setVisible(false);

    // Register timer label and start timer if this is the first initialization
    GameTimer.getInstance().registerTimerLabel(lblTimer);
    GameTimer.getInstance().registerTimerArc(arcTimer);

    if (isFirstTimeInit) {
      imgContinueArrow.setVisible(false);
      lblInstructions.setVisible(false);
      imgInstructionBox.setVisible(false);
      initializeFixedDialogue();
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
      currentDialogueIndex++;
      isFirstTimeInit = false;
    }
  }

  /**
   * Handles mouse clicks on rectangles representing people in the room.
   *
   * @param event the mouse event triggered by clicking a rectangle
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleRectangleClick(MouseEvent event) throws IOException {
    // Prevent rectangle clicks if fade animation is playing
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    // If fixed dialogue is finished and the user clicks on a rectangle, take them to the chat scene
    // with the clicked individual as the flashback focus
    if (currentDialogueIndex >= fixedDialogue.size()) {
      context.handleRectangleClick(event, ((Rectangle) event.getSource()).getId());
    }
  }

  @FXML
  public void handleWitnessAiClick(MouseEvent event) throws IOException {
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    if (currentDialogueIndex < fixedDialogue.size()) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    // Take the user to the chat scene with the clicked individual as the flashback focus
    RoomController roomController = context.getRoomController();
    roomController.fadeOut(event, "LOGOS-09");
  }

  @FXML
  public void handleDefendantClick(MouseEvent event) throws IOException {
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    if (currentDialogueIndex < fixedDialogue.size()) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    // Take the user to the chat scene with the clicked individual as the flashback focus
    RoomController roomController = context.getRoomController();
    roomController.fadeOut(event, "INDUS-07");
  }

  @FXML
  public void handleWitnessHumanClick(MouseEvent event) throws IOException {
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    if (currentDialogueIndex < fixedDialogue.size()) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    // Take the user to the chat scene with the clicked individual as the flashback focus
    RoomController roomController = context.getRoomController();
    roomController.fadeOut(event, "Evan");
  }

  /**
   * Handles the guess button click event.
   *
   * @param event the action event triggered by clicking the guess button
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGuessClick(ActionEvent event) throws IOException {
    context.handleGuessClick();
  }

  /**
   * Handles mouse clicks anywhere in the game area.
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void handleGeneralClick(MouseEvent event) throws IOException {
    context.handleGeneralClick();
  }

  /**
   * Handles mouse click on GUILTY button
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onChooseGuilty(ActionEvent event) throws IOException {
    GameTimer.getInstance().stop();
    App.openDebrief(null, false);
  }

  /**
   * Handles mouse click on GUILTY button
   *
   * @param event the mouse event triggered by clicking in the game area
   * @throws IOException if there is an I/O error
   */
  @FXML
  private void onChooseNotGuilty(ActionEvent event) throws IOException {
    GameTimer.getInstance().stop();
    App.openDebrief(null, true);
  }

  @Override
  protected void onTypewriterEffectFinish() {
    // Show the continue label when the typewriter effect finishes
    imgContinueArrow.setVisible(true);
  }

  /**
   * Display the next line of fixed dialogue, if all fixed dialogue has been consumed then it will
   * trigger the different changes to the scene based on which state the user is currently in.
   */
  public void displayNextLine() {
    if (currentDialogueIndex < fixedDialogue.size()) {
      imgContinueArrow.setVisible(false);
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true);
      currentDialogueIndex++;
    } else if (!isGuessing) {
      imgContinueArrow.setVisible(false);
      txtaDialogue.setVisible(false);
      imgTextArea.setVisible(false);
      lblWhoSpeaking.setVisible(false);
      lblInstructions.setVisible(true);
      imgInstructionBox.setVisible(true);
      context.setState(context.getGameStartedState());
    } else {
      imgContinueArrow.setVisible(false);
      txtaDialogue.setVisible(false);
      imgTextArea.setVisible(false);
      lblWhoSpeaking.setVisible(false);
    }
  }

  /** Finishes the current typewriter effect instantly. */
  public void finishTypingInstantly() {
    finishTypewriterEffectInstantly();
  }

  public Pane getRoom() {
    return room;
  }

  public GameStateContext getContext() {
    return context;
  }

  public Boolean isGuessing() {
    return isGuessing;
  }
}
