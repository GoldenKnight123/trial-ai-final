package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nz.ac.auckland.apiproxy.chat.openai.ChatMessage;

/**
 * Singleton class for managing chat history across all controllers. This class provides a
 * centralized way to store, retrieve, and manage chat messages for different targets (characters)
 * in the game.
 */
public class ChatHistoryManager {

  private static ChatHistoryManager instance;

  // Store chat messages for each target
  private Map<String, List<ChatMessage>> chatHistory;

  // Store formatted chat history text
  private String chatHistoryText;

  // Store fixed dialogue for each character
  private Map<String, String> fixedDialogue;

  // Store interaction dialogue for each character
  private Map<String, String> interactionDialogue;

  /** Private constructor to enforce singleton pattern. */
  private ChatHistoryManager() {
    initialize();
  }

  /**
   * Gets the singleton instance of ChatHistoryManager.
   *
   * @return the singleton instance
   */
  public static ChatHistoryManager getInstance() {
    if (instance == null) {
      instance = new ChatHistoryManager();
    }
    return instance;
  }

  /** Initializes the chat history manager with default values and fixed dialogue. */
  private void initialize() {
    chatHistory = new HashMap<>();
    chatHistoryText = "";
    fixedDialogue = new HashMap<>();
    interactionDialogue = new HashMap<>();

    // Initialize chat history for each character
    chatHistory.put("LOGOS-09", new ArrayList<>());
    chatHistory.put("INDUS-07", new ArrayList<>());
    chatHistory.put("Evan", new ArrayList<>());

    // Initialize fixed dialogue for each character
    fixedDialogue.put(
        "LOGOS-09",
        "It is currently 16th July 22:17AM. I have detected a message from INDUS-07 sent to site"
            + " workers to increase the output levels of the power plant by 15%.");
    fixedDialogue.put(
        "INDUS-07",
        "It is currently 16th July 21:27AM. I am preparing and analysing a way to increase the"
            + " output of the power plant.");
    fixedDialogue.put(
        "Evan",
        "After installing the new module, I began a routine diagnostics check. But there was heavy"
            + " interference in the signal, so I had to carefully tune the diagnostics tool just to"
            + " get a clear reading");

    // Initialize interaction dialogue for each character
    interactionDialogue.put(
        "LOGOS-09",
        "I see you managed to decrypt the message. It seems INDUS-07 was contacted by their manager"
            + " before they decided to install the new Helios system.");
    interactionDialogue.put(
        "INDUS-07",
        "INDUS-07: \"I was preparing and analysing a way to increase the output of the power"
            + " plant.\"");
    interactionDialogue.put(
        "Evan",
        "Evan: \"After carefully tuning the diagnostics, I finally saw the readings clearly, the"
            + " values were at dangerously high levels.\"");
  }

  /**
   * Adds a chat message to the history for a specific target.
   *
   * @param target the character/target to add the message for
   * @param message the chat message to add
   */
  public void addMessage(String target, ChatMessage message) {
    if (target != null && chatHistory.containsKey(target)) {
      chatHistory.get(target).add(message);
      updateChatHistoryText(target, message);
    } else {
      System.err.println("Error: target is null or not found in chatHistory: " + target);
    }
  }

  /**
   * Updates the formatted chat history text when a new message is added.
   *
   * @param target the character/target that sent/received the message
   * @param message the chat message that was added
   */
  private void updateChatHistoryText(String target, ChatMessage message) {
    String role = message.getRole();
    String speaker;

    if ("user".equals(role)) {
      speaker = "You";
    } else {
      speaker = target;
    }

    chatHistoryText += speaker + ": " + message.getContent() + "\n";

    // Add extra newline for assistant messages to match original formatting
    if ("assistant".equals(role)) {
      chatHistoryText += "\n";
    }
  }

  /**
   * Gets the chat history for a specific target.
   *
   * @param target the character/target to get history for
   * @return the list of chat messages for the target, or empty list if target not found
   */
  public List<ChatMessage> getChatHistory(String target) {
    return chatHistory.getOrDefault(target, new ArrayList<>());
  }

  /**
   * Gets the complete formatted chat history text.
   *
   * @return the complete chat history as a formatted string
   */
  public String getChatHistoryText() {
    return chatHistoryText;
  }

  /**
   * Gets the fixed dialogue for a specific character.
   *
   * @param target the character to get fixed dialogue for
   * @return the fixed dialogue string, or null if not found
   */
  public String getFixedDialogue(String target) {
    return fixedDialogue.get(target);
  }

  /**
   * Gets the interaction dialogue for a specific character.
   *
   * @param target the character to get interaction dialogue for
   * @return the interaction dialogue string, or null if not found
   */
  public String getInteractionDialogue(String target) {
    return interactionDialogue.get(target);
  }

  /**
   * Checks if a target has any chat history.
   *
   * @param target the character/target to check
   * @return true if the target exists in chat history, false otherwise
   */
  public boolean hasTarget(String target) {
    return chatHistory.containsKey(target);
  }

  /**
   * Gets a snapshot of the current chat history text. This is useful for creating system prompts
   * that include the current conversation state.
   *
   * @return a copy of the current chat history text
   */
  public String getChatHistorySnapshot() {
    return new String(chatHistoryText);
  }

  /**
   * Checks if the chat history contains a specific dialogue for a target.
   *
   * @param target the character/target to check
   * @param dialogue the dialogue text to search for
   * @return true if the dialogue is found in the chat history, false otherwise
   */
  public boolean containsDialogue(String target, String dialogue) {
    return chatHistoryText.contains(dialogue);
  }

  /** Resets all chat history. This can be used when starting a new game session. */
  public void reset() {
    chatHistory.clear();
    chatHistoryText = "";
    initialize();
  }

  /**
   * Gets the last message sent by a specific target from the chat history text.
   *
   * @param target the character/target to get the last message for
   * @return the last message content, or empty string if not found
   */
  public String getLastMessage(String target) {
    String targetPrefix = target + ": ";
    int lastIndex = chatHistoryText.lastIndexOf(targetPrefix);

    if (lastIndex == -1) {
      return "";
    }

    String lastMessage = chatHistoryText.substring(lastIndex + targetPrefix.length());
    int endIndex = lastMessage.indexOf("\n\n");

    if (endIndex != -1) {
      lastMessage = lastMessage.substring(0, endIndex);
    } else {
      // If no double newline found, look for single newline
      endIndex = lastMessage.indexOf("\n");
      if (endIndex != -1) {
        lastMessage = lastMessage.substring(0, endIndex);
      }
    }

    return lastMessage.trim();
  }

  public boolean enablePlayerMakeVerdict() {
    String[] participants = {"LOGOS-09", "INDUS-07", "Evan"};
    for (String participant : participants) {
      boolean asked = false;
      for (ChatMessage msg : getChatHistory(participant)) {
        if ("user".equals(msg.getRole())) {
          asked = true;
          break;
        }
      }
      if (!asked) {
        return false;
      }
    }
    return true;
  }
}
