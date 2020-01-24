package frc.robot.subsystems;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import edu.wpi.first.wpilibj.util.Color;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.Type;
import frc.robot.lib.Subsystem;

public class ColourWheel extends Subsystem implements ColourWheelInterface {

  private Colour colourPrev = Colour.UNKNOWN;
  private Colour colour;
  private Colour startColour;
  private Colour pairColour;
  private int rotCount;

  private ColourAction action = new ColourAction(Type.NONE, Colour.UNKNOWN);

  private final Motor motor;

  // Values callibrated using vynl sticker for control panel.
  private final Color BlueTarget = ColorMatch.makeColor(0.147, 0.437, 0.416);
  private final Color GreenTarget = ColorMatch.makeColor(0.189, 0.559, 0.250);
  private final Color RedTarget = ColorMatch.makeColor(0.484, 0.366, 0.150);
  private final Color YellowTarget = ColorMatch.makeColor(0.322, 0.546, 0.131);
  private final Color WhiteTarget = ColorMatch.makeColor(0.276, 0.587, 0.217);

  private final ColorSensorV3 colourSensor;
  /**
   * A Rev Color Match object is used to register and detect known colors. This
   * can be calibrated ahead of time or during operation.
   * 
   * This object uses a simple euclidian distance to estimate the closest match
   * with given confidence range.
   */
  private final ColorMatch colourMatcher = new ColorMatch();

  private double speed = 0;

  public ColourWheel(Motor motor, ColorSensorV3 colourSensor, DashboardInterface dash, Log log) {
    super("ColourWheel", dash, log);
    colourMatcher.addColorMatch(BlueTarget);
    colourMatcher.addColorMatch(GreenTarget);
    colourMatcher.addColorMatch(RedTarget);
    colourMatcher.addColorMatch(YellowTarget);
    colourMatcher.addColorMatch(WhiteTarget);
    this.motor = motor;
    this.colourSensor = colourSensor;
  }

  @Override
  public void execute(long timeInMillis) {
    double newSpeed = 0;
    switch (action.type) {
    case ROTATION:
      newSpeed = rotate3_5();
      break;
    case POSITION:
      newSpeed = positionalControl(action.colour);
      break;
    case NONE:
      newSpeed = 0;
      break;
    default:
      log.error("%s: Unknown Type %s", name, action.type);
      break;
    }
    if (newSpeed != speed) {
      motor.set(ControlMode.PercentOutput, newSpeed);
      speed = newSpeed;
    }
  }

  public double rotate3_5() {
    updateColour();
    if (rotCount < 0) {
      startColour = colour;
      switch (startColour) {
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
      rotCount = 0;
    }
    if ((rotCount % 2 == 0 && colour == pairColour) || (rotCount % 2 != 0 && colour == startColour)) {
      rotCount += 1;
    }
    if (rotCount < 14) { // 3.5 rotations == 14 quarter rotations
      if (rotCount > 12) {
        return -0.5;
      } else {
        return -1;
      }
    } else {
      rotCount = -1;
      action = new ColourAction(Type.NONE, Colour.UNKNOWN);
      return 0;
    }
  }

  public double positionalControl(Colour desired) {
    updateColour();
    if (desired == colour || desired == Colour.UNKNOWN) {
      action = new ColourAction(Type.NONE, Colour.UNKNOWN);
      return 0;
    }
    if (colour == Colour.RED && desired == Colour.YELLOW) {
      return -0.5;
    }
    if (colour == Colour.YELLOW && desired == Colour.BLUE) {
      return -0.5;
    }
    if (colour == Colour.BLUE && desired == Colour.GREEN) {
      return -0.5;
    }
    if (colour == Colour.GREEN && desired == Colour.RED) {
      return -0.5;
    }
    if (colour == Colour.YELLOW && desired == Colour.RED) {
      return 0.5;
    }
    if (colour == Colour.BLUE && desired == Colour.YELLOW) {
      return 0.5;
    }
    if (colour == Colour.GREEN && desired == Colour.BLUE) {
      return 0.5;
    }
    if (colour == Colour.RED && desired == Colour.GREEN) {
      return 0.5;
    }
    if (colour == Colour.UNKNOWN) {
      return speed;
    }
    return 1;
  }

  public void updateColour() {
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
  }

  public Colour doubleCheck() {
    if (colour != colourPrev) {
      if (colourPrev == Colour.GREEN && motor.get() < 0 && colour != Colour.BLUE) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.GREEN && motor.get() > 0 && colour != Colour.RED) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.BLUE && motor.get() < 0 && colour != Colour.YELLOW) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.BLUE && motor.get() > 0 && colour != Colour.GREEN) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.YELLOW && motor.get() < 0 && colour != Colour.RED) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.YELLOW && motor.get() > 0 && colour != Colour.BLUE) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.RED && motor.get() < 0 && colour != Colour.GREEN) {
        System.out.println("Middle");
        return colourPrev;
      }
      if (colourPrev == Colour.RED && motor.get() > 0 && colour != Colour.YELLOW) {
        System.out.println("Middle");
        return colourPrev;
      }
    }
    return colour;
  }

  @Override
  public ColourWheelInterface setDesiredAction(ColourAction action) {
    this.action = action;
    return this;
  }

  @Override
  public ColourAction getDesiredAction() {
    return action;
  }

  @Override
  public boolean isFinished() {
    return action.type == Type.NONE;
  }
}