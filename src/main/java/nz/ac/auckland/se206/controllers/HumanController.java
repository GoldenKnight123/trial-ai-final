package nz.ac.auckland.se206.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;
import nz.ac.auckland.apiproxy.exceptions.ApiProxyException;

public class HumanController extends ChatController {

  @FXML private Slider slider;
  @FXML private Text tempTxt;
  @FXML private Text pressureTxt;
  @FXML private Text flowTxt;
  @FXML private Text stressTxt;

  // The real values you want to reveal
  private final String tempValue = "72°C";
  private final String pressureValue = "1.2 bar";
  private final String flowValue = "15 L/s";
  private final String stressValue = "Critical";

  // Where the slider "sweet spot" is
  private final double sweetSpot = 50; // midpoint of slider (adjust as needed)
  private final double tolerance = 5; // how close the player must get

  @FXML
  @Override
  public void initialize() throws ApiProxyException {
    // TODO Auto-generated method stub
    super.initialize();

    slider
        .valueProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              double distance = Math.abs(newVal.doubleValue() - sweetSpot);

              if (distance <= tolerance) {
                // Show the correct values
                tempTxt.setText(tempValue);
                pressureTxt.setText(pressureValue);
                flowTxt.setText(flowValue);
                stressTxt.setText(stressValue);
              } else {
                // Scramble them based on slider distance
                tempTxt.setText(scramble(tempValue));
                pressureTxt.setText(scramble(pressureValue));
                flowTxt.setText(scramble(flowValue));
                stressTxt.setText(scramble(stressValue));
              }
            });
  }

  private String scramble(String input) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isWhitespace(c)) {
        sb.append(" "); // keep spaces
      } else {
        // Replace with a random symbol
        sb.append("#$%&*?".charAt((int) (Math.random() * 6)));
      }
    }
    return sb.toString();
  }
}
