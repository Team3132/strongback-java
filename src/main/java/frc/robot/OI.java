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
	 * Left flight joystick: move back/forward, climb up/down
	 * Right flight joystick: left/right
	 * Right joystick bottom close left button (Button 12): deploy PTO
	 * Right joystick bottom close right button (Button 11): stow PTO
	 * Right joysick bottom middle right button (Button 10): brake
	 * Right joystick bottom middle left button (Button 9): stop braking
	 * Right joystick top close left button (Button 3): shoot
	 * Right joystick thumb button (Button 2): intake
	 * Right joystick trigger (Button 1): vision lineup
	 * Left joystick trigger (Button 1): slow/half speed
	 * 
	 * 
	 * Operator Controls:
	 * Pushing (A) begins positional control. 
	 * Pushing (B) stops the colourwheel from turning.	 
	 * Pushing (X) begins rotational control. 
	 * Pushing left stick to the left rotates colourwheel anticlockwise
	 * Pushing left stick to the right rotates colourwheel clockwise
	 * Pushing (left bumper) sets the hood and shooter to preset shot 1.
	 * Pushing (right bumper) sets the hood and shooter to preset shot 2.
	 * 
	 * 
	 * The following buttons are unused:
	 * (back)(mode)(left bumper)(right stick up/down/left/right)(left stick up/down)
	 * (left trigger)(right trigger)(back)(start)(left joystick click)(right joystick click)
	 * (D-pad up/down/left/right)(Y)
	 * 
	 */
	public void configureJoysticks(FlightStick driverLeft, FlightStick driverRight, InputDevice operator) {
		// Left and Right driver joysticks have separate mappings, as well as Operator controller.
		configureDriverJoystick(driverLeft, driverRight, "driverSticks");
    	configureOperatorJoystick(operator, "operator");
	}
		
	
	public void configureDriverJoystick(FlightStick leftStick, FlightStick rightStick, String name) {

		// Intake - Right Stick Button 2 (on/off)
		onTriggered(rightStick.getButton(2), Sequences.startIntaking());
		onUntriggered(rightStick.getButton(2), Sequences.stopIntaking());
		
		//slowdrive
		onTriggered(leftStick.getButton(1), Sequences.startSlowDriveForward());
		onUntriggered(leftStick.getButton(1), Sequences.setDrivebaseToArcade());

		//deploy pto (empty sequence)
		onTriggered(rightStick.getButton(12), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(12), Sequences.getEmptySequence());

		//stow pto (empty sequence)
		onTriggered(rightStick.getButton(11), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(11), Sequences.getEmptySequence());		

		//brake (empty sequence)
		onTriggered(rightStick.getButton(10), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(10), Sequences.getEmptySequence());		

		//stop braking (empty sequence)
		onTriggered(rightStick.getButton(9), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(9), Sequences.getEmptySequence());	

		//shoot (empty sequence)
		onTriggered(rightStick.getButton(3), Sequences.getEmptySequence());
		onUntriggered(rightStick.getButton(3), Sequences.getEmptySequence());	

		//vision lineup
		onTriggered(rightStick.getButton(2), Sequences.visionAim());
		onUntriggered(rightStick.getButton(2), Sequences.getEmptySequence());	
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

		//colourwheel positional
		//lucas pls do the colourwheel stuff amogh is confused
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.colourWheelPositional(WheelColour.UNKNOWN));
		onUntriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.getEmptySequence());

		//colourwheel rotational
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.colourWheelRotational());
		onUntriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.getEmptySequence());
	
		//shot 1
		//currently no sequence 
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getEmptySequence());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.getEmptySequence());

		//shot 2
		//currently no sequence 
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.getEmptySequence());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.getEmptySequence());

		// Colour Wheel testing.
		onTriggered(stick.getButton(GamepadButtonsX.Y_BUTTON), Sequences.colourWheelPositional(WheelColour.YELLOW));
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.colourWheelPositional(WheelColour.BLUE));
		onTriggered(stick.getButton(GamepadButtonsX.B_BUTTON), Sequences.colourWheelPositional(WheelColour.RED));
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.colourWheelPositional(WheelColour.GREEN));
		onTriggered(stick.getButton(GamepadButtonsX.START_BUTTON), Sequences.colourWheelRotational());
		onTriggered(stick.getButton(GamepadButtonsX.BACK_BUTTON), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.colourWheelLeft());
		onUntriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.stopColourWheel());
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.colourWheelRight());
		onUntriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.stopColourWheel());


	}


 	@Override
	public void configureDiagBox(InputDevice box) {
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

		// Get the interface that the diag box uses.
		LoaderInterface loaderIF = subsystems.loaderOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.LOADER_DISABLE, OperatorBoxButtons.LOADER_MANUAL, subsystems.loaderOverride);
	  // While the loader speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_SPINNER_MOTOR), 
			() -> loaderIF.setTargetSpinnerMotorVelocity(10*box.getAxis(OperatorBoxButtons.LOADER_SPINNER_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_SPINNER_MOTOR),
			() -> loaderIF.setTargetSpinnerMotorVelocity(0));
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR), 
			() -> loaderIF.setTargetPassthroughMotorVelocity(25*box.getAxis(OperatorBoxButtons.LOADER_PASSTHROUGH_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR),
			() -> loaderIF.setTargetPassthroughMotorVelocity(0));
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_FEEDER_MOTOR), 
			() -> loaderIF.setTargetFeederMotorOutput(box.getAxis(OperatorBoxButtons.LOADER_FEEDER_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_FEEDER_MOTOR),
			() -> loaderIF.setTargetFeederMotorOutput(0));
		
		
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_RETRACT), 
			() -> loaderIF.setPaddleExtended(false));
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_EXTEND), 
			() -> loaderIF.setPaddleExtended(true));
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
