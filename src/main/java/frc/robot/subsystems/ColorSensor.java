package frc.robot.subsystems;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.AddressableLED; //LED STUFF
import edu.wpi.first.wpilibj.AddressableLEDBuffer; //LED STUFF
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.interfaces.ColorSensorInterface;

public class ColorSensor extends Subsystem implements ColorSensorInterface {
  //Values callibrated using vynl sticker for control panel.
  private final Color BlueTarget = ColorMatch.makeColor(0.147, 0.437, 0.416);
  private final Color GreenTarget = ColorMatch.makeColor(0.189, 0.559, 0.250);
  private final Color RedTarget = ColorMatch.makeColor(0.484, 0.366, 0.150);
  private final Color YellowTarget = ColorMatch.makeColor(0.322, 0.546, 0.131);
  private final Color WhiteTarget = ColorMatch.makeColor(0.276, 0.587, 0.217);
  
  /**
   * Change the I2C port below to match the connection of your color sensor
   */
  private final I2C.Port i2cPort = I2C.Port.kOnboard;

  /**
   * A Rev Color Sensor V3 object is constructed with an I2C port as a 
   * parameter. The device will be automatically initialized with default 
   * parameters.
   */
  private final ColorSensorV3 m_colorSensor = new ColorSensorV3(i2cPort);

  /**
   * A Rev Color Match object is used to register and detect known colors. This can 
   * be calibrated ahead of time or during operation.
   * 
   * This object uses a simple euclidian distance to estimate the closest match
   * with given confidence range.
   */
  private final ColorMatch m_colorMatcher = new ColorMatch();
  
  //private final double kFullClockwise = 1;
  //private final double kFullAntiClockwise = -1;
  private final double colorSpinnerOff = 0;
  private double colorSpinnerSpeed = -1;

  public String colorStringPrev = "";
  public String colorString;
  public String desiredColor = "";

  public AddressableLED m_led;
  public AddressableLEDBuffer m_ledBuffer;
  public int m_rainbowFirstPixelHue = 90;

  public boolean rotational = false;

  public String startColor;
  public String pairColor;
  public int rotCount;

  public boolean correctColor = false;
  public boolean firstLoop = true;
  public long spinTime;

  @Override
  public void robotInit() {
    m_colorMatcher.addColorMatch(BlueTarget);
    m_colorMatcher.addColorMatch(GreenTarget);
    m_colorMatcher.addColorMatch(RedTarget);
    m_colorMatcher.addColorMatch(YellowTarget);
    m_colorMatcher.addColorMatch(WhiteTarget);
    /*
    m_colorMatcher.addColorMatch(kYellowBlue);
    m_colorMatcher.addColorMatch(kBlueGreen);
    m_colorMatcher.addColorMatch(kGreenRed);
    m_colorMatcher.addColorMatch(kRedYellow);*/
    System.out.println("Started");
    // PWM port 9
    // Must be a PWM header, not MXP or DIO
    m_led = new AddressableLED(9);

    // Reuse buffer
    // Default to a length of 60, start empty output
    // Length is expensive to set, so only set it once, then just update data
    m_ledBuffer = new AddressableLEDBuffer(30);
    m_led.setLength(m_ledBuffer.getLength());

    // Set the data
    m_led.setData(m_ledBuffer);
    m_led.start();
  }

  @Override
  public void teleopPeriodic() {
    //Colour order: (Red, Yellow, Blue, Green) x2
    if (!rotational) {
      String newDesiredColor = getDesiredColor();
      if (!newDesiredColor.equals(desiredColor)) {
        System.out.println("Looking for " + newDesiredColor);
        desiredColor = newDesiredColor;
      }
      spinner.set(getMotorPower(desiredColor, colorString));
    } else {
        rotate3_5();
    }
    if (m_stick.getRawButtonPressed(6)) {
      addSpeed();
    }
    if (m_stick.getRawButtonPressed(5)) {
      subSpeed();
    }
    if (m_stick.getRawButtonPressed(10)) {
      rotational = !rotational;
      rotCount = -1;
      System.out.println("Rotational: " + rotational);
    }
  }

  private void rotate3_5() {
    desiredColor = "";
    if (rotCount < 0) {
      startColor = colorString;
      switch(startColor) {
        case "Red":
          pairColor = "Blue";
          break;
        case "Green":
          pairColor = "Yellow";
          break;
        case "Blue":
          pairColor = "Red";
          break;
        case "Yellow":
          pairColor = "Green";
          break;
      }
      System.out.println(startColor + ", " + pairColor);
      rotCount = 0;
    }
   if (rotCount < 14) { // 3.5 rotations == 14 quarter rotations
    if (rotCount > 12) {
      spinner.set(colorSpinnerSpeed/-2);
    } else {
      spinner.set(colorSpinnerSpeed*-1);
    }
   } else {
    spinner.set(colorSpinnerOff);
    desiredColor = "";
    rotational = false;
    System.out.println("Rotational: " + rotational);
   }
   if ((rotCount % 2 == 0 && colorString == pairColor) || (rotCount % 2 != 0 && colorString == startColor)) {
    rotCount += 1;
    System.out.println(rotCount);
   }
  }

