package frc.robot.subsystems;

import com.revrobotics.ColorMatch;
import com.revrobotics.ColorMatchResult;
import com.revrobotics.ColorSensorV3;

import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import edu.wpi.first.wpilibj.util.Color;

import frc.robot.Constants;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.Type;
import frc.robot.lib.Subsystem;

/**
 * This subsystem is made to spin the Colour Wheel on the control panel in the 2020 game.
 * It 5 seperate actions:
 *   1) Rotational control, spins the colour wheel 3.5 full rotations, or 14 quater turns.
 *      It uses the colour wheel as an encoder, checking for every second colour.
 *   2) Positional control, spins the colour wheel to the selected colour,
 *      choosing clockwise or anticlockwise depending on what is faster.
 *   3) Manual adjustment clockwise, moves the colour wheel clockwise at a slow speed incase it is off by a bit.
 *   4) Manual adjustment anticlockwise, same as above, in the opposite direction.
 *   5) None, stops the motor. This is the default action.
 * 
 * This class expects to be given one motor and a RevRobotics Colour Sensor V3.
 */

public class ColourWheel extends Subsystem implements ColourWheelInterface {

  private Colour colourPrev = Colour.UNKNOWN; //Used in doubleCheck to check for mistakes with colour detection.
  private Colour colour = Colour.UNKNOWN; //Variable for what the colour sensor currently sees.
  private Colour startColour; 
  private Colour pairColour; //Pair and start colour are used for rotational controls to see what colours to check for.
  private int rotCount = -1; //Roation counter for rotation controls.
  private boolean firstLoop = true; //Variable to check if this is the first time the colour sensor saw the desired colour.
  private long spinTime; //Variable to store the time when the colour sensor sees the desired colour.
  private double speed = 0;
  private ColourAction action = new ColourAction(Type.NONE, Colour.UNKNOWN); //Default action for colour wheel subsystem.

  private final Motor motor;
  private final ColorSensorV3 colourSensor;

  /**
   * A Rev Color Match object is used to register and detect known colors. This
   * can be calibrated ahead of time or during operation.
   * 
   * This object uses a simple euclidian distance to estimate the closest match
   * with given confidence range.
   */
  private final ColorMatch colourMatcher = new ColorMatch();

