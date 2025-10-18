package nz.ac.auckland.se206.controllers;

import java.util.Random;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;
import nz.ac.auckland.se206.AudioPlayer;
import nz.ac.auckland.se206.ChatHistoryManager;

public class HumanController extends ChatController {

  // Newspaper jigsaw pieces from the FXML
  @FXML private ImageView newsPaperGuide;
  @FXML private ImageView topLeftNewsPaper;
  @FXML private ImageView topRightNewsPaper;
  @FXML private ImageView bottomLeftNewspaper;
  @FXML private ImageView bottomRightNewsPaper;
  @FXML private Rectangle rectOverlay;
  @FXML private Pane interactablePane;
  @FXML private ImageView pileInteractiveImg;
  @FXML private ImageView pilePopUpImg;

  // --- Puzzle Logic ---

  // Target coordinates for each piece, extracted from your FXML's solved state.
  private static final double TARGET_X_TOP_LEFT = 431.0;
  private static final double TARGET_Y_TOP_LEFT = 135.0;
  private static final double TARGET_X_TOP_RIGHT = 555.0;
  private static final double TARGET_Y_TOP_RIGHT = 135.0;
  private static final double TARGET_X_BOTTOM_LEFT = 431.0;
  private static final double TARGET_Y_BOTTOM_LEFT = 349.0;
  private static final double TARGET_X_BOTTOM_RIGHT = 524.0;
  private static final double TARGET_Y_BOTTOM_RIGHT = 330.0;
  private static final double TARGET_ROTATION = 0.0; // All pieces are upright.

  // A tolerance for how close the piece must be to snap into place.
  private static final double SNAP_TOLERANCE = 35.0;

  // Ensure we only notify AI once when puzzle is solved
  private boolean puzzleSolvedNotified = false;

  /** A small helper class to store the mouse's position relative to the image's top-left corner. */
  private static class DragContext {
    double x;
    double y;
  }

  @FXML
  @Override
  public void initialize() throws ApiProxyException {
    super.initialize();

    // Set up the newspaper jigsaw puzzle
    setupNewspaperPuzzle();

    // Register interactable sounds with the AudioPlayer singleton so they can be
    // played by key. (Files should exist under src/main/resources/sounds/)
    AudioPlayer audio = AudioPlayer.getInstance();
    audio.addSound("paper_pile_interact", "src/main/resources/sounds/paper_pile_interact.mp3");
    audio.addSound("paper_slide_human", "src/main/resources/sounds/paper_slide_human.mp3");
  }

  /** Reflection helper: set superclass private field only if it's currently null. */

  /** Sets up the newspaper puzzle by scattering the pieces and making them interactive. */
  private void setupNewspaperPuzzle() {
    // Scatter the pieces to random starting positions
    scatterPieces();

    // Make each piece draggable, rotatable, and snappable
    makePieceInteractive(topLeftNewsPaper, TARGET_X_TOP_LEFT, TARGET_Y_TOP_LEFT);
    makePieceInteractive(topRightNewsPaper, TARGET_X_TOP_RIGHT, TARGET_Y_TOP_RIGHT);
    makePieceInteractive(bottomLeftNewspaper, TARGET_X_BOTTOM_LEFT, TARGET_Y_BOTTOM_LEFT);
    makePieceInteractive(bottomRightNewsPaper, TARGET_X_BOTTOM_RIGHT, TARGET_Y_BOTTOM_RIGHT);
  }

  /** Moves the newspaper pieces to random locations on the screen to start the puzzle. */
  private void scatterPieces() {
    Random random = new Random();

    // Scatter pieces within the window bounds, avoiding the central puzzle area initially.
    topLeftNewsPaper.setLayoutX(random.nextDouble() * 200); // Left side
    topLeftNewsPaper.setLayoutY(random.nextDouble() * 500);
    // Start upright
    topLeftNewsPaper.setRotate(0);

    topRightNewsPaper.setLayoutX(900 + random.nextDouble() * 200); // Right side
    topRightNewsPaper.setLayoutY(random.nextDouble() * 500);
    // Start upright
    topRightNewsPaper.setRotate(0);

    bottomLeftNewspaper.setLayoutX(random.nextDouble() * 200); // Left side
    bottomLeftNewspaper.setLayoutY(100 + random.nextDouble() * 500);
    // Start upright
    bottomLeftNewspaper.setRotate(0);

    bottomRightNewsPaper.setLayoutX(900 + random.nextDouble() * 200); // Right side
    bottomRightNewsPaper.setLayoutY(100 + random.nextDouble() * 500);
    // Start upright
    bottomRightNewsPaper.setRotate(0);
  }

  /**
   * Applies all necessary event handlers to a puzzle piece to make it interactive.
   *
   * @param piece The ImageView of the puzzle piece.
   * @param targetX The final correct X position.
   * @param targetY The final correct Y position.
   */
  private void makePieceInteractive(ImageView piece, double targetX, double targetY) {
    final DragContext dragContext = new DragContext();

    // Event handler for when the mouse button is pressed
    piece.setOnMousePressed(
        event -> {
          // Prevent other nodes from processing the event
          event.consume();

          // Bring the clicked piece to the front to see it clearly
          piece.toFront();

          // Record the starting mouse position relative to the piece's corner
          dragContext.x = event.getSceneX() - piece.getLayoutX();
          dragContext.y = event.getSceneY() - piece.getLayoutY();
        });

    // Event handler for when the mouse is dragged
    piece.setOnMouseDragged(
        event -> {
          event.consume();
          // Update the piece's position based on mouse movement
          piece.setLayoutX(event.getSceneX() - dragContext.x);
          piece.setLayoutY(event.getSceneY() - dragContext.y);
        });

    // Event handler for when the mouse is clicked (for rotation)
    piece.setOnMouseClicked(
        event -> {
          event.consume();
          // Check for a right-click to rotate the piece
          if (event.getButton() == MouseButton.SECONDARY) {
            // Rotate by 90 degrees clockwise
            piece.setRotate((piece.getRotate() + 90) % 360);
          }
        });

    // Event handler for when the mouse button is released
    piece.setOnMouseReleased(
        event -> {
          event.consume();
          // Play slide sound on piece release
          try {
            AudioPlayer.getInstance().playSound("paper_slide_human");
          } catch (Exception ignored) {
            // Don't let audio failures break puzzle logic
          }
          // Check if the piece is in the correct final position
          checkPlacement(piece, targetX, targetY);
        });
  }

