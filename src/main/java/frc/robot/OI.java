package frc.robot;

import java.util.function.Supplier;

import org.strongback.Strongback;
import org.strongback.SwitchReactor;
import org.strongback.components.Switch;
import org.strongback.components.ui.FlightStick;
import org.strongback.components.ui.InputDevice;
import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.*;
import frc.robot.interfaces.ClimberInterface.ClimberAction;
import frc.robot.interfaces.ColourWheelInterface.Colour;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.OperatorBoxButtons;
import frc.robot.subsystems.*;


public class OI implements OIInterface {

	private SwitchReactor reactor = Strongback.switchReactor();
	private Controller exec;
	private Log log;
	private Subsystems subsystems;


	public OI(Controller controller, Subsystems subsystems, Log log) {
		this.exec = controller;
		this.subsystems = subsystems;
		this.log = log;

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
		onTriggered(rightStick.getButton(2), Sequences.startIntaking());
		
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
			return Sequences.startIntaking();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.stopIntaking());
	
		
		 
		/* Using these buttons for colour wheel while testing.
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), () -> { 
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
		});
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), () -> {
			scoreModeCargo = !scoreModeCargo;
			sysoutScoreMode();
		}); */

		//Colour Wheel testing.
		onTriggered(stick.getButton(GamepadButtonsX.Y_BUTTON), Sequences.colourWheelPositional(Colour.YELLOW));
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.colourWheelPositional(Colour.BLUE));
		onTriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.colourWheelPositional(Colour.RED));
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.colourWheelPositional(Colour.GREEN));
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), Sequences.colourWheelRotational());
		onTriggered(stick.getButton(GamepadButtonsX.BACK_BUTTON), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.colourWheelLeft());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.colourWheelRight());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.stopColourWheel());


	}


 	@Override
	public void configureDiagBox(InputDevice box) {

	}
		/*
		// Intake
		onTriggered(box.getButton(OperatorBoxButtons.RED1), Sequences.startIntakingOnly());
		onUntriggered(box.getButton(OperatorBoxButtons.RED1), Sequences.stopIntakingOnly());
		
		// Test passthrough (this is temporary)
		onTriggered(box.getButton(OperatorBoxButtons.RED3), Sequences.startPassthrough());
		onUntriggered(box.getButton(OperatorBoxButtons.RED3), Sequences.stopPassthrough());

		// Climber overrides.
		OverridableSubsystem<ClimberInterface> climberOverride = subsystems.climberOverride;
		// Get the interface that the diag box uses.
		ClimberInterface climberIF = climberOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.CLIMBER_DISABLE, OperatorBoxButtons.CLIMBER_MANUAL, climberOverride);
	  // Override front stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.CLIMBER_FRONT_HEIGHT), 
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_FRONT_HEIGHT,
				scaleStiltsPotHeight(box.getAxis(OperatorBoxButtons.CLIMBER_POT).read()))));
	  // Override rear stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.CLIMBER_REAR_HEIGHT), 
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_REAR_HEIGHT,
				scaleStiltsPotHeight(box.getAxis(OperatorBoxButtons.CLIMBER_POT).read()))));
		// Override both front and rear stilts height.
		whileTriggered(box.getButton(OperatorBoxButtons.CLIMBER_BOTH_HEIGHT),
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_BOTH_HEIGHT,
				scaleStiltsPotHeight(box.getAxis(OperatorBoxButtons.CLIMBER_POT).read()))));
	  // Override stilts driving power.
		whileTriggered(box.getButton(OperatorBoxButtons.CLIMBER_DRIVE_SPEED), 
			() -> climberIF.setDesiredAction(
				new ClimberAction(ClimberAction.Type.SET_DRIVE_SPEED,
				box.getAxis(OperatorBoxButtons.CLIMBER_POT).read())));

		// Intake overrides.
		OverridableSubsystem<IntakeInterface> intakeOverride = subsystems.intakeOverride;
		// Get the interface that the diag box uses.
		IntakeInterface intakeIF = intakeOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.INTAKE_DISABLE, OperatorBoxButtons.INTAKE_MANUAL, intakeOverride);
	  // While the intake speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.INTAKE_MOTOR), 
			() -> intakeIF.setMotorOutput(box.getAxis(OperatorBoxButtons.INTAKE_POT).read()));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_EXTEND), 
			() -> intakeIF.setExtended(true));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_RETRACT), 
			() -> intakeIF.setExtended(false));

		// Passthrough overrides.
		OverridableSubsystem<PassthroughInterface> passthroughOverride = subsystems.passthroughOverride;
		// Get the interface that the diag box uses.
		PassthroughInterface passthroughIF = passthroughOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.PASSTHRU_DISABLE, OperatorBoxButtons.PASSTHRU_MANUAL, passthroughOverride);
	  // While the passthrough speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.PASSTHRU_MOTOR), 
			() -> passthroughIF.setTargetMotorOutput(box.getAxis(OperatorBoxButtons.PASSTHRU_POT).read()));

		
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
