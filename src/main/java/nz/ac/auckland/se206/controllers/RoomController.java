package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.AudioPlayer;
import nz.ac.auckland.se206.ChatHistoryManager;
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
  private static boolean canInteract = false;
  private static GameStateContext context = new GameStateContext();

  /**
   * Initializes introductory dialogue for the trial. Each line is a separate item in the list. It
   * will be displayed one by one and is handled by the displayNextLine method
   */
  private static void initializeFixedDialogue() {
    fixedDialogue.clear();
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

  @FXML private ImageView imgWitnessAiBody;
  @FXML private ImageView imgWitnessAiStand;
  @FXML private ImageView imgDefendantBody;
  @FXML private ImageView imgDefendantStand;
  @FXML private ImageView imgWitnessHumanBody;
  @FXML private ImageView imgWitnessHumanStand;
  @FXML private Rectangle rectWitnessAi;
  @FXML private Rectangle rectDefendant;
  @FXML private Rectangle rectWitnessHuman;
  @FXML private TextArea txtaDialogue;
  @FXML private ImageView imgContinueArrow;
  @FXML private ImageView imgTextArea;
  @FXML private ImageView imgInstructionBox;
  @FXML private ImageView imgInteractable1;
  @FXML private ImageView imgInteractable2;
  @FXML private ImageView imgInteractable3;
  @FXML private ImageView imgMute;
  @FXML private Label lblWhoSpeaking;
  @FXML private Label lblInstructions;
  @FXML private Label lblTimer;
  @FXML private Label lblSpokenTo1;
  @FXML private Label lblSpokenTo2;
  @FXML private Label lblSpokenTo3;
  @FXML private Button btnGuilty;
  @FXML private Button btnNotGuilty;
  @FXML private Button verdictBtn;
  @FXML private Arc arcTimer;
  @FXML private Pane room;

  // Rain effect variables
  private Timeline rainTimeline;
  private Random random = new Random();
  private List<Line> rainDrops = new ArrayList<>();

  // Idle animation timelines
  private Timeline aiIdleTimeline;
  private Timeline defendantIdleTimeline;
  private Timeline humanIdleTimeline;

  private boolean isFading = false;
  private boolean isGuessing = false;

  private AudioPlayer audioPlayer = AudioPlayer.getInstance();

  private HashMap<String, Boolean> flashbackMap =
      new HashMap<>() {
        {
          put("LOGOS-09", false);
          put("Evan", false);
          put("INDUS-07", false);
        }
      };

  // Function to set variables and states for the guessing state
  public void initializeGuessingState() {
    // Disable interactions during guessing state setup
    canInteract = false;

    // Show text area to display dialogue
    txtaDialogue.setVisible(true);
    imgTextArea.setVisible(true);
    imgInstructionBox.setVisible(false);
    lblWhoSpeaking.setVisible(true);

    // Hide continue arrow and show timer
    imgContinueArrow.setVisible(false);
    lblInstructions.setVisible(false);

    // Show timer and start countdown
    lblTimer.setVisible(true);
    arcTimer.setVisible(true);

    // Set max time to 60 seconds and start timer
    GameTimer.getInstance().setMaxTime(60);
    GameTimer.getInstance().updateTimerDisplay();
    GameTimer.getInstance().start();

    // Set up timer expired callback to send to game over screen
    GameTimer.getInstance()
        .setOnTimerExpired(
            () -> {
              // Safeguard: hide the Skip to verdict button so it cannot be used after expiry
              if (verdictBtn != null) {
                verdictBtn.setVisible(false);
              }
              try {
                App.openGameOver(null, "Failed to make a verdict in time.");
              } catch (IOException e) {
                e.printStackTrace();
              }
            });

    // Initialize fixed dialogue for guessing state
    fixedDialogue.clear();

    // If all interactables are complete and the player has spoken to each character, allow them to
    // make a guess, otherwise send them to game over
    if (ChatHistoryManager.getInstance().haveSpokenToAll()
        && ChatHistoryManager.getInstance().interactablesComplete()) {
      fixedDialogue.add("I have finished analysing the memories of the witness and defendants.");
      fixedDialogue.add("I shall now decide if defendant is GUILTY or NOT GUILTY.");
    } else {
      fixedDialogue.add("I have not been able to gather enough evidence.");
      fixedDialogue.add(
          "I am not able to make a decision on whether the defendant is guilty or not.");
    }

    // Reset dialogue index and display first line
    currentDialogueIndex = 0;
    displayTextWithTypewriterEffect(
        txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true, "user");
    currentDialogueIndex++;
    isGuessing = true;
  }

  /**
   * Forces a check of mouse hover states for all interactive elements. This is useful after fade
   * transitions or dialogue completion.
   */
  private void forceHoverStateCheck() {
    // Check if mouse is currently over any of the interactive images
    if (rectWitnessAi.isHover() && canInteract && !isFading) {
      // Trigger hover effect for LOGOS-09
      ColorAdjust highlight = new ColorAdjust();
      highlight.setBrightness(0.3);
      imgWitnessAiBody.setEffect(highlight);
      imgWitnessAiBody.setImage(new Image("images/logos-09-hover.png"));
      rectWitnessAi.setCursor(Cursor.HAND);
      audioPlayer.playSound("hover");
    }

    if (rectDefendant.isHover() && canInteract && !isFading) {
      // Trigger hover effect for INDUS-07
      ColorAdjust highlight = new ColorAdjust();
      highlight.setBrightness(0.3);
      imgDefendantBody.setEffect(highlight);
      imgDefendantBody.setImage(new Image("images/indus-07-hover.png"));
      rectDefendant.setCursor(Cursor.HAND);
      audioPlayer.playSound("hover");
    }

    if (rectWitnessHuman.isHover() && canInteract && !isFading) {
      // Trigger hover effect for Evan
      ColorAdjust highlight = new ColorAdjust();
      highlight.setBrightness(0.3);
      imgWitnessHumanBody.setEffect(highlight);
      imgWitnessHumanBody.setImage(new Image("images/evan-hover.png"));
      rectWitnessHuman.setCursor(Cursor.HAND);
      audioPlayer.playSound("hover");
    }
  }

  public void fadeIn() {
    System.out.println("Fading in room");
    isFading = true; // Set flag to prevent interactions during fade

    // Ensure the room itself is fully visible first
    room.setOpacity(1.0);

    // Restart rain effect if it's not running
    if (rainTimeline == null || rainTimeline.getStatus() != Animation.Status.RUNNING) {
      createRainEffect();
    }

    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(0.0);
    fadeTransition.setToValue(1.0);

    // Check if interactables are complete and set state accordingly
    if (ChatHistoryManager.getInstance().isInteractableComplete("LOGOS-09")) {
      imgInteractable1.setOpacity(1);
    }
    if (ChatHistoryManager.getInstance().isInteractableComplete("INDUS-07")) {
      imgInteractable2.setOpacity(1);
    }
    if (ChatHistoryManager.getInstance().isInteractableComplete("Evan")) {
      imgInteractable3.setOpacity(1);
    }

    // Check if the player has spoken to each character and show "Spoken To" labels
    if (ChatHistoryManager.getInstance().hasBeenSpokenTo("LOGOS-09")) {
      lblSpokenTo1.setVisible(true);
    }
    if (ChatHistoryManager.getInstance().hasBeenSpokenTo("INDUS-07")) {
      lblSpokenTo2.setVisible(true);
    }
    if (ChatHistoryManager.getInstance().hasBeenSpokenTo("Evan")) {
      lblSpokenTo3.setVisible(true);
    }
    fadeTransition.setOnFinished(
        e -> {
          isFading = false; // Reset flag when fade completes
          forceHoverStateCheck();
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

    // Create fade transition for the room
    FadeTransition fadeTransition = new FadeTransition();
    fadeTransition.setDuration(Duration.millis(1000));
    fadeTransition.setNode(room);
    fadeTransition.setFromValue(1.0);
    fadeTransition.setToValue(0.0);

    // Create zoom transition for the entire room to simulate diving into the mind
    ScaleTransition zoomTransition = new ScaleTransition();
    zoomTransition.setDuration(Duration.millis(800));
    zoomTransition.setNode(room);
    zoomTransition.setFromX(1.0);
    zoomTransition.setFromY(1.0);
    zoomTransition.setToX(3);
    zoomTransition.setToY(3);

    // Create translation transition to move towards the clicked individual
    TranslateTransition translateTransition = new TranslateTransition();
    translateTransition.setDuration(Duration.millis(800));
    translateTransition.setNode(room);
    translateTransition.setFromX(0);
    translateTransition.setFromY(0);

    // Set translation based on which character was clicked
    switch (target) {
      case "LOGOS-09": // AI Witness (left side)
        translateTransition.setToX(1000);
        translateTransition.setToY(200);
        break;
      case "INDUS-07": // Defendant (center)
        translateTransition.setToX(-50);
        translateTransition.setToY(300);
        break;
      case "Evan": // Human Witness (right side)
        translateTransition.setToX(-1200);
        translateTransition.setToY(200);
        break;
    }

    // Combine fade, zoom, and translation animations
    ParallelTransition parallelTransition =
        new ParallelTransition(fadeTransition, zoomTransition, translateTransition);

    parallelTransition.setOnFinished(
        e -> {
          // Reset the scale and translation of the room after animation
          room.setScaleX(1.0);
          room.setScaleY(1.0);
          room.setTranslateX(0);
          room.setTranslateY(0);

          isFading = false; // Reset flag when fade completes
          try {
            if (flashbackMap.get(target)) {
              // If the flashback for this target has already been viewed, send them to their memory
              App.openChat(event, target);
            } else {
              // If not, mark it as viewed and open the flashback
              flashbackMap.put(target, true);
              // Transition to flashback music when going to flashback
              AudioPlayer.getInstance().transitionToFlashbackMusic();
              App.openFlashback(event, target);
            }

          } catch (IOException ex) {
            ex.printStackTrace();
          }
        });
    parallelTransition.play();
  }

  /**
   * Initializes the room view. If it's the first time initialization, it will provide instructions
   * via text-to-speech.
   */
  @FXML
  public void initialize() {
    context.setRoomController(this); // Set reference to this controller

    createIdleAnimation(aiIdleTimeline, imgWitnessAiBody);
    createIdleAnimation(defendantIdleTimeline, imgDefendantBody);
    createIdleAnimation(humanIdleTimeline, imgWitnessHumanBody);

    isGuessing = false;

    lblTimer.setVisible(true);
    arcTimer.setVisible(true);
    lblTimer.setAlignment(Pos.CENTER);

    // Create a highlight effect for hover
    ColorAdjust highlight = new ColorAdjust();
    highlight.setBrightness(0.3);

    // Setup hover effects of images
    Image logos09 = imgWitnessAiBody.getImage();
    Image logos09Hover = new Image("images/logos-09-hover.png");

    rectWitnessAi.setOnMouseEntered(
        event -> {
          if (canInteract && !isFading) {
            imgWitnessAiBody.setEffect(highlight);
            imgWitnessAiBody.setImage(logos09Hover);
            rectWitnessAi.setCursor(Cursor.HAND);
            audioPlayer.playSound("hover");
          }
        });

    rectWitnessAi.setOnMouseExited(
        event -> {
          imgWitnessAiBody.setEffect(null);
          imgWitnessAiBody.setImage(logos09);
          rectWitnessAi.setCursor(Cursor.DEFAULT);
        });

    Image indus07 = imgDefendantBody.getImage();
    Image indus07Hover = new Image("images/indus-07-hover.png");

    rectDefendant.setOnMouseEntered(
        event -> {
          if (canInteract && !isFading) {
            imgDefendantBody.setEffect(highlight);
            imgDefendantBody.setImage(indus07Hover);
            rectDefendant.setCursor(Cursor.HAND);
            audioPlayer.playSound("hover");
          }
        });

    rectDefendant.setOnMouseExited(
        event -> {
          imgDefendantBody.setEffect(null);
          imgDefendantBody.setImage(indus07);
          rectDefendant.setCursor(Cursor.DEFAULT);
        });

    Image evan = imgWitnessHumanBody.getImage();
    Image evanHover = new Image("images/evan-hover.png");

    rectWitnessHuman.setOnMouseEntered(
        event -> {
          if (canInteract && !isFading) {
            imgWitnessHumanBody.setEffect(highlight);
            imgWitnessHumanBody.setImage(evanHover);
            rectWitnessHuman.setCursor(Cursor.HAND);
            audioPlayer.playSound("hover");
          }
        });

    rectWitnessHuman.setOnMouseExited(
        event -> {
          imgWitnessHumanBody.setEffect(null);
          imgWitnessHumanBody.setImage(evan);
          rectWitnessHuman.setCursor(Cursor.DEFAULT);
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
      System.out.println("Initializing RoomController");

      // Start rain effect
      createRainEffect();

      GameTimer.getInstance().start();
      // Set up timer expired callback to force return to room
      GameTimer.getInstance()
          .setOnTimerExpired(
              () -> {
                // Safeguard: hide the Skip to verdict button when the timer expiry sequence runs
                if (verdictBtn != null) {
                  verdictBtn.setVisible(false);
                }
                audioPlayer.playSound("timesUp", 0.5);
                App.forceReturnToRoom();
                audioPlayer.stopSound("background");
              });

      // Play background music
      audioPlayer.startBackgroundMusic();

      // Reset flashback map
      flashbackMap.put("LOGOS-09", false);
      flashbackMap.put("Evan", false);
      flashbackMap.put("INDUS-07", false);

      imgContinueArrow.setVisible(false);
      lblInstructions.setVisible(false);
      imgInstructionBox.setVisible(false);
      currentDialogueIndex = 0;
      initializeFixedDialogue();
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true, "user");
      currentDialogueIndex++;
      isFirstTimeInit = false;
    }
  }

  @FXML
  public void handleWitnessAiClick(MouseEvent event) throws IOException {
    if (isFading) {
      System.out.println("Cannot click rectangle: Fade animation is in progress");
      return;
    }

    if (!canInteract) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    audioPlayer.playSound("buttonClick", 0.5);
    audioPlayer.playSound("zoomIn", 0.5);

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

    if (!canInteract) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    audioPlayer.playSound("buttonClick", 0.5);
    audioPlayer.playSound("zoomIn", 0.5);

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

    if (!canInteract) {
      System.out.println("Cannot click rectangle: Fixed dialogue is still playing");
      return;
    }

    if (GameTimer.getInstance().getTimeLeft() <= 2) {
      return;
    }

    audioPlayer.playSound("buttonClick", 0.5);
    audioPlayer.playSound("zoomIn", 0.5);

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
    // Display next line of fixed dialogue if available
    if (currentDialogueIndex < fixedDialogue.size()) {
      imgContinueArrow.setVisible(false);
      displayTextWithTypewriterEffect(
          txtaDialogue, fixedDialogue.get(currentDialogueIndex), 50, true, true, "user");
      currentDialogueIndex++;
    }

    // If all fixed dialogue has been consumed and the user is not yet guessing transition to game
    // started state to allow interaction with individuals in the courtroom
    else if (!isGuessing) {
      imgContinueArrow.setVisible(false);
      txtaDialogue.setVisible(false);
      imgTextArea.setVisible(false);
      lblWhoSpeaking.setVisible(false);
      lblInstructions.setVisible(true);
      imgInstructionBox.setVisible(true);
      canInteract = true;
      forceHoverStateCheck();
      context.setState(context.getGameStartedState());
    }

    // If all fixed dialogue has been consumed and the user is guessing, transition to verdict scene
    // depending on whether all witnesses have been spoken to and interactions have been completed
    else {
      if (isFading) {
        return;
      }

      imgContinueArrow.setVisible(false);
      txtaDialogue.setVisible(false);
      imgTextArea.setVisible(false);
      lblWhoSpeaking.setVisible(false);
      // Fade out the room and then open the verdict scene. We always open the verdict scene;
      // the boolean below selects whether the verdict view should show the "game over" variant
      // (used when the player hasn't spoken to all required participants).
      boolean showGameOverVariant =
          (!ChatHistoryManager.getInstance().haveSpokenToAll()
              || !ChatHistoryManager.getInstance().interactablesComplete());
      FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), room);
      fadeTransition.setFromValue(1.0);
      fadeTransition.setToValue(0.0);
      // Prevent interactions during fade
      isFading = true;
      fadeTransition.setOnFinished(
          e -> {
            try {

              if (showGameOverVariant) {
                isFading = true;
                App.openGameOver(null, "Failed to interact with all participants.");
              } else {
                isFading = true;
                App.openVerdict(null);
              }
            } catch (IOException ioEx) {
              ioEx.printStackTrace();
            }
            // Reset fading flag (this controller may be cached but should not accept input while
            // faded)
            isFading = false;
          });
      fadeTransition.play();
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

  public void setFirstTimeInit(Boolean value) {
    isFirstTimeInit = value;
  }

  @FXML
  private void onMute(MouseEvent event) throws ApiProxyException, IOException {
    // Toggle mute state
    boolean muted = audioPlayer.toggleBackgroundMuted();
    // Update mute icon
    if (muted) {
      imgMute.setImage(new Image(getClass().getResourceAsStream("/images/muteOn.png")));
    } else {
      imgMute.setImage(new Image(getClass().getResourceAsStream("/images/muteOff.png")));
    }
    audioPlayer.playSound("buttonClick", 0.5);
  }

  /**
   * Handler for the "Skip to verdict" button. Sets the global timer to 0 so the expiry callback
   * runs the normal verdict flow.
   */
  @FXML
  private void onSkipToVerdict() {
    GameTimer.getInstance().setTimeLeft(0);
  }

  /** Creates and starts the rain animation effect. */
  private void createRainEffect() {
    // Clear any existing rain drops
    rainDrops.clear();

    // Create initial rain drops
    for (int i = 0; i < 100; i++) {
      Line rainDrop = createRainDrop();
      rainDrops.add(rainDrop);
      room.getChildren().add(0, rainDrop); // Add to back of scene
    }

    // Create timeline for rain animation - faster update rate
    rainTimeline = new Timeline(new KeyFrame(Duration.millis(30), e -> updateRain()));
    rainTimeline.setCycleCount(Animation.INDEFINITE);
    rainTimeline.play();
  }

  /** Creates a single rain drop line. */
  private Line createRainDrop() {
    Line rainDrop = new Line();

    // Random position across the width of the room
    double x = random.nextDouble() * room.getWidth();
    double y = -random.nextDouble() * 800; // Start above the visible area

    rainDrop.setStartX(x);
    rainDrop.setStartY(y);
    rainDrop.setEndX(x + 2); // Slight angle for more realistic look
    rainDrop.setEndY(y + 15 + random.nextDouble() * 10); // Variable length

    // Set rain drop appearance - black color with transparency
    rainDrop.setStroke(Color.rgb(0, 0, 0, 0.7)); // Black with transparency
    rainDrop.setStrokeWidth(0.5);

    return rainDrop;
  }

  /** Updates all rain drops positions and recycles them when they fall off screen. */
  private void updateRain() {
    for (Line rainDrop : rainDrops) {
      // Move rain drop downwards
      double speed = 12 + random.nextDouble() * 8;
      rainDrop.setStartY(rainDrop.getStartY() + speed);
      rainDrop.setEndY(rainDrop.getEndY() + speed);

      // If rain drop has fallen off screen, reset it to the top
      if (rainDrop.getStartY() > room.getHeight()) {
        double x = random.nextDouble() * room.getWidth();
        rainDrop.setStartX(x);
        rainDrop.setStartY(-random.nextDouble() * 200);
        rainDrop.setEndX(x + 2);
        rainDrop.setEndY(rainDrop.getStartY() + 15 + random.nextDouble() * 10);

        // Vary the opacity slightly for more natural look - black color
        rainDrop.setStroke(Color.rgb(0, 0, 0, 0.5 + random.nextDouble() * 0.4));
      }
    }
  }

  /** Creates idle animation timeline for the specified character. */
  private void createIdleAnimation(Timeline idleTimeline, ImageView character) {
    // Create a timeline that triggers animation changes every few seconds
    idleTimeline =
        new Timeline(
            new KeyFrame(Duration.seconds(0), e -> startIdleMovement(character)),
            new KeyFrame(Duration.seconds(8), e -> startIdleMovement(character)),
            new KeyFrame(Duration.seconds(16), e -> startIdleMovement(character)));

    idleTimeline.setCycleCount(Animation.INDEFINITE);
    idleTimeline.play();
  }

  /** Creates a subtle movement animation for the specified character. */
  private void startIdleMovement(ImageView character) {
    // Random subtle movements - slightly different for each character
    double translateX = -5 + Math.random() * 10; // Random value between -5 and 5
    double translateY = -3 + Math.random() * 6; // Random value between -3 and 3
    double scaleVariation = 0.95 + Math.random() * 0.1; // Random value between 0.95 and 1.05

    // Create translation animation
    TranslateTransition bodyTranslate = new TranslateTransition(Duration.seconds(4), character);
    bodyTranslate.setToX(character.getTranslateX() + translateX);
    bodyTranslate.setToY(character.getTranslateY() + translateY);
    bodyTranslate.setAutoReverse(true);
    bodyTranslate.setCycleCount(2);

    // Create subtle scale animation for breathing effect
    ScaleTransition bodyScale = new ScaleTransition(Duration.seconds(6), character);
    bodyScale.setToX(scaleVariation);
    bodyScale.setToY(scaleVariation);
    bodyScale.setAutoReverse(true);
    bodyScale.setCycleCount(2);

    // Start animations
    bodyTranslate.play();
    bodyScale.play();
  }
}