  private double getMotorPower(String desired, String current) {
    if (desired.equals(current) || desired.equals("")) {
      if (correctColor = false) {
        if (firstLoop = true) {
          long spinTime = System.currentTimeMillis();
          if (spinner.get() > 0) {
            firstLoop = false;
            return 0.1;
          } else {
            firstLoop = false;
            return -0.1;
          }
        }
        if (spinTime - System.currentTimeMillis() < 1000) {
          return spinner.get();
        } else {
          correctColor = true;
          return colorSpinnerOff;
        }
      }
      return colorSpinnerOff;
    }
    if (colorString.equals("Red") && desired.equals("Yellow")) {
      return colorSpinnerSpeed/-2;
    }
    if (colorString.equals("Yellow") && desired.equals("Blue")) {
      return colorSpinnerSpeed/-2;
    }
    if (colorString.equals("Blue") && desired.equals("Green")) {
      return colorSpinnerSpeed/-2;
    }
    if (colorString.equals("Green") && desired.equals("Red")) {
      return colorSpinnerSpeed/-2;
    }
    if (colorString.equals("Yellow") && desired.equals("Red")) {
      return colorSpinnerSpeed/2;
    }
    if (colorString.equals("Blue") && desired.equals("Yellow")) {
      return colorSpinnerSpeed/2;
    }
    if (colorString.equals("Green") && desired.equals("Blue")) {
      return colorSpinnerSpeed/2;
    }
    if (colorString.equals("Red") && desired.equals("Green")) {
      return colorSpinnerSpeed/2;
    }
    if (colorString.equals("White")) {
      return spinner.get();
    }
    return colorSpinnerSpeed;
  }

  @Override
  public void robotPeriodic() {
    /**
     * The method GetColor() returns a normalized color value from the sensor and can be
     * useful if outputting the color to an RGB LED or similar. To
     * read the raw color, use GetRawColor().
     * 
     * The color sensor works best when within a few inches from an object in
     * well lit conditions (the built in LED is a big help here!). The farther
     * an object is the more light from the surroundings will bleed into the 
     * measurements and make it difficult to accurately determine its color.
     */
    Color detectedColor = m_colorSensor.getColor();
    
    ColorMatchResult match = m_colorMatcher.matchClosestColor(detectedColor);

    if (match.color == BlueTarget) {
      colorString = "Blue";
    } else if (match.color == RedTarget) {
      colorString = "Red";
    } else if (match.color == GreenTarget) {
      colorString = "Green";
    } else if (match.color == YellowTarget) {
      colorString = "Yellow";
    } else if (match.color == WhiteTarget) {
      colorString = "White";
    }/* else if (match.color == kYellowBlue) {
      colorString = "YellowBlue";
    } else if (match.color == kBlueGreen) {
      colorString = "BlueGreen";
    } else if (match.color == kGreenRed) {
      colorString = "GreenRed";
    } else if (match.color == kRedYellow) {
      colorString = "RedYellow";
    }*/ else {
      colorString = "Unknown";
    }
    
    colorString = doubleCheck();

    if (!colorString.equals(colorStringPrev)) {
      //Colour + Confidence
      System.out.println(colorString + " " + match.confidence);
      //Colour + Time taken to run test.
      //System.out.println(colorString + " " + (System.currentTimeMillis()-starttime));
      colorStringPrev = colorString;
    }
    setColor(colorString);
    //rainbow();
    // Set the LEDs
    m_led.setData(m_ledBuffer);
  }
  private String doubleCheck() {
    if (!colorString.equals(colorStringPrev)) {
      if (colorStringPrev.equals("Green") && spinner.get() < 0 && !colorString.equals("Blue")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Green") && spinner.get() > 0 && !colorString.equals("Red")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Blue") && spinner.get() < 0 && !colorString.equals("Yellow")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Blue") && spinner.get() > 0 && !colorString.equals("Green")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Yellow") && spinner.get() < 0 && !colorString.equals("Red")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Yellow") && spinner.get() > 0 && !colorString.equals("Blue")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Red") && spinner.get() < 0 && !colorString.equals("Green")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
      if (colorStringPrev.equals("Red") && spinner.get() > 0 && !colorString.equals("Yellow")) {
        System.out.println("Middle");
        return colorStringPrev;
      }
    }
      return colorString;
  }
  
  private void rainbow() {
    // For every pixel
    for (var i = 0; i < m_ledBuffer.getLength(); i++) {
      // Calculate the hue - hue is easier for rainbows because the color
      // shape is a circle so only one value needs to precess
      final var hue = (m_rainbowFirstPixelHue + (i * 180 / m_ledBuffer.getLength())) % 180;
      // Set the value
      m_ledBuffer.setHSV(i, hue, 255, 64);
    }
    // Increase by to make the rainbow "move"
    m_rainbowFirstPixelHue += 3;
    // Check bounds
    m_rainbowFirstPixelHue %= 180;
  }
  private void setColor(String colour) {
    // For every pixel
    if (colour.equals("Red")) {
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        m_ledBuffer.setRGB(i, 128, 0, 0);
      }
    } else if (colour.equals("Green")) {
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        m_ledBuffer.setRGB(i, 0, 128, 0);
      }
    } else if (colour.equals("Blue")) {
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        m_ledBuffer.setRGB(i, 0, 0, 128);
      }
    } else if (colour.equals("Yellow")) {
      for (var i = 0; i < m_ledBuffer.getLength(); i++) {
        m_ledBuffer.setRGB(i, 128, 128, 0);
      }
    } else {
      rainbow();
    }
  }
}