package frc.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.interfaces.ColorSensorInterface;

public class ColorSensor extends Subsystem implements ColorSensorInterface {

  private enum Colour {
    RED,
    YELLOW,
    BLUE,
    GREEN,
    UNKNOWN
  }


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
  private final ColorSensorV3 colourSensor = new ColorSensorV3(i2cPort);

  /**
   * A Rev Color Match object is used to register and detect known colors. This can 
   * be calibrated ahead of time or during operation.
   * 
   * This object uses a simple euclidian distance to estimate the closest match
   * with given confidence range.
   */
  private final ColorMatch colourMatcher = new ColorMatch();
  
  //private final double kFullClockwise = 1;
  //private final double kFullAntiClockwise = -1;
  private final double colourSpinnerOff = 0;
  private double colourSpinnerSpeed = -1;

  private Colour colourPrev = Colour.UNKNOWN;
  private Colour colour;
  private Colour desiredColour = Colour.UNKNOWN;

  private boolean rotational = false;

  private Colour startColour;
  private Colour pairColour;
  private int rotCount;

  private final PWMVictorSPX spinner = new PWMVictorSPX(1);

  @Override
  public void robotInit() {
    colourMatcher.addColorMatch(BlueTarget);
    colourMatcher.addColorMatch(GreenTarget);
    colourMatcher.addColorMatch(RedTarget);
    colourMatcher.addColorMatch(YellowTarget);
    colourMatcher.addColorMatch(WhiteTarget);
    // PWM port 9
    // Must be a PWM header, not MXP or DIO
  }

  @Override
  public void teleopPeriodic() {
    //Colour order: (Red, Yellow, Blue, Green) x2
    if (!rotational) {
      Colour newDesiredColor = Colour.getDesiredColor();
      if (newDesiredColor != desiredColour) {
        System.out.println("Looking for " + newDesiredColor);
        desiredColour = newDesiredColor;
      }
      spinner.set(getMotorPower(desiredColour, colour));
    } else {
        rotate3_5();
    }
    if (m_stick.getRawButtonPressed(10)) {
      rotational = !rotational;
      rotCount = -1;
      System.out.println("Rotational: " + rotational);
    }
  }

  private void rotate3_5() {
    desiredColour = Colour.UNKNOWN;
    if (rotCount < 0) {
      startColour = colour;
      switch(startColour) {
        case RED:
          pairColour = Colour.BLUE;
          break;
        case GREEN:
          pairColour = Colour.YELLOW;
          break;
        case BLUE:
          pairColour = Colour.RED;
          break;
        case YELLOW:
          pairColour = Colour.GREEN;
          break;
        case UNKNOWN:
          pairColour = Colour.UNKNOWN;
          break;
      }
      System.out.println(startColour + ", " + pairColour);
      rotCount = 0;
    }
   if (rotCount < 14) { // 3.5 rotations == 14 quarter rotations
    if (rotCount > 12) {
      spinner.set(colourSpinnerSpeed/-2);
    } else {
      spinner.set(colourSpinnerSpeed*-1);
    }
   } else {
    spinner.set(colourSpinnerOff);
    desiredColour = Colour.UNKNOWN;
    rotational = false;
    System.out.println("Rotational: " + rotational);
   }
   if ((rotCount % 2 == 0 && colour == pairColour) || (rotCount % 2 != 0 && colour == startColour)) {
    rotCount += 1;
    System.out.println(rotCount);
   }
  }

  private double getMotorPower(Colour desired, Colour current) {
    if (desired == current || desired == Colour.UNKNOWN) {
      return colourSpinnerOff;
    }
    if (current == Colour.RED && desired == Colour.YELLOW) {
      return colourSpinnerSpeed/-2;
    }
    if (current == Colour.YELLOW && desired == Colour.BLUE) {
      return colourSpinnerSpeed/-2;
    }
    if (current == Colour.BLUE && desired == Colour.GREEN) {
      return colourSpinnerSpeed/-2;
    }
    if (current == Colour.GREEN && desired == Colour.RED) {
      return colourSpinnerSpeed/-2;
    }
    if (current == Colour.YELLOW && desired == Colour.RED) {
      return colourSpinnerSpeed/2;
    }
    if (current == Colour.BLUE && desired == Colour.YELLOW) {
      return colourSpinnerSpeed/2;
    }
    if (current == Colour.GREEN && desired == Colour.BLUE) {
      return colourSpinnerSpeed/2;
    }
    if (current == Colour.RED && desired == Colour.GREEN) {
      return colourSpinnerSpeed/2;
    }
    if (current == Colour.UNKNOWN) {
      return spinner.get();
    }
    return colourSpinnerSpeed;
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
    Color detectedColor = colourSensor.getColor();
    
    ColorMatchResult match = colourMatcher.matchClosestColor(detectedColor);

    if (match.color == BlueTarget) {
      colour = Colour.BLUE;
    } else if (match.color == RedTarget) {
      colour = Colour.RED;
    } else if (match.color == GreenTarget) {
      colour = Colour.GREEN;
    } else if (match.color == YellowTarget) {
      colour = Colour.YELLOW;
    } else {
      colour = Colour.UNKNOWN;
    }
    
    colour = doubleCheck();

    if (colour != colourPrev) {
      //Colour + Confidence
      System.out.println(colour + " " + match.confidence);
      //Colour + Time taken to run test.
      //System.out.println(colorString + " " + (System.currentTimeMillis()-starttime));
      colourPrev = colour;
    }
  }
  private Colour doubleCheck() {
    if (colour != colourPrev) {
      if (colourPrev == Colour.GREEN && spinner.get() < 0 && colour != Colour.BLUE) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.GREEN && spinner.get() > 0 && colour != Colour.RED) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.BLUE && spinner.get() < 0 && colour != Colour.YELLOW) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.BLUE && spinner.get() > 0 && colour != Colour.GREEN) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.YELLOW && spinner.get() < 0 && colour != Colour.RED) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.YELLOW && spinner.get() > 0 && colour != Colour.BLUE) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.RED && spinner.get() < 0 && colour != Colour.GREEN) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.RED && spinner.get() > 0 && colour != Colour.YELLOW) {
        System.out.println("Middle");
        return colourPrev;
      }
    }
      return colour;
  }
}