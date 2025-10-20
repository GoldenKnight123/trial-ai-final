package nz.ac.auckland.se206;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class AudioPlayer {
  private static AudioPlayer instance;
  private Map<String, MediaPlayer> soundCache;
  private Random random;
  private int lastKeyboardSoundIndex = -1;
  private final int maxKeyboardSounds = 5;
  private boolean backgroundMuted = false;
  private Timeline audioTransitionTimeline;

  private AudioPlayer() {
    soundCache = new HashMap<>();
    random = new Random();
    initializeSounds();
  }

  /**
   * Gets the singleton instance of AudioPlayer.
   *
   * @return the AudioPlayer instance
   */
  public static AudioPlayer getInstance() {
    if (instance == null) {
      instance = new AudioPlayer();
    }
    return instance;
  }

  /** Initializes all sound files into the cache. */
  private void initializeSounds() {
    // List of sound files and their corresponding keys
    String[] soundFiles = {
      "src/main/resources/sounds/keyboard1.wav",
      "src/main/resources/sounds/keyboard2.wav",
      "src/main/resources/sounds/keyboard3.wav",
      "src/main/resources/sounds/keyboard4.wav",
      "src/main/resources/sounds/keyboard5.wav",
      "src/main/resources/sounds/error.wav",
      "src/main/resources/sounds/computerOpen.wav",
      "src/main/resources/sounds/computerClose.wav",
      "src/main/resources/sounds/playerTalk.wav",
      "src/main/resources/sounds/logos09Talk.wav",
      "src/main/resources/sounds/indus07Talk.wav",
      "src/main/resources/sounds/evanTalk.wav",
      "src/main/resources/sounds/computerTalk.wav",
      "src/main/resources/sounds/computerTalk2.wav",
      "src/main/resources/sounds/background.mp3",
      "src/main/resources/sounds/backgroundFlashback.mp3",
      "src/main/resources/sounds/buttonClick.mp3",
      "src/main/resources/sounds/zoomIn.wav",
      "src/main/resources/sounds/startButtonAppear.wav",
      "src/main/resources/sounds/sendMessage.wav",
      "src/main/resources/sounds/scan.wav",
      "src/main/resources/sounds/scanLong.wav",
      "src/main/resources/sounds/timesUp.wav",
      "src/main/resources/sounds/hover.wav",
      "src/main/resources/sounds/passwordAccepted.mp3",
      "src/main/resources/sounds/emailOpen.wav"
    };

    // Corresponding sound keys
    String[] soundKeys = {
      "keyboard1",
      "keyboard2",
      "keyboard3",
      "keyboard4",
      "keyboard5",
      "error",
      "computerOpen",
      "computerClose",
      "playerTalk",
      "logos09Talk",
      "indus07Talk",
      "evanTalk",
      "computerTalk",
      "computerTalk2",
      "background",
      "backgroundFlashback",
      "buttonClick",
      "zoomIn",
      "startButtonAppear",
      "sendMessage",
      "scan",
      "scanLong",
      "timesUp",
      "hover",
      "passwordAccepted",
      "emailOpen"
    };

    for (int i = 0; i < soundFiles.length; i++) {
      final int index = i; // Assign to a final variable
      try {
        File soundFile = new File(soundFiles[index]);
        if (soundFile.exists()) {
          Media soundMedia = new Media(soundFile.toURI().toString());
          MediaPlayer player = new MediaPlayer(soundMedia);

          // Set up error handling
          player.setOnError(
              () -> {
                System.err.println(
                    "MediaPlayer error for " + soundKeys[index] + ": " + player.getError());
              });

          // Set background music tracks to loop infinitely
          if (soundKeys[index].equals("background")
              || soundKeys[index].equals("backgroundFlashback")) {
            player.setCycleCount(MediaPlayer.INDEFINITE);
          }

          soundCache.put(soundKeys[index], player);
        } else {
          System.err.println("Sound file not found: " + soundFiles[index]);
        }
      } catch (Exception e) {
        System.err.println("Failed to load sound: " + soundFiles[index] + " - " + e.getMessage());
      }
    }
  }

  /**
   * Plays a sound by its key name.
   *
   * @param soundKey the key identifying the sound
   * @param volume the volume level (0.0 to 1.0)
   */
  public void playSound(String soundKey, double volume) {
    if (Platform.isFxApplicationThread()) {
      playSoundInternal(soundKey, volume);
    } else {
      Platform.runLater(() -> playSoundInternal(soundKey, volume));
    }
  }

  /** Internal method to play sound on JavaFX Application Thread. */
  private void playSoundInternal(String soundKey, double volume) {
    MediaPlayer player = soundCache.get(soundKey);
    if (player != null) {
      try {
        // Reset to beginning instead of stopping
        player.seek(Duration.ZERO);
        player.setVolume(volume);

        // Ensure player is ready before playing
        if (player.getStatus() == MediaPlayer.Status.READY
            || player.getStatus() == MediaPlayer.Status.PAUSED
            || player.getStatus() == MediaPlayer.Status.STOPPED) {
          player.play();
        }
      } catch (Exception e) {
        System.err.println("Error playing sound: " + soundKey + " - " + e.getMessage());
      }
    } else {
      System.err.println("Sound not found: " + soundKey);
    }
  }

  /**
   * Plays a sound with default volume of 0.5.
   *
   * @param soundKey the key identifying the sound
   */
  public void playSound(String soundKey) {
    playSound(soundKey, 0.5);
  }

  /**
   * Plays a random keyboard sound, ensuring it's different from the last one played.
   *
   * @param volume the volume level (0.0 to 1.0)
   */
  public void playRandomKeyboardSound(double volume) {
    int soundIndex;
    do {
      soundIndex = random.nextInt(maxKeyboardSounds);
    } while (soundIndex == lastKeyboardSoundIndex);

    lastKeyboardSoundIndex = soundIndex;
    playSound("keyboard" + (soundIndex + 1), volume);
  }

  /** Plays a random keyboard sound with default volume of 0.5. */
  public void playRandomKeyboardSound() {
    playRandomKeyboardSound(0.5);
  }

  /**
   * Adds a new sound to the cache.
   *
   * @param soundKey the key to identify the sound
   * @param filePath the path to the sound file
   */
  public void addSound(String soundKey, String filePath) {
    try {
      File soundFile = new File(filePath);
      if (soundFile.exists()) {
        Media soundMedia = new Media(soundFile.toURI().toString());
        MediaPlayer player = new MediaPlayer(soundMedia);

        // Set up error handling for new sounds
        player.setOnError(
            () -> {
              System.err.println("MediaPlayer error for " + soundKey + ": " + player.getError());
            });

        soundCache.put(soundKey, player);
      } else {
        System.err.println("Sound file not found: " + filePath);
      }
    } catch (Exception e) {
      System.err.println("Failed to add sound: " + filePath + " - " + e.getMessage());
    }
  }

  /** Stops all currently playing sounds. */
  public void stopAllSounds() {
    // Stop any ongoing audio transitions
    for (MediaPlayer player : soundCache.values()) {
      if (player != null) {
        try {
          player.stop();
        } catch (Exception e) {
          System.err.println("Error stopping sound: " + e.getMessage());
        }
      }
    }
  }

  /** Disposes of all MediaPlayer resources. */
  public void dispose() {
    // Dispose all MediaPlayers to free resources
    for (MediaPlayer player : soundCache.values()) {
      if (player != null) {
        try {
          player.dispose();
        } catch (Exception e) {
          System.err.println("Error disposing sound: " + e.getMessage());
        }
      }
    }
    soundCache.clear();
  }

  /**
   * Sets the volume for a specific sound.
   *
   * @param soundKey the key identifying the sound
   * @param volume the volume level (0.0 to 1.0)
   */
  public void setVolume(String soundKey, double volume) {
    MediaPlayer player = soundCache.get(soundKey);
    if (player != null) {
      try {
        player.setVolume(volume);
      } catch (Exception e) {
        System.err.println("Error setting volume for " + soundKey + ": " + e.getMessage());
      }
    }
  }

  /**
   * Sets the playback rate for a specific sound.
   *
   * @param soundKey the key identifying the sound
   * @param rate the playback rate (1.0 is normal speed)
   */
  public void setRate(String soundKey, double rate) {
    MediaPlayer player = soundCache.get(soundKey);
    if (player != null) {
      try {
        player.setRate(rate);
      } catch (Exception e) {
        System.err.println("Error setting rate for " + soundKey + ": " + e.getMessage());
      }
    }
  }

  public boolean toggleBackgroundMuted() {
    backgroundMuted = !backgroundMuted;

    // Set background music volume
    setVolume("background", backgroundMuted ? 0 : 0.25);

    // Also set flashback music volume if it's currently playing
    MediaPlayer flashbackPlayer = soundCache.get("backgroundFlashback");
    if (flashbackPlayer != null && flashbackPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
      setVolume("backgroundFlashback", backgroundMuted ? 0 : 0.25);
    }

    return backgroundMuted;
  }

  public void stopSound(String soundKey) {
    MediaPlayer player = soundCache.get(soundKey);
    if (player != null) {
      try {
        player.stop();
      } catch (Exception e) {
        System.err.println("Error stopping sound " + soundKey + ": " + e.getMessage());
      }
    }
  }

  public void startBackgroundMusic() {
    // Start both background and flashback tracks playing simultaneously
    // Background music starts at normal volume, flashback starts muted
    playSound("background", backgroundMuted ? 0 : 0.25);
    playSound("backgroundFlashback", 0); // Always start at 0 volume

    // Note: Both tracks are already set to loop infinitely during initialization
  }

  /**
   * Transitions to flashback music by fading out background and fading in flashback music. This
   * creates a smooth audio transition when entering flashbacks.
   */
  public void transitionToFlashbackMusic() {
    if (Platform.isFxApplicationThread()) {
      transitionToFlashbackMusicInternal();
    } else {
      Platform.runLater(this::transitionToFlashbackMusicInternal);
    }
  }

  private void transitionToFlashbackMusicInternal() {
    // Stop any existing transition
    if (audioTransitionTimeline != null) {
      audioTransitionTimeline.stop();
    }

    // Ensure both tracks are playing (they should already be from startBackgroundMusic)
    MediaPlayer backgroundPlayer = soundCache.get("background");
    MediaPlayer flashbackPlayer = soundCache.get("backgroundFlashback");

    if (backgroundPlayer != null && backgroundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
      backgroundPlayer.play();
    }
    if (flashbackPlayer != null && flashbackPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
      flashbackPlayer.play();
    }

    // Create transition timeline - crossfade between the tracks
    final double[] backgroundVolume = {backgroundMuted ? 0 : 0.25};
    final double[] flashbackVolume = {0.0};
    final int transitionSteps = 30; // Number of steps for smooth transition
    final double volumeStep = 0.25 / transitionSteps; // Volume change per step

    audioTransitionTimeline = new Timeline();

    for (int i = 0; i <= transitionSteps; i++) {
      final int step = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(i * 50), // 50ms between steps = 1.5 second total transition
              e -> {
                if (!backgroundMuted) {
                  backgroundVolume[0] = Math.max(0, 0.25 - (step * volumeStep));
                  setVolume("background", backgroundVolume[0]);
                }

                if (!backgroundMuted) {
                  flashbackVolume[0] = Math.min(0.25, step * volumeStep);
                  setVolume("backgroundFlashback", flashbackVolume[0]);
                }
              });
      audioTransitionTimeline.getKeyFrames().add(keyFrame);
    }

    audioTransitionTimeline.play();
  }

  /**
   * Transitions back to background music by fading out flashback and fading in background music.
   * This creates a smooth audio transition when exiting flashbacks.
   */
  public void transitionToBackgroundMusic() {
    if (Platform.isFxApplicationThread()) {
      transitionToBackgroundMusicInternal();
    } else {
      Platform.runLater(this::transitionToBackgroundMusicInternal);
    }
  }

  private void transitionToBackgroundMusicInternal() {
    // Stop any existing transition
    if (audioTransitionTimeline != null) {
      audioTransitionTimeline.stop();
    }

    // Ensure both tracks are playing (they should already be from startBackgroundMusic)
    MediaPlayer backgroundPlayer = soundCache.get("background");
    MediaPlayer flashbackPlayer = soundCache.get("backgroundFlashback");

    if (backgroundPlayer != null && backgroundPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
      backgroundPlayer.play();
    }
    if (flashbackPlayer != null && flashbackPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
      flashbackPlayer.play();
    }

    // Create transition timeline - crossfade between the tracks
    final double[] backgroundVolume = {0.0};
    final double[] flashbackVolume = {backgroundMuted ? 0 : 0.25};
    final int transitionSteps = 30; // Number of steps for smooth transition
    final double volumeStep = 0.25 / transitionSteps; // Volume change per step

    audioTransitionTimeline = new Timeline();

    for (int i = 0; i <= transitionSteps; i++) {
      final int step = i;
      KeyFrame keyFrame =
          new KeyFrame(
              Duration.millis(i * 50), // 50ms between steps = 1.5 second total transition
              e -> {
                if (!backgroundMuted) {
                  flashbackVolume[0] = Math.max(0, 0.25 - (step * volumeStep));
                  setVolume("backgroundFlashback", flashbackVolume[0]);
                }

                if (!backgroundMuted) {
                  backgroundVolume[0] = Math.min(0.25, step * volumeStep);
                  setVolume("background", backgroundVolume[0]);
                }
              });
      audioTransitionTimeline.getKeyFrames().add(keyFrame);
    }

    audioTransitionTimeline.play();
  }
}
