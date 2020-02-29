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
import frc.robot.interfaces.ClimberInterface.ClimberAction.Type;
import frc.robot.lib.GamepadButtonsX;
import frc.robot.lib.OperatorBoxButtons;
import frc.robot.lib.WheelColour;
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
	 * Left flight joystick: move back/forward, climb up
	 * Right flight joystick: left/right
	 * While held, right joystick top far left button (Button 5): release ratchets
	 * While not held, right joystick top far left button (Button 5): enable ratchets
	 * Right joystick top far right button (Button 6): shift PTO mode
	 * Right joystick trigger (Button 1): intake
	 * Right joystick thumb button (Button 2): vision lineup
	 * Left joystick trigger (Button 1): slow/half speed
	 * 
	 * 
	 * Operator Controls:
	 * Pushing (A) begins positional control 
	 * Pushing (B) deploys/stows buddy climber	 
	 * Pushing (X) begins rotational control 
	 * Pushing (Y) deploy/stow the colourwheel 
	 * Left trigger: shoot
	 * Pushing left stick to the left rotates colourwheel anticlockwise
	 * Pushing left stick to the right rotates colourwheel clockwise
	 * Pushing (left bumper) sets the hood and shooter to preset shot 1
	 * Pushing (right bumper) sets the hood and shooter to preset shot 2
	 * 
	 * 
	 * The following operator buttons are unused:
	 * (back)(mode)(right stick up/down/left/right)(left stick up/down)(left trigger)
	 * (right trigger)(back)(start)(left joystick click)(right joystick click)
	 * (D-pad up/down/left/right)
	 * 
	 */
	public void configureJoysticks(FlightStick driverLeft, FlightStick driverRight, InputDevice operator) {
		// Left and Right driver joysticks have separate mappings, as well as Operator controller.
		configureDriverJoystick(driverLeft, driverRight, "driverSticks");
    	configureOperatorJoystick(operator, "operator");
	}
		
	
	public void configureDriverJoystick(FlightStick leftStick, FlightStick rightStick, String name) {

		//intake 
		onTriggered(rightStick.getButton(1), Sequences.startIntaking());
		onUntriggered(rightStick.getButton(1), Sequences.stopIntaking());
		
		//slowdrive
		onTriggered(leftStick.getButton(1), Sequences.startSlowDriveForward());
		onUntriggered(leftStick.getButton(1), Sequences.setDrivebaseToArcade());

		//release/enable ratchets (empty sequence)
		onTriggered(rightStick.getButton(5), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(5), Sequences.getEmptySequence());		

		//shift PTO mode, toggle (empty sequence)
		onTriggered(rightStick.getButton(6), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(6), Sequences.getEmptySequence());		

		//vision lineup
		onTriggered(rightStick.getButton(2), Sequences.visionAim());
		onUntriggered(rightStick.getButton(2), Sequences.getEmptySequence());	
	}

	public void configureOperatorJoystick(InputDevice stick, String name) {
		// i dont know what the text below is for, its from 2019
		//onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), Sequences.getStartSequence());
		
		//lucas pls finish the colourwheel stuff amogh is confused

		//note: manual adjust doesnt have an untriggered stop on it 
		//note: drop colourwheel has a seperate button (Y)


		//colourwheel positional
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.startColourWheelPositional(WheelColour.UNKNOWN));
		onUntriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.stopColourWheel());

		//colourwheel rotational
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.startColourWheelRotational());
		onUntriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.stopColourWheel());

		//manual adjust clockwise  
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD),
		() -> {
			return Sequences.colourWheelClockwise();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD), Sequences.getEmptySequence());

		//manual adjust anticlockwise  
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, -GamepadButtonsX.AXIS_THRESHOLD),
		() -> {
			return Sequences.colourWheelAnticlockwise();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD), Sequences.getEmptySequence());

		//deploy/stow colourwheel, toggle (empty sequence)
		onTriggered(stick.getButton(GamepadButtonsX.Y_BUTTON), Sequences.getEmptySequence());

		//shoot (empty sequence)
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD),
		() -> {
			return Sequences.getEmptySequence();
		});
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.getEmptySequence());

		//shot 1 (empty sequence)
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getEmptySequence());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getEmptySequence());

		//shot 2 (empty sequence) 
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.getEmptySequence());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.getEmptySequence());

		//buddy climb, toggle (empty sequence)
		onTriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.getEmptySequence());
		onUntriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.getEmptySequence());

		// Colour Wheel testing.
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.startColourWheelPositional(WheelColour.UNKNOWN));
		onUntriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.startColourWheelRotational());
		onUntriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.colourWheelAnticlockwise());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.colourWheelClockwise());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.stopColourWheel());


	}


 	@Override
	public void configureDiagBox(InputDevice box) {
		// Shooter overrides.
		OverridableSubsystem<ShooterInterface> shooterOverride = subsystems.shooterOverride;
		// Get the interface that the diag box uses.
		ShooterInterface shooterIF = shooterOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.SHOOTER_DISABLE, OperatorBoxButtons.SHOOTER_MANUAL, shooterOverride);
		// While the shooter speed button is pressed, set the target speed. Does not
		// turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.SHOOTER_SPEED), () -> {
			shooterIF.setTargetRPM(
					1.5 * Constants.SHOOTER_TARGET_SPEED_RPM * box.getAxis(OperatorBoxButtons.SHOOTER_POT).read());
			log.sub("Shooter speed button pressed %f", box.getAxis(OperatorBoxButtons.SHOOTER_POT).read());
		});

		// Intake overrides.
		OverridableSubsystem<IntakeInterface> intakeOverride = subsystems.intakeOverride;
		// Get the interface that the diag box uses.
		IntakeInterface intakeIF = intakeOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.INTAKE_DISABLE, OperatorBoxButtons.INTAKE_MANUAL, intakeOverride);
	    // While the intake speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.INTAKE_MOTOR), 
			() -> intakeIF.setMotorOutput(box.getAxis(OperatorBoxButtons.INTAKE_POT).read()));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_DEPLOY), 
			() -> intakeIF.setExtended(true));
		onTriggered(box.getButton(OperatorBoxButtons.INTAKE_STOW), 
			() -> intakeIF.setExtended(false));


		// Get the interface that the diag box uses.
		LoaderInterface loaderIF = subsystems.loaderOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.LOADER_DISABLE, OperatorBoxButtons.LOADER_MANUAL, subsystems.loaderOverride);
		// While the loader speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_SPINNER_MOTOR), 
			() -> loaderIF.setTargetSpinnerMotorRPM(10*box.getAxis(OperatorBoxButtons.LOADER_SPINNER_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_SPINNER_MOTOR),
			() -> loaderIF.setTargetSpinnerMotorRPM(0));
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR), 
			() -> loaderIF.setTargetPassthroughMotorOutput(box.getAxis(OperatorBoxButtons.LOADER_PASSTHROUGH_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR),
			() -> loaderIF.setTargetPassthroughMotorOutput(0));
		
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_BLOCKING), 
			() -> loaderIF.setPaddleNotBlocking(false));
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_NOTBLOCKING), 
			() -> loaderIF.setPaddleNotBlocking(true));

		
		ClimberInterface climberIF = subsystems.climberOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.CLIMBER_DISABLE, OperatorBoxButtons.CLIMBER_MANUAL, subsystems.climberOverride);
		onTriggered(box.getButton(OperatorBoxButtons.CLIMBER_EXTEND), 
			() -> climberIF.setDesiredAction(new ClimberAction(Type.SET_BOTH_HEIGHT, 
			0.2*box.getAxis(OperatorBoxButtons.CLIMBER_POT).read())));
	
		
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
