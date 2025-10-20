package nz.ac.auckland.se206;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import nz.ac.auckland.se206.controllers.AiDefendantController;
import nz.ac.auckland.se206.controllers.AiWitnessController;
import nz.ac.auckland.se206.controllers.ChatController;
import nz.ac.auckland.se206.controllers.DebriefController;
import nz.ac.auckland.se206.controllers.FlashbackController;
import nz.ac.auckland.se206.controllers.GameOverController;
import nz.ac.auckland.se206.controllers.HumanController;
import nz.ac.auckland.se206.controllers.RoomController;
import nz.ac.auckland.se206.controllers.StartMenuController;
import nz.ac.auckland.se206.controllers.VerdictController;

/**
 * This is the entry point of the JavaFX application. This class initializes and runs the JavaFX
 * application.
 */
public class App extends Application {

  /** Inner class to hold both scene and controller together. */
  private static class SceneControllerPair {
    private final Scene scene;
    private final Object controller;

    // Constructor
    public SceneControllerPair(Scene scene, Object controller) {
      this.scene = scene;
      this.controller = controller;
    }

    // Getter for scene
    public Scene getScene() {
      return scene;
    }

    // Getter for controller
    public Object getController() {
      return controller;
    }
  }

  // Cached scene and primary stage
  private static Scene scene;
  private static Stage primaryStage;
  private static String currentId = "";

  // Cache for storing loaded scenes and controllers
  private static Map<String, Scene> sceneCache = new HashMap<>();
  private static Map<String, Object> controllerCache = new HashMap<>();

  /**
   * The main method that launches the JavaFX application.
   *
   * @param args the command line arguments
   */
  public static void main(final String[] args) {
    launch();
  }

  /**
   * Loads and caches a scene if not already cached, then returns the cached scene.
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the cached scene
   * @throws IOException if the FXML file is not found
   */
  private static SceneControllerPair loadAndCacheScene(String fxml) throws IOException {
    String cacheKey = fxml;

    // Load and cache the scene if not already cached
    if (!sceneCache.containsKey(cacheKey)) {
      // Load FXML and create scene
      FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
      System.out.println("Loading and caching scene: " + fxml);

      // Load FXML and create scene
      Parent root = loader.load();
      Object controller = loader.getController();

      // Create scene with black background
      Scene newScene = new Scene(root);
      newScene.setFill(Color.BLACK);

      // Cache the scene and controller
      sceneCache.put(cacheKey, newScene);
      controllerCache.put(cacheKey, controller);
    }

    return new SceneControllerPair(sceneCache.get(cacheKey), controllerCache.get(cacheKey));
  }

  /**
   * Gets a cached controller for the specified FXML file.
   *
   * @param fxml the name of the FXML file (without extension)
   * @return the controller instance, or null if not cached
   */
  public static Object getController(String fxml) {
    return controllerCache.get(fxml);
  }

  /**
   * Sets the root of the scene to the specified FXML file using cached scenes.
   *
   * @param fxml the name of the FXML file (without extension)
   * @throws IOException if the FXML file is not found
   */
  public static void setRoot(String fxml) throws IOException {
    SceneControllerPair pair = loadAndCacheScene(fxml);
    scene = pair.getScene();
    primaryStage.setScene(scene);
  }