  public ColourWheel(Motor motor, ColorSensorV3 colourSensor, DashboardInterface dash, Log log) {
    super("ColourWheel", dash, log);
    log.info("Creating Colour Wheel Subsystem");
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_BLUE_VALUE); //Adding colours to the colourMatcher
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_GREEN_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_RED_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_YELLOW_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_WHITE_TARGET);
    this.motor = motor;
    this.colourSensor = colourSensor;
  }

  @Override
  public void execute(long timeInMillis) {
    double newSpeed = 0;
    updateColour();
    switch (action.type) {
    case ROTATION:
      newSpeed = rotate3_5();
      break;
    case POSITION:
      newSpeed = positionalControl(action.colour);
      break;
    case ADJUST_WHEEL_ANTICLOCKWISE:
      newSpeed = Constants.COLOUR_WHEEL_MOTOR_ADJUST;
      break;
    case ADJUST_WHEEL_CLOCKWISE:
      newSpeed = -Constants.COLOUR_WHEEL_MOTOR_ADJUST;
      break;
    case NONE:
      newSpeed = Constants.COLOUR_WHEEL_MOTOR_OFF;
      break;
    default:
      log.error("%s: Unknown Type %s", name, action.type);
      break;
    }
    if (newSpeed != speed) {
      motor.set(ControlMode.PercentOutput, newSpeed);
      log.info("%s: set new speed %.2f", name, newSpeed);
      speed = newSpeed;
    }
  }

  /**
  *       _________________
  *      /\        |       /\
  *     /  \       |      /  \
  *    /    \  R   |  Y  /    \
  *   /      \     |    /      \
  *  /   G    \    |   /    B   \
  * /          \   |  /          \
  * |           \  | /           |
  * |_____________\|/____________|
  * |             /|\            |
  * \            / | \           /
  *  \      B   /  |  \     G   /
  *   \        /   |   \       /
  *    \      /    |    \     /
  *     \    /   Y |  R  \   /
  *      \  /      |      \ /
  *       \/_______|______\/
  * 
  * If colour wheel is on G, spin for 14 quater turns by checking when passing G and B.
  * At 12 rotations, go to half speed to slow down in time.
  * 
  * If unknown, turn at slow speed and start again.
  * 
  */
  public double rotate3_5() {
    if (rotCount < 0) { //First run through checks the current colour and sets the colour 2 tiles across to the pair colour.
      startColour = colour;
      if (startColour == Colour.UNKNOWN) {
        return -Constants.COLOUR_WHEEL_MOTOR_ADJUST;
      }
      pairColour = Colour.of((startColour.id + 2) % 4);
      rotCount = 0;
    }
    if ((rotCount % 2 == 0 && colour == pairColour) || (rotCount % 2 != 0 && colour == startColour)) {
      rotCount += 1; //Checks if it is expecting the start or pair colour and adds one if it finds it.
    }
    if (rotCount < 14) { // 3.5 rotations == 14 quarter rotations
      if (rotCount > 12) {
        return -Constants.COLOUR_WHEEL_MOTOR_HALF; //Slows down with 0.5 full rotations left.
      } else {
        return -Constants.COLOUR_WHEEL_MOTOR_FULL;
      }
    } else {
      rotCount = -1; //Reset rotation count and action.
      action = new ColourAction(Type.NONE, Colour.UNKNOWN);
      return Constants.COLOUR_WHEEL_MOTOR_OFF;
    }
  }

  /**
  *       _________________
  *      /\        |       /\
  *     /  \       |      /  \
  *    /    \  R   |  Y  /    \
  *   /      \     |    /      \
  *  /   G    \    |   /    B   \
  * /          \   |  /          \
  * |           \  | /           |
  * |_____________\|/____________|
  * |             /|\            |
  * \            / | \           /
  *  \      B   /  |  \     G   /
  *   \        /   |   \       /
  *    \      /    |    \     /
  *     \    /   Y |  R  \   /
  *      \  /      |      \ /
  *       \/_______|______\/
  * 
  * If colour wheel is on G, turn anticlockwise at half speed,
  * clockwise at half speed for B and clockwise at full speed for anything else. 
  * 
  * If unknown, turn at current speed and direction until correct colour is found.
  * 
  */
  public double positionalControl(Colour desired) {
    double newSpeed = (colour.id - desired.id) % 4; //Calculate new speed.
    if (newSpeed > 1) newSpeed -= 4; //If above calculation is 3, set speed to 1.
    newSpeed /= 2;
    if (colour == Colour.UNKNOWN) { //Colour is unknown, move in current direcion until colour identified.
      if(speed != 0) {
        return speed;
      } else {
        return Constants.COLOUR_WHEEL_MOTOR_HALF;
      }
    }
    if (desired == colour) { //If correct colour found, move slowly to line up better and then stop.
      if (firstLoop == true) {
        spinTime = System.currentTimeMillis(); //Check time when correct colour found.
        if (motor.get() > 0) { //Move at slow speed in current direction.
          firstLoop = false;
          return Constants.COLOUR_WHEEL_MOTOR_ADJUST;
        } else {
          firstLoop = false;
          return -Constants.COLOUR_WHEEL_MOTOR_ADJUST;
        }
      } 
      if (System.currentTimeMillis() - spinTime < 500) { //Check if 0.5 seconds has passed.
        return motor.get();
      } else {
        action = new ColourAction(Type.NONE, Colour.UNKNOWN);
        firstLoop = true;
        log.info("ColourWheel: Desired colour found.");
        return Constants.COLOUR_WHEEL_MOTOR_OFF;
      }
    }
    
    return newSpeed;
  }

  public void updateColour() {
    Color detectedColor = colourSensor.getColor();
    ColorMatchResult match = colourMatcher.matchClosestColor(detectedColor);
    if (match.color == Constants.COLOUR_WHEEL_BLUE_VALUE) {
      colour = Colour.BLUE;
    } else if (match.color == Constants.COLOUR_WHEEL_RED_TARGET) {
      colour = Colour.RED;
    } else if (match.color == Constants.COLOUR_WHEEL_GREEN_TARGET) {
      colour = Colour.GREEN;
    } else if (match.color == Constants.COLOUR_WHEEL_YELLOW_TARGET) {
      colour = Colour.YELLOW;
    } else {
      colour = Colour.UNKNOWN;
    }
    colour = doubleCheck();
  }

  /**
   * 
   * Method to check if the current detected colour
   * was a possible colour after seeing the previous colour.
   * Example: The colour sensor cannot see blue directly after red,
   *          so it sets the detected colour back to blue and checks
   *          again after the wheel has moved.
   * 
  */
  private Colour doubleCheck() {
    if (speed == 0 || colourPrev == Colour.UNKNOWN) {
      colourPrev = colour;
      return colour;
    }
    if (colour == colourPrev) return colourPrev;
    int direction = speed < 0 ? 1 : -1; //Get direction
    int newColour = (colourPrev.id + direction) % 4;
    if (newColour == colour.id) colourPrev = colour;
    log.sub("%s: p%d c%d a%b", name, colourPrev.id, colour.id, newColour == colour.id);
    return colourPrev;
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
    //log.info("isFinished Colour Wheel Action: %s", action);
    return action.type == Type.NONE;
  }

  @Override
	public void updateDashboard() {
    updateColour();
    dashboard.putString("Desired colour", action.colour.toString());
    dashboard.putString("Current colour", colour.toString());
    dashboard.putNumber("Colour wheel motor", motor.get());
    dashboard.putString("Colour wheel action", action.type.toString());
    dashboard.putNumber("Colour wheel rotations", rotCount);
    dashboard.putNumber("spinTime", spinTime);
    dashboard.putNumber("realTime", System.currentTimeMillis());
	}
}