  /**
   * Checks if a puzzle piece has been moved to its correct location. If so, it snaps the piece into
   * place and disables it.
   *
   * @param piece The piece that was just released.
   * @param targetX The target X coordinate for the piece.
   * @param targetY The target Y coordinate for the piece.
   */
  private void checkPlacement(ImageView piece, double targetX, double targetY) {
    // Calculate the distance between the piece's center and its target's center
    double distance =
        Math.sqrt(
            Math.pow(piece.getLayoutX() - targetX, 2) + Math.pow(piece.getLayoutY() - targetY, 2));

    // Check if the piece is close enough to the target and has the correct rotation
    if (distance < SNAP_TOLERANCE && piece.getRotate() == TARGET_ROTATION) {
      // Snap to the exact position and rotation
      piece.setLayoutX(targetX);
      piece.setLayoutY(targetY);
      piece.setRotate(TARGET_ROTATION);

      // Lock the piece in place by disabling it
      piece.setDisable(true);

      // Check if this was the last piece to solve the puzzle
      checkWinCondition();
    }
  }

  /** Checks if all puzzle pieces are in their correct, disabled state. */
  private void checkWinCondition() {
    if (topLeftNewsPaper.isDisabled()
        && topRightNewsPaper.isDisabled()
        && bottomLeftNewspaper.isDisabled()
        && bottomRightNewsPaper.isDisabled()) {

      // All pieces are correctly placed. Puzzle is solved.
      // Hide the guide image if present. No popup notification is shown.
      if (newsPaperGuide != null) {
        newsPaperGuide.setVisible(false);
      }

      // The puzzle is solved. Keep the overlay visible so the user must click
      // it to close the interactable. This mirrors your requested UX: the
      // interactable will only be hidden/disabled when the overlay is clicked.
      if (rectOverlay != null) {
        rectOverlay.setVisible(true);
        rectOverlay.setDisable(false);
      }

      // Notify AI and mark interactable complete once
      if (!puzzleSolvedNotified && getChatCompletionRequest() != null) {
        ChatMessage sysMsg =
            new ChatMessage(
                "system",
                "The newspaper article has been reassembled and is now readable by the judge.");

        if (getTarget() != null) {
          ChatHistoryManager.getInstance().addMessage(getTarget(), sysMsg);
        }
        getChatCompletionRequest().addMessage(sysMsg);

        // Mark interactable complete (use the appropriate id — 'Evan' preserved from old code)
        ChatHistoryManager.getInstance().markInteractableComplete("Evan");

        puzzleSolvedNotified = true;
        // Optionally display interaction dialogue that may be attached to this event
        displayInteractionDialogue();
      }
    }
  }

  /**
   * Handler for clicking the overlay area to close the interaction. Matches
   * onMouseClicked="#onCloseInteraction" in the FXML.
   */
  @FXML
  public void onCloseInteraction(MouseEvent event) {
    // Safely hide the overlay rectangle if it's present
    if (rectOverlay != null) {
      rectOverlay.setVisible(false);
      rectOverlay.setDisable(true);
    }

    // The user wanted the entire interactable pane to be disabled and hidden
    // when the overlay is closed — do that directly.
    if (interactablePane != null) {
      interactablePane.setVisible(false);
      interactablePane.setDisable(true);
    }

    // Consume the event so it doesn't propagate further
    if (event != null) {
      event.consume();
    }
  }

  /** Sets up the hover and click handlers for the pile of newspapers. */
  @FXML
  private void pileEntered(MouseEvent event) {
    System.out.println("Pile entered");
    if (pileInteractiveImg != null) {
      pileInteractiveImg.setVisible(false);
    }
    if (pilePopUpImg != null) {
      pilePopUpImg.setVisible(true);
    }
    try {
      AudioPlayer.getInstance().playSound("paper_pile_interact", 0.3);
    } catch (Exception ignored) {
    }
    if (event != null) {
      event.consume();
    }
  }

  @FXML
  private void pileExited(MouseEvent event) {
    System.out.println("Pile exited");
    if (pileInteractiveImg != null) {
      pileInteractiveImg.setVisible(true);
    }
    if (pilePopUpImg != null) {
      pilePopUpImg.setVisible(false);
    }
    if (event != null) {
      event.consume();
    }
  }

  @FXML
  private void pileClicked(MouseEvent event) {
    System.out.println("Pile clicked");
    try {
      AudioPlayer.getInstance().playSound("paper_pile_interact", 0.6);
    } catch (Exception ignored) {
    }
    // Enable the interactable pane when the pile is clicked
    if (interactablePane != null) {
      interactablePane.setVisible(true);
      interactablePane.setDisable(false);
      // Ensure the interactable pane is on top so the overlay is visible
      interactablePane.toFront();
    }

    // Explicitly show and enable the overlay, and bring it to the front so
    // it blocks the underlying UI while the player interacts with the puzzle.
    if (rectOverlay != null) {
      rectOverlay.setVisible(true);
      rectOverlay.setDisable(false);
    }

    if (event != null) {
      event.consume();
    }
  }
}