  /**
   * Opens the flashback view and sets the target for the flashback controller using cached scenes.
   *
   * @param event the mouse event that triggered the method
   * @param target the target to display in the flashback (for example a character id)
   * @throws IOException if the FXML file is not found
   */
  public static void openFlashback(MouseEvent event, String target) throws IOException {
    // Load and cache the flashback scene
    SceneControllerPair pair = loadAndCacheScene("flashback");
    FlashbackController flashbackController = (FlashbackController) pair.getController();

    // Set the target for the flashback controller and fade in
    flashbackController.setTarget(target);
    flashbackController.fadeIn();
    currentId = "flashback";

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Opens the start screen using cached scenes.
   *
   * @param event the mouse event that triggered the method
   * @throws IOException if the FXML file is not found
   */
  public static void openStartScreen(MouseEvent event) throws IOException {
    // Load and cache the start screen scene
    SceneControllerPair pair = loadAndCacheScene("startScreen");
    StartMenuController startMenuController = (StartMenuController) pair.getController();
    System.out.println("Opening start screen");

    // Start fade in first, then set scene
    startMenuController.fadeIn();
    currentId = "startScreen";

    // Set the scene and show the primary stage
    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Opens a memory scene for the specified target (AI or human) using cached scenes and fades it
   * in.
   *
   * @param event the mouse event that triggered the method
   * @param target the target to set (for example: "LOGOS-09", "INDUS-07", "Evan")
   * @throws IOException if the FXML file is not found
   */
  public static void openChat(MouseEvent event, String target) throws IOException {
    switch (target) {
      case "LOGOS-09":
        // AI witness selected
        SceneControllerPair witnessPair = loadAndCacheScene("memoryAiWitness");
        AiWitnessController aiWitnessController = (AiWitnessController) witnessPair.getController();
        aiWitnessController.setTarget(target);

        aiWitnessController.setInitialOpacity();

        scene = witnessPair.getScene();
        primaryStage.setScene(scene);
        primaryStage.show();
        currentId = "memoryAiWitness";

        aiWitnessController.fadeIn();
        break;
      case "INDUS-07":
        // AI defendant selected
        SceneControllerPair defendantPair = loadAndCacheScene("memoryAiDefendant");
        AiDefendantController aiDefendantController =
            (AiDefendantController) defendantPair.getController();
        aiDefendantController.setTarget(target);

        aiDefendantController.setInitialOpacity();

        scene = defendantPair.getScene();
        primaryStage.setScene(scene);
        primaryStage.show();
        currentId = "memoryAiDefendant";

        aiDefendantController.fadeIn();
        break;
      case "Evan":
        // Human witness selected
        SceneControllerPair pair = loadAndCacheScene("memoryHuman");
        HumanController humanController = (HumanController) pair.getController();
        humanController.setTarget(target);

        humanController.setInitialOpacity();

        scene = pair.getScene();
        primaryStage.setScene(scene);
        primaryStage.show();
        currentId = "memoryHuman";

        humanController.fadeIn();
        break;
      default:
        throw new IllegalArgumentException("Unknown target: " + target);
    }
  }

  public static void openRoom(ActionEvent event) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("room");

    // Prepare the room for smooth transition
    RoomController roomController = (RoomController) pair.getController();

    // Start fade in first, then set scene
    roomController.fadeIn();
    currentId = "room";

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Opens the verdict scene using cached scenes.
   *
   * @param event the mouse event that triggered the method
   * @throws IOException if the FXML file is not found
   */
  public static void openVerdict(ActionEvent event) throws IOException {

    // Load and cache the verdict scene
    SceneControllerPair pair = loadAndCacheScene("verdict");
    VerdictController verdictController = (VerdictController) pair.getController();
    // Bind a timer-expired handler for the verdict scene so the controller can show a message
    try {
      verdictController.bindTimerExpired();
    } catch (Exception e) {
      // safe no-op if controller doesn't support it
    }
    verdictController.fadeIn();
    currentId = "verdict";

    // Set the verdict text in the controller
    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Opens the game over scene using cached scenes.
   *
   * @param event the mouse event that triggered the method
   * @param message the message to display on the game over screen
   * @throws IOException if the FXML file is not found
   */
  public static void openGameOver(ActionEvent event, String message) throws IOException {
    // Load and cache the game over scene
    SceneControllerPair pair = loadAndCacheScene("gameOver");
    GameOverController gameOverController = (GameOverController) pair.getController();
    gameOverController.setGameOverMessage(message);
    // Start fade in first, then set scene
    currentId = "gameOver";
    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void openDebrief(ActionEvent event, boolean correct) throws IOException {
    SceneControllerPair pair = loadAndCacheScene("debrief");

    // Ensure the debrief is visible when returning to it
    DebriefController debriefController = (DebriefController) pair.getController();

    if (correct) {
      debriefController.setCorrectLabel("You were CORRECT!");
    } else {
      debriefController.setCorrectLabel("You were INCORRECT!");
    }

    // Set verdict text in debrief controller
    VerdictController verdictController = (VerdictController) getController("verdict");
    String verdictText = verdictController.getVerdictText();

    // Get chat history from ChatHistoryManager
    String chatHistory = ChatHistoryManager.getInstance().getChatHistoryText();

    // Start GPT analysis with chat history and decision correctness
    debriefController.runGptDebrief(chatHistory, verdictText, correct);
    currentId = "debrief";

    scene = pair.getScene();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  /**
   * Forces a return to the room scene, typically invoked when the timer expires.
   *
   * <p>This method handles different current scenes appropriately, ensuring a smooth transition
   * back to the room. If the current scene is already the room, it resets the room state. If in a
   * chat or flashback scene, it fades out before transitioning.
   */
  public static void forceReturnToRoom() {
    System.out.println("Timer expired, forcing return to room");
    ArrayList<String> roomIds =
        new ArrayList<>(Arrays.asList("memoryAiWitness", "memoryAiDefendant", "memoryHuman"));
    try {
      // If already in the room, just reset the state
      if (currentId.equals("room")) {
        SceneControllerPair roomPair = loadAndCacheScene("room");
        RoomController roomController = (RoomController) roomPair.getController();
        roomController.getContext().setState(roomController.getContext().getGuessingState());
        roomController.initializeGuessingState();
        return;
      }

      // Otherwise, fade out current scene and return to room
      System.out.println("Current scene: " + currentId);
      SceneControllerPair pair = loadAndCacheScene(currentId);

      if (roomIds.contains(currentId)) {
        ChatController chatController = (ChatController) pair.getController();
        // Stop any ongoing typewriter effects before fading out
        chatController.stopAnyTypewriterEffect();
        chatController.fadeOut(null);
      } else if (currentId.equals("flashback")) {
        // Transition back to background music when leaving flashback due to timer
        AudioPlayer.getInstance().transitionToBackgroundMusic();
        FlashbackController flashbackController = (FlashbackController) pair.getController();
        // Stop any ongoing typewriter effects before fading out
        flashbackController.stopAnyTypewriterEffect();
        flashbackController.fadeOut(null);
      } else {
        System.out.println("Unknown scene, returning to room directly");
        openRoom(null);
        return;
      }

      // Set to guessing state in room after fade out
      SceneControllerPair roomPair = loadAndCacheScene("room");
      RoomController roomController = (RoomController) roomPair.getController();
      roomController.getContext().setState(roomController.getContext().getGuessingState());
      roomController.initializeGuessingState();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is invoked when the application starts. It loads and shows the "room" scene using
   * the caching system.
   *
   * @param stage the primary stage of the application
   * @throws IOException if the "src/main/resources/fxml/room.fxml" file is not found
   */
  @Override
  public void start(final Stage stage) throws IOException {
    primaryStage = stage; // Store reference to primary stage

    SceneControllerPair pair = loadAndCacheScene("startScreen");
    scene = pair.getScene();
    stage.setScene(scene);
    stage.show();
    scene.getRoot().requestFocus();
  }

  public static void resetFxmlFiles() {
    try {
      Path fxmlReloadDir = Paths.get("src/main/resources/fxml_reload");
      Path fxmlDir = Paths.get("src/main/resources/fxml");

      // Copy each FXML file from fxml_reload to fxml
      Files.list(fxmlReloadDir)
          .filter(path -> path.toString().endsWith(".fxml"))
          .forEach(
              source -> {
                try {
                  Path target = fxmlDir.resolve(source.getFileName());
                  Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                  System.out.println("Reset: " + source.getFileName());
                } catch (IOException e) {
                  System.err.println("Failed to reset: " + source.getFileName());
                  e.printStackTrace();
                }
              });

    } catch (IOException e) {
      System.err.println("Failed to reset FXML files");
      e.printStackTrace();
    }
  }

  /**
   * Reinitializes the application state by clearing cached scenes and controllers, resetting the
   * game timer, resetting chat history and resetting the state of the courtroom. This is used for
   * starting a new game session.
   */
  public static void reinitialize() {
    // Reset the room controller state
    RoomController roomController = (RoomController) getController("room");
    if (roomController != null) {
      roomController.setFirstTimeInit(true);
    }
    // Clear cached scenes and controllers
    sceneCache.clear();
    controllerCache.clear();

    // Reset game timer and chat history
    GameTimer.getInstance().reset();
    ChatHistoryManager.getInstance().reset();
  }
}
