package frc.robot;

import java.util.function.Supplier;

import org.strongback.Strongback;
import org.strongback.SwitchReactor;
import org.strongback.components.Switch;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.InputDevice;
import frc.robot.Constants.LiftSetpoint;
import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.*;
import frc.robot.interfaces.ClimberInterface.ClimberAction;
import frc.robot.interfaces.HatchInterface.HatchAction;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.OperatorBoxButtons;
import frc.robot.subsystems.*;


public class OI implements OIInterface {

	private SwitchReactor reactor = Strongback.switchReactor();
	private Controller exec;
	private Log log;
	private Subsystems subsystems;

	// Used to switch the lift position buttons between setting to 
	// hatch positions or to cargo positions
	private boolean scoreModeCargo;

	public OI(Controller controller, Subsystems subsystems, Log log) {
		this.exec = controller;
		this.subsystems = subsystems;
		this.log = log;
		this.scoreModeCargo = false;

	}
    
	/*
	 * Gamepad button mappings
	 * =======================
	 * 
	 * Logitech controller looks like:
	 *   (left bumper)                (right bumper)
	 *   (left trigger)               (right trigger)
	 * 
	 *       ^        (back)   (start)      (Y)
	 *     < hat >                       (X)   (B)
	 *       \/       (mode)                (A)
	 *       
	 *      (left thumbstick)   (right thumbstick)
	 * 
	 * Driver Controls:
	 * Left Flight Joystick: Back/Forward
	 * Right Flight Joystick: Left/Right
	 * Left Joystick Trigger (Button 1): Half speed mode.
	 * 
	 * Operator Controls:
	 * 
	 * The hat vertical controls the lift height. Repeatedly pushing makes it go to the next height.
	 * The hat horizontal controls the sideways scoring.
	 * Pushing (A) starts intaking. Releasing it, stops intaking.
	 * Pushing (B) does an intake eject. Releasing it, stops ejecting.
	 * Pushing (left bumper) does a front eject (ie the outtake opens for the cube to fall out).
	 * Pushing both triggers deploys the ramp (safety)
	 * Pushing (Y) gets the robot ready for climbing.
	 * While (X) is held, the robot will climb. 
	 * (start) resets the robot lift at the bottom and intake stowed.
	 * 
	 * The following buttons are unused:
	 *  (back)(mode)(left bumper).
	 */
	public void configureJoysticks(FlightStick driverLeft, FlightStick driverRight, InputDevice operator) {
		// Left and RIght driver joysticks have separate mappings, as well as Operator controller.
		configureDriverJoystick(driverLeft, driverRight, "driverSticks");
    	configureOperatorJoystick(operator, "operator");
	}
		
	
	public void configureDriverJoystick(FlightStick leftStick, FlightStick rightStick, String name) {

		// Left Stick's onTrigger drive slowly mode is handled in Robot.java, not here
		// Hatch Vision
		// Intaking is on this button.
		onTriggered(leftStick.getButton(1), Sequences.startDriveByVision());
		onUntriggered(leftStick.getButton(1), Sequences.stopDriveByVision());

		// Intake - Right Stick Button 2 (on/off)
		onTriggered(rightStick.getButton(2), () -> {
			scoreModeCargo = true;
			sysoutScoreMode();
			return Sequences.startIntaking();
		}); 
		onUntriggered(rightStick.getButton(2), Sequences.stopIntaking());

		onTriggered(leftStick.getButton(6), Sequences.startSlowDriveForward());
		onUntriggered(leftStick.getButton(6), Sequences.setDrivebaseToArcade());

		onTriggered(rightStick.getButton(3), Sequences.turnToWall());  // Face the drivers station wall.
		onUntriggered(rightStick.getButton(3), Sequences.setDrivebaseToArcade());
	}

	public void configureOperatorJoystick(InputDevice stick, String name) {
		// Reset robot: intake stowed and lift at bottom.
		//TODO: update
		// onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), Sequences.getStartSequence());
		
		// Intake
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD),
		() -> {
			scoreModeCargo = true;
			sysoutScoreMode();
			return Sequences.startIntaking();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.stopIntaking());

		onTriggered(stick.getButton(GamepadButtonsX.BACK_BUTTON), Sequences.raiseIntake());
		
