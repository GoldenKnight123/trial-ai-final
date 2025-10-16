package nz.ac.auckland.se206;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AudioPlayer {
  private static AudioPlayer instance;
  private Map<String, MediaPlayer> soundCache;
  private Random random;
  private int lastKeyboardSoundIndex = -1;
  private final int MAX_KEYBOARD_SOUNDS = 5;

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
    String[] soundFiles = {
      "src/main/resources/sounds/keyboard1.wav",
      "src/main/resources/sounds/keyboard2.wav",
      "src/main/resources/sounds/keyboard3.wav",
      "src/main/resources/sounds/keyboard4.wav",
      "src/main/resources/sounds/keyboard5.wav",
      "src/main/resources/sounds/error.wav",
      "src/main/resources/sounds/computerOpen.wav",
      "src/main/resources/sounds/computerClose.wav",
      "src/main/resources/sounds/messageSend.wav",
      "src/main/resources/sounds/uiClick.wav",
      "src/main/resources/sounds/uiBack.wav",
      "src/main/resources/sounds/computer2.wav",
      "src/main/resources/sounds/playerTalk.wav",
      "src/main/resources/sounds/logos09Talk.wav",
      "src/main/resources/sounds/indus07Talk.wav",
      "src/main/resources/sounds/evanTalk.wav",
      "src/main/resources/sounds/computerTalk.wav",
      "src/main/resources/sounds/computerTalk2.wav",
      "src/main/resources/sounds/background.mp3"
    };

    String[] soundKeys = {
      "keyboard1",
      "keyboard2",
      "keyboard3",
      "keyboard4",
      "keyboard5",
      "error",
      "computerOpen",
      "computerClose",
      "messageSend",
      "uiClick",
      "uiBack",
      "computer2",
      "playerTalk",
      "logos09Talk",
      "indus07Talk",
      "evanTalk",
      "computerTalk",
      "computerTalk2",
      "background"
    };

    for (int i = 0; i < soundFiles.length; i++) {
      try {
        File soundFile = new File(soundFiles[i]);
        if (soundFile.exists()) {
          Media soundMedia = new Media(soundFile.toURI().toString());
          MediaPlayer player = new MediaPlayer(soundMedia);
          soundCache.put(soundKeys[i], player);
        }
      } catch (Exception e) {
        System.err.println("Failed to load sound: " + soundFiles[i]);
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
    MediaPlayer player = soundCache.get(soundKey);
    if (player != null) {
      player.stop();
      player.setVolume(volume);
      player.play();
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
      soundIndex = random.nextInt(MAX_KEYBOARD_SOUNDS);
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
        soundCache.put(soundKey, player);
      }
    } catch (Exception e) {
      System.err.println("Failed to add sound: " + filePath);
    }
  }

  /** Stops all currently playing sounds. */
  public void stopAllSounds() {
    for (MediaPlayer player : soundCache.values()) {
      if (player != null) {
        player.stop();
      }
    }
  }

  /** Disposes of all MediaPlayer resources. */
  public void dispose() {
    for (MediaPlayer player : soundCache.values()) {
      if (player != null) {
        player.dispose();
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
      player.setVolume(volume);
    }
  }
}
