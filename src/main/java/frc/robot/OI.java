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
import frc.robot.interfaces.ColourWheelInterface.Colour;
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

		// tell the climber to go back down
		// Right Stick
		onTriggered(rightStick.getButton(5), Sequences.abortLevelStage());
		onUntriggered(rightStick.getButton(5), Sequences.stopLevelNclimb());

		// Level 3 sequence of buttons
		/*onTriggered(rightStick.getButton(12), Sequences.startLevel3climb());
		onUntriggered(rightStick.getButton(12), Sequences.stopLevelNclimb());

		onTriggered(rightStick.getButton(10), Sequences.startLevelDriveForward());
		onUntriggered(rightStick.getButton(10), Sequences.stopLevelDrive());

		onTriggered(rightStick.getButton(8), Sequences.startRearRaise());
		onUntriggered(rightStick.getButton(8), Sequences.stopLevelNclimb());

		onTriggered(leftStick.getButton(12), Sequences.startLevelDriveForward());
		onUntriggered(leftStick.getButton(12), Sequences.stopLevelDrive());*/

		onTriggered(leftStick.getButton(10), Sequences.startFrontRaise());
		onUntriggered(leftStick.getButton(10), Sequences.stopLevelNclimb());

		onTriggered(leftStick.getButton(8), Sequences.startLevelDriveForward());
		onUntriggered(leftStick.getButton(8), Sequences.stopLevelDrive());

		// Level 2 sequence of buttons

		onTriggered(rightStick.getButton(11), Sequences.startLevel2climb());
		onUntriggered(rightStick.getButton(11), Sequences.stopLevelNclimb());

		onTriggered(rightStick.getButton(9), Sequences.startLevelDriveForward());
		onUntriggered(rightStick.getButton(9), Sequences.stopLevelDrive());

		onTriggered(rightStick.getButton(7), Sequences.startRearRaise());
		onUntriggered(rightStick.getButton(7), Sequences.stopLevelNclimb());

		onTriggered(leftStick.getButton(11), Sequences.startLevelDriveForward());
		onUntriggered(leftStick.getButton(11), Sequences.stopLevelDrive());

		onTriggered(leftStick.getButton(9), Sequences.startFrontRaise());
		onUntriggered(leftStick.getButton(9), Sequences.stopLevelNclimb());

		onTriggered(leftStick.getButton(7), Sequences.startLevelDriveForward());
		onUntriggered(leftStick.getButton(7), Sequences.stopLevelDrive());

		//onTriggered(rightStick.getButton(9), Sequences.startFrontRaise());
		//onUntriggered(rightStick.getButton(9), Sequences.stopLevelNclimb());

		//onTriggered(rightStick.getButton(11), Sequences.startRearRaise());
		//onUntriggered(rightStick.getButton(11), Sequences.stopLevelNclimb());


		//onTriggered(leftStick.getButton(9), Sequences.startLevelDriveForward());
		//onUntriggered(leftStick.getButton(9), Sequences.stopLevelDrive());

		//onTriggered(leftStick.getButton(11), Sequences.startLevelDriveBackward());
		//onUntriggered(leftStick.getButton(11), Sequences.stopLevelDrive());
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

		//onTriggered(stick.getButton(GamepadButtonsX.BACK_BUTTON), Sequences.raiseIntake());
		
		// Deploy/retract lift. 
		onTriggered(stick.getDPad(0, GamepadButtonsX.DPAD_NORTH), Sequences.liftDeploy());
		onUntriggered(stick.getDPad(0, GamepadButtonsX.DPAD_NORTH), Sequences.liftRetract());

		/*
		// Spitter Sequence (cargoSpit) 
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.startCargoSpit());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.stopCargoSpit());

		// Reverse button
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.startReverseCycle());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.stopReverseCycle());
		*/

		// Hatch hold & release
		onTriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), () -> {
			scoreModeCargo = false;
			sysoutScoreMode();
			return Sequences.releaseHatch();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.holdHatch());

		// Hatch deploy/stow buttons.
		onTriggered(stick.getDPad(0,GamepadButtonsX.DPAD_WEST), Sequences.getReadyHatchSequence());			
		onTriggered(stick.getDPad(0,GamepadButtonsX.DPAD_EAST), Sequences.getStowHatchSequence());
		
		// Microadjust hatch left and right
		//whileTriggered(axisAsSwitch(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS)),
		//		() -> { return Sequences.getHatchDeltaPositionSequence(-1 * stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read()); });
				
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() >= 0.5;}, Sequences.setHatchPower(-0.5));
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() < 0.5 &&
			stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() > -0.5;}, Sequences.setHatchPower(0));
		onTriggered(() -> { return stick.getAxis(GamepadButtonsX.LEFT_X_AXIS).read() <= -0.5;}, Sequences.setHatchPower(0.5));


		onTriggered(stick.getButton(GamepadButtonsX.LEFT_THUMBSTICK_CLICK), Sequences.hatchCalibrate());		
		
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_THUMBSTICK_CLICK), Sequences.startShooting());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_THUMBSTICK_CLICK), Sequences.stopShooting());

		// Lift movement. The position is set by whether the OI is in cargo mode or hatch mode 
		/**onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), () -> { 
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
		});*/
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), () -> {
			scoreModeCargo = !scoreModeCargo;
			sysoutScoreMode();
		});

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
		/*
		// Intake
		onTriggered(box.getButton(OperatorBoxButtons.RED1), Sequences.startIntakingOnly());
		onUntriggered(box.getButton(OperatorBoxButtons.RED1), Sequences.stopIntakingOnly());

		// Spitter 
		onTriggered(box.getButton(OperatorBoxButtons.RED2), Sequences.startSpitterOnly());
		onUntriggered(box.getButton(OperatorBoxButtons.RED2), Sequences.stopSpitterOnly());

		
		// Test passthrough (this is temporary)
		onTriggered(box.getButton(OperatorBoxButtons.RED3), Sequences.startPassthrough());
		onUntriggered(box.getButton(OperatorBoxButtons.RED3), Sequences.stopPassthrough());

		
		// Lift movement. Multiple presses move up through configured stops.
		onTriggered(box.getButton(OperatorBoxButtons.RED4), Sequences.liftSetpointUp());
		onTriggered(box.getButton(OperatorBoxButtons.RED5), Sequences.liftSetpointDown());
		
		// Hatch deploy/stow buttons.
		onTriggered(box.getButton(OperatorBoxButtons.YELLOW1), Sequences.getReadyHatchSequence());
		onTriggered(box.getButton(OperatorBoxButtons.YELLOW2), Sequences.getStowHatchSequence());

		// Lift deploy/stow buttons.
		onTriggered(box.getButton(OperatorBoxButtons.YELLOW3), Sequences.liftDeploy());
		onTriggered(box.getButton(OperatorBoxButtons.YELLOW4), Sequences.liftRetract());

		// Level three
		// Extend overrides
		final double l3ExtendPower = 0.25;
		onTriggered(box.getButton(OperatorBoxButtons.GREEN1), Sequences.setL3LeftFrontStiltsPowerOverride(l3ExtendPower));
		onUntriggered(box.getButton(OperatorBoxButtons.GREEN1), Sequences.setL3LeftFrontStiltsPowerOverride(0));
		onTriggered(box.getButton(OperatorBoxButtons.BLUE2), Sequences.setL3RightFrontStiltsPowerOverride(l3ExtendPower));
		onUntriggered(box.getButton(OperatorBoxButtons.BLUE2), Sequences.setL3RightFrontStiltsPowerOverride(0));
		onTriggered(box.getButton(OperatorBoxButtons.CLEAR), Sequences.setL3RearStiltsPowerOverride(l3ExtendPower));
		onUntriggered(box.getButton(OperatorBoxButtons.CLEAR), Sequences.setL3RearStiltsPowerOverride(0));
		// Retract overrides
		final double l3RetractPower = -0.5;
		onTriggered(box.getButton(OperatorBoxButtons.BLUE1), Sequences.setL3LeftFrontStiltsPowerOverride(l3RetractPower));
		onUntriggered(box.getButton(OperatorBoxButtons.BLUE1), Sequences.setL3LeftFrontStiltsPowerOverride(0));
		onTriggered(box.getButton(OperatorBoxButtons.GREEN2), Sequences.setL3RightFrontStiltsPowerOverride(l3RetractPower));
		onUntriggered(box.getButton(OperatorBoxButtons.GREEN2), Sequences.setL3RightFrontStiltsPowerOverride(0));
		onTriggered(box.getButton(OperatorBoxButtons.GREEN5), Sequences.setL3RearStiltsPowerOverride(l3RetractPower));
		onUntriggered(box.getButton(OperatorBoxButtons.GREEN5), Sequences.setL3RearStiltsPowerOverride(0));
		// L3 driving.
		final double l3DrivePower = 0.3;
		onTriggered(box.getButton(OperatorBoxButtons.BLUE4), Sequences.setL3DrivePower(l3DrivePower));
		onUntriggered(box.getButton(OperatorBoxButtons.BLUE4), Sequences.setL3DrivePower(0));
		onTriggered(box.getButton(OperatorBoxButtons.GREEN4), Sequences.setL3DrivePower(-l3DrivePower));
		onUntriggered(box.getButton(OperatorBoxButtons.GREEN4), Sequences.setL3DrivePower(0));
		*/

		// Shooter overrides.
		OverridableSubsystem<ShooterInterface> shooterOverride = subsystems.shooterOverride;
	  // Get the interface that the diag box uses.
		ShooterInterface shooterIF = shooterOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.SHOOTER_DISABLE, OperatorBoxButtons.SHOOTER_MANUAL, shooterOverride);
	  // While the shooter speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.SHOOTER_SPEED), 
			() -> {shooterIF.setTargetSpeed(box.getAxis(OperatorBoxButtons.SHOOTER_POT).read());log.sub("Shooter speed button pressed %f", box.getAxis(OperatorBoxButtons.SHOOTER_POT).read());});

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

		// Spark Test overrides. Buttons shared with the lift, disable it before enabling sparks.
		OverridableSubsystem<SparkTestInterface> sparkTestOverride = subsystems.sparkTestOverride;
		// Get the interface that the diag box uses.
		SparkTestInterface sparkTestIF = sparkTestOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.SPARK_DISABLE, OperatorBoxButtons.SPARK_MANUAL, sparkTestOverride);
		// While the spark speed button is pressed, set the target output level. Does not turn off on release.
		final double outputScale = 42;  
		whileTriggered(box.getButton(OperatorBoxButtons.SPARK_SET_SPEED), 
			() -> sparkTestIF.setMotorOutput(outputScale * box.getAxis(OperatorBoxButtons.SPARK_POT).read()));

		// Passthrough overrides.
		OverridableSubsystem<PassthroughInterface> passthroughOverride = subsystems.passthroughOverride;
		// Get the interface that the diag box uses.
		PassthroughInterface passthroughIF = passthroughOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.PASSTHRU_DISABLE, OperatorBoxButtons.PASSTHRU_MANUAL, passthroughOverride);
	  // While the passthrough speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.PASSTHRU_MOTOR), 
			() -> passthroughIF.setTargetMotorOutput(box.getAxis(OperatorBoxButtons.PASSTHRU_POT).read()));

		// Hatch overrides.
		OverridableSubsystem<HatchInterface> hatchOverride = subsystems.hatchOverride;
		// Get the interface that the diag box uses.
		HatchInterface hatchIF = hatchOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.HATCH_DISABLE, OperatorBoxButtons.HATCH_MANUAL, hatchOverride);
	  // Run the motor left while the move left button is pressed.
		onTriggered(box.getButton(OperatorBoxButtons.HATCH_MOVE_LEFT), 
			() -> hatchIF.setAction(new HatchAction(HatchAction.Type.SET_MOTOR_POWER, 0.2)));
		onUntriggered(box.getButton(OperatorBoxButtons.HATCH_MOVE_LEFT), 
			() -> hatchIF.setAction(new HatchAction(HatchAction.Type.SET_MOTOR_POWER, 0)));
		// Run the motor right while the move right button is pressed.
		onTriggered(box.getButton(OperatorBoxButtons.HATCH_MOVE_RIGHT), 
			() -> hatchIF.setAction(new HatchAction(HatchAction.Type.SET_MOTOR_POWER, -0.2)));
		onUntriggered(box.getButton(OperatorBoxButtons.HATCH_MOVE_RIGHT), 
			() -> hatchIF.setAction(new HatchAction(HatchAction.Type.SET_MOTOR_POWER, 0)));
		// Hatch grabber.
		onTriggered(box.getButton(OperatorBoxButtons.HATCH_HOLD), 
			() -> hatchIF.setHeld(true));
		onTriggered(box.getButton(OperatorBoxButtons.HATCH_RELEASE), 
			() -> hatchIF.setHeld(false));

		// Lift overrides. Buttons shared with the Spark Test override, disable it before enabling lift.
		OverridableSubsystem<LiftInterface> liftOverride = subsystems.liftOverride;
		// Get the interface that the diag box uses.
		LiftInterface liftIF = liftOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.LIFT_DISABLE, OperatorBoxButtons.LIFT_MANUAL, liftOverride);
	  // Override lift height.
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_SET_HEIGHT), 
			() -> liftIF.setTargetHeight(scaleLiftPotHeight(box.getAxis(OperatorBoxButtons.LIFT_POT).read())));
	  // Lift carriage.
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_DEPLOY_CARRIAGE), 
			() -> liftIF.deploy());
		onTriggered(box.getButton(OperatorBoxButtons.LIFT_RETRACT_CARRIAGE), 
			() -> liftIF.retract());
	}

	private double scaleStiltsPotHeight(double value) {
		// Pot has a value of -1 to 1. Scale to 0 - max_height.
		return (value + 1) / 2 * Constants.CLIMBER_L3_CLIMB_HEIGHT;
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