		// Deploy/retract lift. 
	/*	onTriggered(stick.getDPad(0, GamepadButtonsX.DPAD_NORTH), Sequences.liftDeploy());
		onUntriggered(stick.getDPad(0, GamepadButtonsX.DPAD_NORTH), Sequences.liftRetract());
*/
		// Spitter Sequence (cargoSpit) 
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.startCargoSpit());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.stopCargoSpit());

		// Reverse button
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.startReverseCycle());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.stopReverseCycle());

		// Hatch hold & release
		onTriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), () -> {
			scoreModeCargo = false;
			sysoutScoreMode();
			return Sequences.releaseHatch();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.holdHatch());

		// Hatch deploy/stow buttons.
/*		onTriggered(stick.getDPad(0,GamepadButtonsX.DPAD_WEST), Sequences.getReadyHatchSequence());			
		onTriggered(stick.getDPad(0,GamepadButtonsX.DPAD_EAST), Sequences.getStowHatchSequence());
		*/
		// Microadjust hatch left and right
		//whileTriggered(axisAsSwitch(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS)),
		//		() -> { return Sequences.getHatchDeltaPositionSequence(-1 * stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read()); });
				
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() >= 0.5;}, Sequences.setHatchPower(-0.5));
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() < 0.5 &&
			stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() > -0.5;}, Sequences.setHatchPower(0));
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() <= -0.5;}, Sequences.setHatchPower(0.5));


		onTriggered(stick.getButton(GamepadButtonsX.LEFT_THUMBSTICK_CLICK), Sequences.hatchCalibrate());		
		
		// Lift movement. The position is set by whether the OI is in cargo mode or hatch mode 
		/*onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), () -> { 
			sysoutScoreMode();
			return scoreModeCargo ? Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_BOTTOM_CARGO_HEIGHT)
								  : Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_BOTTOM_HATCH_HEIGHT);
		});

		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), () -> { 
			sysoutScoreMode();
			return scoreModeCargo ? Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_MIDDLE_CARGO_HEIGHT)
								  : Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_MIDDLE_HATCH_HEIGHT);
		});
		onTriggered(stick.getButton(GamepadButtonsX.Y_BUTTON), () -> { 
			sysoutScoreMode();
			return scoreModeCargo ? Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_TOP_CARGO_HEIGHT)
								  : Sequences.moveLift(LiftSetpoint.LIFT_ROCKET_TOP_HATCH_HEIGHT);
		});
		onTriggered(stick.getButton(GamepadButtonsX.B_BUTTON), () -> { 
			sysoutScoreMode();
			return scoreModeCargo ? Sequences.moveLift(LiftSetpoint.LIFT_CARGO_SHIP_CARGO_HEIGHT)
								  : Sequences.moveLift(LiftSetpoint.LIFT_CARGO_SHIP_HATCH_HEIGHT);
		});*/
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), () -> {
			scoreModeCargo = !scoreModeCargo;
			sysoutScoreMode();
		});
		onTriggered(stick.getDPad(0,GamepadButtonsX.DPAD_SOUTH), Sequences.moveLift(LiftSetpoint.LIFT_BOTTOM_HEIGHT));

		// Lift microadjust
		whileTriggered(() -> {
			return stick.getAxis(GamepadButtonsX.RIGHT_Y_AXIS).read() > 0.8;
		}, Sequences.getMicroAdjustDownSequence());
		whileTriggered(() -> {
			return stick.getAxis(GamepadButtonsX.RIGHT_Y_AXIS).read() < -0.8;
		}, Sequences.getMicroAdjustUpSequence());
	}

	private void sysoutScoreMode() {
		if (scoreModeCargo) {
			System.out.println("||||||||||||||| CARGO MODE |||||||||||||||");
		} else {
			System.out.println("+++++++++++++++ HATCH MODE +++++++++++++++");
		}
	}

 	@Override
	public void configureDiagBox(InputDevice box) {
		//Red1 and Red2 for left
		//Red3 to deploy climber
		//Yellow3 to climb
		//Yellow1 and Yellow2 for right
		//Yellow4 and Yellow5 for both

		onTriggered(box.getButton(OperatorBoxButtons.RED_BUTTON1), Sequences.startClimberLeftUp());
		onUntriggered(box.getButton(OperatorBoxButtons.RED_BUTTON1), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.RED_BUTTON2), Sequences.startClimberDown());
		onUntriggered(box.getButton(OperatorBoxButtons.RED_BUTTON2), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.RED_BUTTON3), Sequences.deployClimber());
		onUntriggered(box.getButton(OperatorBoxButtons.RED_BUTTON3), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON1), Sequences.startClimberRightUp());
		onUntriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON1), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON2), Sequences.startClimberRightDown());
		onUntriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON2), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON3), Sequences.climb());
		onUntriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON3), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON4), Sequences.startClimberUp());
		onUntriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON4), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON5), Sequences.startClimberDown());
		onUntriggered(box.getButton(OperatorBoxButtons.YELLOW_BUTTON5), Sequences.pauseClimber());

		onTriggered(box.getButton(OperatorBoxButtons.RED_BUTTON5), Sequences.stopClimber());

		//disabling stuff
		onTriggered(box.getButton(OperatorBoxButtons.RED_MANUAL), Sequences.overrideClimberLeft());
		onTriggered(box.getButton(OperatorBoxButtons.YELLOW_MANUAL), Sequences.overrideClimberRight());

		OverridableSubsystem<ClimberInterface> climberOverride = subsystems.climberOverride;
		// Get the interface that the diag box uses.
		ClimberInterface climberIF = climberOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.RED_DISABLE, OperatorBoxButtons.RED_MANUAL, subsystems.climberOverride);
	  // Override front stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.GREEN_BUTTON1), 
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_LEFT_HEIGHT,
				scaleClimbPotHeight(box.getAxis(OperatorBoxButtons.YELLOW_POT).read()))));
	  // Override rear stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.GREEN_BUTTON2), 
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_RIGHT_HEIGHT,
				scaleClimbPotHeight(box.getAxis(OperatorBoxButtons.RED_POT).read()))));
		// Override both front and rear stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.GREEN_BUTTON3),
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_BOTH_HEIGHT,
				scaleClimbPotHeight(box.getAxis(OperatorBoxButtons.GREEN_POT).read()))));	  

	}

	private double scaleClimbPotHeight(double value) {
		// Pot has a value of -1 to 1. Scale to 0 - max_height.
		return (value + 1) / 2 * Constants.CLIMBER_DEPLOY_HEIGHT;
	}

	private double scaleLiftPotHeight(double value) {
		// Pot has a value of -1 to 1. Scale to 0 - max_height.
		return (value + 1) / 2 * Constants.LIFT_DEFAULT_MAX_HEIGHT;
	}

	private void mapOverrideSwitch(InputDevice box, int disableButton, int manualButton, OverridableSubsystem overrideableSubsystem) {
		onTriggered(box.getButton(disableButton), () -> overrideableSubsystem.turnOff());
		onTriggered(box.getButton(manualButton), () -> overrideableSubsystem.setManualMode());
		onUntriggered(
					Switch.or(box.getButton(disableButton),
							  box.getButton(manualButton)),
					() -> overrideableSubsystem.setAutomaticMode());

	}

	
	/**
	 * Configure the rules for the user interfaces
	 */
	@SuppressWarnings("unused")
	private void onTriggered(Switch swtch, Sequence seq) {
		reactor.onTriggered(swtch, () -> exec.doSequence(seq));
	}
	
	@SuppressWarnings("unused")
	private void onTriggered(Switch swtch, Runnable func) {
		reactor.onTriggered(swtch, func);
	}

	@SuppressWarnings("unused")
	private void onTriggered(Switch swtch, Supplier<Sequence> func) {
		reactor.onTriggered(swtch, () -> exec.doSequence(func.get()));
	}

	@SuppressWarnings("unused")
	private void onUntriggered(Switch swtch, Sequence seq) {
		reactor.onUntriggered(swtch, () -> exec.doSequence(seq));
	}

	@SuppressWarnings("unused")
	private void onUntriggered(Switch swtch, Runnable func) {
		reactor.onUntriggered(swtch, func);
	}

	@SuppressWarnings("unused")
	private void onUntriggered(Switch swtch, Supplier<Sequence> func) {
		reactor.onUntriggered(swtch, () -> exec.doSequence(func.get()));
	}
	
	@SuppressWarnings("unused")
	private void whileTriggered(Switch swtch, Runnable func) {
		reactor.whileTriggered(swtch, func);
  }
	
	@SuppressWarnings("unused")
	private void whileTriggered(Switch swtch, Sequence seq) {
		reactor.whileTriggered(swtch, () -> exec.doSequence(seq));
  }
	
  @SuppressWarnings("unused")
  private void whileTriggered(Switch swtch, Supplier<Sequence> func) {
	  reactor.whileTriggered(swtch, () -> exec.doSequence(func.get()));
  }
}
