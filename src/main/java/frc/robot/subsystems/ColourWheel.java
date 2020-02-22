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
import frc.robot.interfaces.NetworkTableHelperInterface;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.Type;
import frc.robot.lib.Subsystem;

/**
 * This subsystem is made to spin the Colour Wheel on the control panel in the 2020 game.
 * It 5 seperate actions:
 *   1) Rotational control, spins the colour wheel 3.25 full rotations, or 26 eighth turns.
 *      It uses the colour wheel as an encoder, checking for every colour.
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
  private Colour nextColour = Colour.UNKNOWN;
  private int rotCount = 0; //Roation counter for rotation controls.
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

  public ColourWheel(Motor motor, ColorSensorV3 colourSensor, NetworkTableHelperInterface networkTable,DashboardInterface dash, Log log) {
    super("ColourWheel", networkTable, dash, log);
    log.info("Creating Colour Wheel Subsystem");
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_BLUE_TARGET); //Adding colours to the colourMatcher
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_GREEN_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_RED_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_YELLOW_TARGET);
    colourMatcher.addColorMatch(Constants.COLOUR_WHEEL_WHITE_TARGET);
    this.motor = motor;
    this.colourSensor = colourSensor;
    log.register(false, () -> (double) colour.id, "%s/colour", name)
       .register(false, () -> (double) rotCount, "%s/rotCount", name)
       .register(false, () -> (double) motor.getOutputPercent(), "%s/motorspeed", name)
       .register(false, () -> (double) nextColour.id, "%s/nextColour", name)
       .register(false, () -> (double) spinTime, "%s/spinTime", name);
  }

  @Override
  public void execute(long timeInMillis) {
    double newSpeed = 0;
    updateColour();
    switch (action.type) {
    case ROTATION:
      newSpeed = rotationalControl();
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
  * If colour wheel is on G, spin for 26 eighth turns by checking each colour
  * 
  * If unknown, turn at slow speed and start again.
  * 
  */
  public double rotationalControl() {
    if (firstLoop) {
      nextColour = colour.next(speed);
      log.info("%s: Next Colour is %s.", name, nextColour);
      firstLoop = false;
    }
    if (colour.equals(nextColour)) {
      log.info("%s: Found %s.", name, colour);
      log.info("%s: Added one to rotations. %d", name, rotCount);
      rotCount += 1;
      firstLoop = true;
    }
    if (rotCount < (3*8 + 2)) {
      return Constants.COLOUR_WHEEL_MOTOR_FULL;
    } else {
      rotCount = 0; //Reset rotation count and action.
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
  * If colour wheel is on G, turn anticlockwise for R,
  * and clockwise at full speed for anything else.
  * 
  * If unknown, turn at current speed and direction until correct colour is found.
  * 
  */
  public double positionalControl(Colour desired) {
    double newSpeed = (colour.id - desired.id) % 4; //Calculate new speed.
    if (newSpeed == 3) newSpeed -= 4; //If above calculation is 3, set speed to -1.
    if (newSpeed == -3) newSpeed += 4; //If above calculation is 3, set speed to -1.
    newSpeed = Constants.COLOUR_WHEEL_MOTOR_FULL * Math.signum(newSpeed);
    if (colour.equals(Colour.UNKNOWN)) { //Colour is unknown, move in current direcion until colour identified.
      if(speed != 0) {
        return speed;
      } else {
        return Constants.COLOUR_WHEEL_MOTOR_FULL;
      }
    }
    if (desired.equals(colour)) { //If correct colour found, move slowly to line up better and then stop.
      if (firstLoop == true) {

        spinTime = System.currentTimeMillis(); //Check time when correct colour found.
        firstLoop = false;
      } 
      if (System.currentTimeMillis() - spinTime < 50) { //Check if 50 milliseconds has passed.
        return motor.get();
      } else {
        action = new ColourAction(Type.NONE, Colour.UNKNOWN);
        log.info("ColourWheel: Desired colour found.");
        return Constants.COLOUR_WHEEL_MOTOR_OFF;
      }
    }
    return newSpeed;
  }

  public void updateColour() {
    Color detectedColor = colourSensor.getColor();
    ColorMatchResult match = colourMatcher.matchClosestColor(detectedColor);
    Colour sensedColour = Colour.UNKNOWN;
    if (match.color == Constants.COLOUR_WHEEL_BLUE_TARGET) {
      sensedColour = Colour.BLUE;
    } else if (match.color == Constants.COLOUR_WHEEL_RED_TARGET) {
      sensedColour = Colour.RED;
    } else if (match.color == Constants.COLOUR_WHEEL_GREEN_TARGET) {
      sensedColour = Colour.GREEN;
    } else if (match.color == Constants.COLOUR_WHEEL_YELLOW_TARGET) {
      sensedColour = Colour.YELLOW;
    } else {
      sensedColour = Colour.UNKNOWN;
    }
    colour = doubleCheck(sensedColour);
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
  private Colour doubleCheck(Colour sensedColour) {
    if (speed == 0 || colourPrev.equals(Colour.UNKNOWN)) {
      colourPrev = sensedColour;
      return sensedColour;
    }
    if (sensedColour.equals(colourPrev.next(speed))) {
      colourPrev = sensedColour;
    }
    return colourPrev;
  }

  @Override
  public ColourWheelInterface setDesiredAction(ColourAction action) {
    this.action = action;
    firstLoop = true;
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
    dashboard.putString("Colourwheel Desired colour", action.colour.toString());
    dashboard.putString("Colourwheel Current colour", colour.toString());
    dashboard.putNumber("Colourwheel Motor", motor.get());
    dashboard.putString("Colourwheel Action", action.type.toString());
    dashboard.putNumber("Colourwheel Rotations", rotCount);
    dashboard.putNumber("Colourwheel spinTime", spinTime);
    dashboard.putNumber("Colourwheel realTime", System.currentTimeMillis());
	}
}