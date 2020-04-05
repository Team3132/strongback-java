package frc.robot.subsystems;

import java.util.function.Supplier;

import org.strongback.components.Clock;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Solenoid;

import frc.robot.Constants;
import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.ColourWheelType;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.LEDColour;
import frc.robot.lib.Subsystem;
import frc.robot.lib.WheelColour;

/**
 * This subsystem is made to spin the Colour Wheel on the control panel in the
 * 2020 game. It 5 seperate actions:
 * 
 * 1) Rotational control, spins the colour wheel 3.25 full rotations, or 26
 * eighth turns. It uses the colour wheel as an encoder, checking for every
 * colour.
 * 
 * 2) Positional control, spins the colour wheel to the selected colour,
 * choosing clockwise or anticlockwise depending on what is faster.
 * 
 * 3) Manual adjustment clockwise, moves the colour wheel clockwise at a slow
 * speed incase it is off by a bit.
 * 
 * 4) Manual adjustment anticlockwise, same as above, in the opposite direction.
 * 
 * 5) None, stops the motor. This is the default action.
 * 
 * This class expects to be given one motor and a RevRobotics Colour Sensor V3.
 */

public class ColourWheel extends Subsystem implements ColourWheelInterface {
  // Used in doubleCheck to check for mistakes with colour detection.
  private WheelColour colourPrev = WheelColour.UNKNOWN;
  private WheelColour colour = WheelColour.UNKNOWN; // Variable for what the colour sensor currently sees.
  private WheelColour nextColour = WheelColour.UNKNOWN;
  private int rotCount = 0; // Roation counter for rotation controls.
  // Variable to check if this is the first time the colour sensor saw the desired colour.
  private boolean firstLoop = true;
  private long spinTime; // Variable to store the time when the colour sensor sees the desired colour.
  private double speed = 0;
  // Default action for colour wheel subsystem.
  private ColourAction action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);

  private Clock clock;
  private final Motor motor;
  private Solenoid solenoid;
  private final LEDStripInterface ledStrip;
  private final Supplier<WheelColour> colourSensor;

  public ColourWheel(Motor motor, Solenoid solenoid, Supplier<WheelColour> colourSensor, LEDStripInterface ledStrip,
      Clock clock, DashboardInterface dash, Log log) {
    super("ColourWheel", dash, log);
    log.info("Creating Colour Wheel Subsystem");
    this.motor = motor;
    this.clock = clock;
    this.colourSensor = colourSensor;
    this.ledStrip = ledStrip;
    this.solenoid = solenoid;
    log.register(false, () -> (double) colour.id, "%s/colour", name)
        .register(false, () -> (double) rotCount, "%s/rotCount", name)
        .register(false, () -> (double) motor.getOutputPercent(), "%s/motorspeed", name)
        .register(false, () -> (double) nextColour.id, "%s/nextColour", name)
        .register(false, () -> (double) spinTime, "%s/spinTime", name);
  }

  public void update() {
    double newSpeed = 0;
    updateColour();
    switch (action.type) {
    case ROTATION:
      newSpeed = rotationalControl();
      ledStrip.setProgressColour(LEDColour.GREEN, LEDColour.YELLOW, ((double) rotCount) / Constants.COLOUR_WHEEL_ROTATION_TARGET);
      break;
    case POSITION:
      newSpeed = positionalControl(action.colour);
      ledStrip.setColour(colour.convert());
      break;
    case ADJUST_WHEEL_ANTICLOCKWISE:
      newSpeed = Constants.COLOUR_WHEEL_MOTOR_ADJUST;
      ledStrip.setColour(colour.convert());
      break;
    case ADJUST_WHEEL_CLOCKWISE:
      newSpeed = -Constants.COLOUR_WHEEL_MOTOR_ADJUST;
      ledStrip.setColour(colour.convert());
      break;
    case NONE:
      newSpeed = Constants.COLOUR_WHEEL_MOTOR_OFF;
      break;
    default:
      log.error("%s: Unknown Type %s", name, action.type);
      break;
    }
    if (newSpeed != speed) {
      motor.set(ControlMode.DutyCycle, newSpeed);
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
    if (colour.equals(nextColour)) {
      log.info("%s: Found %s.", name, colour);
      log.info("%s: Added one to rotations. %d", name, rotCount);
      rotCount += 1;
      firstLoop = true;
    }
    if (firstLoop) {
      nextColour = colour.next(speed);
      log.info("%s: Next Colour is %s.", name, nextColour);
      firstLoop = false;
    }
    if (rotCount < Constants.COLOUR_WHEEL_ROTATION_TARGET) {
      return Constants.COLOUR_WHEEL_MOTOR_FULL;
    } else {
      action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);
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
   * If colour wheel is on G, turn anticlockwise for R, and clockwise at full
   * speed for anything else.
   * 
   * If unknown, turn at current speed and direction until correct colour is
   * found.
   * 
   */
  public double positionalControl(WheelColour desired) {
    double newSpeed = (colour.id - desired.id) % 4; // Calculate new speed.
    if (newSpeed == 3) {
      newSpeed -= 4; // If above calculation is 3, set speed to -1.
    }
    if (newSpeed == -3) {
      newSpeed += 4; // If above calculation is -3, set speed to 1.
    }
    newSpeed = Constants.COLOUR_WHEEL_MOTOR_FULL * Math.signum(newSpeed);
    if (colour.equals(WheelColour.UNKNOWN)) { // Colour is unknown, move in current direcion until colour identified.
      if (speed != 0) {
        return speed;
      } else {
        return Constants.COLOUR_WHEEL_MOTOR_FULL;
      }
    }
    if (desired.equals(colour)) { // If correct colour found, move slowly to line up better and then stop.
      if (firstLoop) {
        spinTime = clock.currentTimeInMillis(); // Check time when correct colour found.
        firstLoop = false;
      }
      if (clock.currentTimeInMillis() - spinTime < Constants.COLOUR_WHEEL_DELAY) {
        return motor.get();
      } else {
        action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);
        log.info("ColourWheel: Desired colour found.");
        return Constants.COLOUR_WHEEL_MOTOR_OFF;
      }
    }
    return newSpeed;
  }

  public void updateColour() {
    colour = doubleCheck(colourSensor.get());
  }

  /**
   * 
   * Method to check if the current detected colour was a possible colour after
   * seeing the previous colour. Example: The colour sensor cannot see blue
   * directly after red, so it sets the detected colour back to blue and checks
   * again after the wheel has moved.
   * 
   */
  private WheelColour doubleCheck(WheelColour sensedColour) {
    if (speed == 0 || colourPrev.equals(WheelColour.UNKNOWN)) {
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
    if (action == new ColourAction(ColourWheelType.POSITION, WheelColour.UNKNOWN)) {
      log.error("%s: No colour found in FMS!", name);
      setDesiredAction(new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN));
    }
    firstLoop = true;
    rotCount = 0;
    nextColour = WheelColour.UNKNOWN;
    return this;
  }

  @Override
  public ColourAction getDesiredAction() {
    return action;
  }

  @Override
  public boolean isFinished() {
    return action.type == ColourWheelType.NONE;
  }

  @Override
  public void disable() {
    motor.set(ControlMode.DutyCycle, 0);
    action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);
  }

  @Override
  public void updateDashboard() {
    dashboard.putString("Colourwheel Desired colour", action.colour.toString());
    dashboard.putString("Colourwheel Current colour", colour.toString());
    dashboard.putNumber("Colourwheel Motor", motor.get());
    dashboard.putString("Colourwheel Action", action.type.toString());
    dashboard.putNumber("Colourwheel Rotations", rotCount);
    dashboard.putNumber("Colourwheel spinTime", spinTime);
    dashboard.putNumber("Colourwheel realTime", clock.currentTimeInMillis());
  }

  @Override
  public void setArmExtended(boolean extended) {
    if (extended) {
      solenoid.extend();
    } else {
      solenoid.retract();
    }
  }

  @Override
  public boolean isArmExtended() {
    return solenoid.isExtended();
  }

  @Override
  public boolean isArmRetracted() {
    return solenoid.isRetracted();
  }
}