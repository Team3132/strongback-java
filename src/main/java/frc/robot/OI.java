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
	 * Left flight joystick: move back/forward, climb up
	 * Right flight joystick: left/right, climb up
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
		
	
	private void configureDriverJoystick(FlightStick leftStick, FlightStick rightStick, String name) {

		// Intake 
		onTriggered(rightStick.getButton(1), Sequences.startIntaking());
		onUntriggered(rightStick.getButton(1), Sequences.stopIntaking());
		
		// Slow drive
		/*onTriggered(leftStick.getButton(1), Sequences.startSlowDriveForward());
		onUntriggered(leftStick.getButton(1), Sequences.setDrivebaseToArcade());*/

		// Release/enable ratchets (empty sequence)
		onTriggered(rightStick.getButton(5), Sequences.releaseClimberBrake());
		onUntriggered(rightStick.getButton(5), Sequences.applyClimberBrake());

		// Vision lineup
		onTriggered(rightStick.getButton(2), Sequences.visionAim());

		// Arcade
		onTriggered(leftStick.getButton(4), Sequences.setDrivebaseToArcade());	

		// Reverse intake
		onTriggered(rightStick.getButton(3), Sequences.reverseIntaking());
		onUntriggered(rightStick.getButton(3), Sequences.stopIntaking());

		onToggle(rightStick.getButton(6), "climb/drive", Sequences.enableClimbMode(), Sequences.enableDriveMode());

	}

	private void configureOperatorJoystick(InputDevice stick, String name) {
		// Colourwheel positional
		onTriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.startColourWheelPositional(WheelColour.UNKNOWN));
		onUntriggered(stick.getButton(GamepadButtonsX.A_BUTTON), Sequences.stopColourWheel());

		// Colourwheel rotational
		onTriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.startColourWheelRotational());
		onUntriggered(stick.getButton(GamepadButtonsX.X_BUTTON), Sequences.stopColourWheel());

		// Colourwheel manual adjust clockwise  
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD), Sequences.colourWheelClockwise());
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD), Sequences.stopColourWheel());

		// Colourwheel manual adjust anticlockwise  
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, -GamepadButtonsX.AXIS_THRESHOLD), Sequences.colourWheelAnticlockwise());
		onUntriggered(stick.getAxis(GamepadButtonsX.LEFT_X_AXIS, GamepadButtonsX.AXIS_THRESHOLD), Sequences.stopColourWheel());

		// Close/ender shot
		onTriggered(stick.getAxis(GamepadButtonsX.LEFT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.spinUpCloseShot(Constants.SHOOTER_CLOSE_TARGET_SPEED_RPS));
		// Auto line/front trench shot
		onTriggered(stick.getButton(GamepadButtonsX.LEFT_BUMPER), Sequences.spinUpFarShot(Constants.SHOOTER_AUTO_LINE_TARGET_SPEED_RPS));
		// Colourwheel/back trench shot
		onTriggered(stick.getButton(GamepadButtonsX.RIGHT_BUMPER), Sequences.spinUpFarShot(Constants.SHOOTER_FAR_TARGET_SPEED_RPS));

		// Acutally shoot the balls
		// YOU MUST use one of the spinUp buttons before pressing this
		onTriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.startShooting());
		onUntriggered(stick.getAxis(GamepadButtonsX.RIGHT_TRIGGER_AXIS, GamepadButtonsX.TRIGGER_THRESHOLD), Sequences.stopShooting());

		// Buddy climb
		onToggle(stick.getButton(GamepadButtonsX.B_BUTTON), "deploybuddyclimb/stowbuddyclimb", Sequences.deployBuddyClimb(), Sequences.stowBuddyClimb());

		// Stow the intake
		onTriggered(stick.getButton(GamepadButtonsX.BACK_BUTTON), Sequences.raiseIntake());
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
		onTriggered(box.getButton(OperatorBoxButtons.SHOOTER_HOOD_UP), 
			() -> shooterIF.setHoodExtended(true));
		onTriggered(box.getButton(OperatorBoxButtons.SHOOTER_HOOD_DOWN), 
			() -> shooterIF.setHoodExtended(false));
		whileTriggered(box.getButton(OperatorBoxButtons.SHOOTER_SPEED), () -> {
			shooterIF.setTargetRPS(
					1.5 * Constants.SHOOTER_CLOSE_TARGET_SPEED_RPS * box.getAxis(OperatorBoxButtons.SHOOTER_POT).read());
		});
		onUntriggered(box.getButton(OperatorBoxButtons.SHOOTER_SPEED), 
			() -> shooterIF.setTargetRPS(0));

		// Intake overrides.
		OverridableSubsystem<IntakeInterface> intakeOverride = subsystems.intakeOverride;
		// Get the interface that the diag box uses.
		IntakeInterface intakeIF = intakeOverride.getOverrideInterface();
		// Setup the switch for manual/auto/off modes.
		mapOverrideSwitch(box, OperatorBoxButtons.INTAKE_DISABLE, OperatorBoxButtons.INTAKE_MANUAL, intakeOverride);
	    // While the intake speed button is pressed, set the target speed. Does not turn off.
		whileTriggered(box.getButton(OperatorBoxButtons.INTAKE_MOTOR), 
			() -> intakeIF.setTargetRPS(box.getAxis(OperatorBoxButtons.INTAKE_POT).read() * Constants.INTAKE_TARGET_RPS));
		onUntriggered(box.getButton(OperatorBoxButtons.INTAKE_MOTOR), 
			() -> intakeIF.setTargetRPS(0));
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
			() -> loaderIF.setTargetSpinnerRPS(1.5*Constants.LOADER_MOTOR_INTAKING_RPS*box.getAxis(OperatorBoxButtons.LOADER_SPINNER_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_SPINNER_MOTOR),
			() -> loaderIF.setTargetSpinnerRPS(0));
		whileTriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR), 
			() -> loaderIF.setTargetPassthroughDutyCycle(box.getAxis(OperatorBoxButtons.LOADER_PASSTHROUGH_POT).read()));
		onUntriggered(box.getButton(OperatorBoxButtons.LOADER_PASSTHROUGH_MOTOR),
			() -> loaderIF.setTargetPassthroughDutyCycle(0));
		
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_BLOCKING), 
			() -> loaderIF.setPaddleBlocking(true));
		onTriggered(box.getButton(OperatorBoxButtons.LOADER_PADDLE_NOTBLOCKING), 
			() -> loaderIF.setPaddleBlocking(false));
	}



	private void mapOverrideSwitch(InputDevice box, int disableButton, int manualButton, OverridableSubsystem<?> overrideableSubsystem) {
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

	/**
	 * Create a mode switch based on two buttons.
	 * Supports having other buttons change their behaviour based on the mode.
	 * 
	 * <pre>
     * {@code
	 * onMode(rightStick.getButton(5), rightStick.getButton(6), "drive/climb", Sequences.enableClimbMode(), Sequences.enableDriveMode())
	 *   .onTriggered(rightStick.getButton(5), Sequences.releaseClimberBrake(), Sequences.startSlowDriveForward())
	 *   .onUntriggered(rightStick.getButton(5), Sequences.releaseClimberBrake(), Sequences.driveFast());
     * }
     * </pre>
	 * 
	 * @param switchOn condition used to enable the mode. Normally a button press.
	 * @param switchOff condition used to disable the mode. Normally a button press.
	 * @param name used for logging when the mode changes.
	 * @param activateSeq sequence to run when the mode is actived.
	 * @param deactiveSeq sequence to run when the mode is deactived.
	 * @return the ModeSwitch for further chaining of more buttons based on the mode.
	 */
	@SuppressWarnings("unused")
	private ModeSwitch onMode(Switch switchOn, Switch switchOff, String name, Sequence activateSeq, Sequence deactiveSeq) {
		return new ModeSwitch(switchOn, switchOff, name, activateSeq, deactiveSeq);
	}

	/**
	 * Maintains the state of a toggle switch based on a single button.
	 * Supports having other buttons change their behaviour based on the state of the toggle.
	 */
	@SuppressWarnings("unused")
	private class ModeSwitch {
		private boolean active = false;

		/**
		 * Creates a ToggleSwitch to track the state and run sequences on state change.
		 * 
		 * @param switchOn condition used to enable the mode. Normally a button press.
		 * @param switchOff condition used to disable the mode. Normally a button press.
		 * @param name   used for logging when the mode changes.
		 * @param activatedSeq sequence to run when the mode is actived.
		 * @param deactivedSeq sequence to run when the mode is deactived.
		 * @return the ModeSwitch for chaining of more buttons based on the mode.
		 */
		public ModeSwitch(Switch switchOn, Switch switchOff, String name, Sequence activatedSeq, Sequence deactivedSeq) {
			Strongback.switchReactor().onTriggered(switchOn, () -> {
				if (active) {
					return;
				}
				log.sub("Activating " + name);
				exec.doSequence(activatedSeq);
				active = true;
			});
			Strongback.switchReactor().onTriggered(switchOff, () -> {
				if (!active) {
					return;
				}
				log.sub("Deactivating " + name);
				exec.doSequence(deactivedSeq);
				active = false;
			});
		}

		/**
		 * Run different sequences depending on the mode on button press.
		 * @param swtch condition to trigger a sequence to run. Normally a button press.
		 * @param activeSeq sequence to run if the mode is active.
		 * @param inactiveSeq sequence to run if the mode is inactive.
		 * @return the ModeSwitch for further chaining of more buttons based on the mode.
		 */
		public ModeSwitch onTriggered(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
			Strongback.switchReactor().onTriggered(swtch, () -> {
				if (active) {
					exec.doSequence(activeSeq);
				} else {
					exec.doSequence(inactiveSeq);
				}
			});
			return this;
		}

		/**
		 * Run different sequences depending on the mode on button release.
		 * @param swtch condition to trigger a sequence to run. Normally a button release.
		 * @param activeSeq sequence to run if the mode is active.
		 * @param inactiveSeq sequence to run if the mode is inactive.
		 * @return the ModeSwitch for further chaining of more buttons based on the mode.
		 */
		public ModeSwitch onUntriggered(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
			Strongback.switchReactor().onUntriggered(swtch, () -> {
				if (active) {
					exec.doSequence(activeSeq);
				} else {
					exec.doSequence(inactiveSeq);
				}
			});
			return this;
		}

		/**
		 * Run different sequences depending on the mode while a button is pressed.
		 * @param swtch condition to trigger a sequence to run. Normally while a button is pressed.
		 * @param activeSeq sequence to run if the mode is active.
		 * @param inactiveSeq sequence to run if the mode is inactive.
		 * @return the ModeSwitch for further chaining of more buttons based on the mode.
		 */
		public ModeSwitch whileTriggered(Switch swtch, Sequence activeSeq, Sequence inactiveSeq) {
			Strongback.switchReactor().whileTriggered(swtch, () -> {
				if (active) {
					exec.doSequence(activeSeq);
				} else {
					exec.doSequence(inactiveSeq);
				}
			});
			return this;
		}
	}

	
	/**
	 * Create a toggle switch based on a single button.
	 * Supports having other buttons change their behaviour based on the state of the toggle.
	 * 
	 * <pre>
     * {@code
	 * onToggle(rightStick.getButton(6), "drive/climb", Sequences.enableClimbMode(), Sequences.enableDriveMode())
	 *   .onTriggered(rightStick.getButton(5), Sequences.releaseClimberBrake(), Sequences.startSlowDriveForward())
	 *   .onUntriggered(rightStick.getButton(5), Sequences.releaseClimberBrake(), Sequences.driveFast());
     * }
     * </pre>
	 * 
	 * @param swtch condition used to toggle the state. Normally a button press.
	 * @param name used for logging when the toggle changes.
	 * @param onSeq sequence to run when the toggle is triggered on.
	 * @param offSeq sequence to run when the toggle is triggered off.
	 * @return the ToggleSwitch for further chaining of more buttons based on the toggle state.
	 */
	private ToggleSwitch onToggle(Switch swtch, String name, Sequence onSeq, Sequence offSeq) {
		return new ToggleSwitch(swtch, name, onSeq, offSeq);
	}

	/**
	 * Maintains the state of a toggle switch based on a single button.
	 * Supports having other buttons change their behaviour based on the state of the toggle.
	 */
	@SuppressWarnings("unused")
	private class ToggleSwitch {
		private boolean toggled = false;

		/**
		 * Creates a ToggleSwitch to track the state and run sequences on state change.
		 * 
		 * @param swtch  condition used to toggle the state. Normally a button press.
		 * @param name   used for logging when the toggle changes.
		 * @param onSeq  sequence to run when the toggle is triggered on.
		 * @param offSeq sequence to run when the toggle is triggered off.
		 * @return the ToggleSwitch for further chaining of more buttons based on the toggle state.
		 */
		public ToggleSwitch(Switch swtch, String name, Sequence onSeq, Sequence offSeq) {
			Strongback.switchReactor().onTriggered(swtch, () -> {
				if (!toggled) {
					log.sub("Toggling on " + name);
					exec.doSequence(onSeq);
				} else {
					log.sub("Toggling off " + name);
					exec.doSequence(offSeq);
				}
				toggled = !toggled;
			});
		}

		/**
		 * Run different sequences depending on toggle state on button press.
		 * @param swtch condition to trigger a sequence to run. Normally a button press.
		 * @param ifOnSeq sequence to run if the toggle is on.
		 * @param ifOffSeq sequence to run if the toggle is off.
		 * @return the ToggleSwitch for further chaining of more button based on the toggle state.
		 */
		public ToggleSwitch onTriggered(Switch swtch, Sequence ifOnSeq, Sequence ifOffSeq) {
			Strongback.switchReactor().onTriggered(swtch, () -> {
				if (toggled) {
					exec.doSequence(ifOnSeq);
				} else {
					exec.doSequence(ifOffSeq);
				}
			});
			return this;
		}

		/**
		 * Run different sequences depending on toggle state on button release.
		 * @param swtch condition to trigger a sequence to run. Normally a button release.
		 * @param ifOnSeq sequence to run if the toggle is on.
		 * @param ifOffSeq sequence to run if the toggle is off.
		 * @return the ToggleSwitch for further chaining of more button based on the toggle state.
		 */
		public ToggleSwitch onUntriggered(Switch swtch, Sequence ifOnSeq, Sequence ifOffSeq) {
			Strongback.switchReactor().onUntriggered(swtch, () -> {
				if (toggled) {
					exec.doSequence(ifOnSeq);
				} else {
					exec.doSequence(ifOffSeq);
				}
			});
			return this;
		}

		/**
		 * Run different sequences depending on toggle state while a button is pressed.
		 * @param swtch condition to trigger a sequence to run. Normally while a button is pressed.
		 * @param ifOnSeq sequence to run if the toggle is on.
		 * @param ifOffSeq sequence to run if the toggle is off.
		 * @return the ToggleSwitch for further chaining of more button based on the toggle state.
		 */
		public ToggleSwitch whileTriggered(Switch swtch, Sequence ifOnSeq, Sequence ifOffSeq) {
			Strongback.switchReactor().whileTriggered(swtch, () -> {
				if (toggled) {
					exec.doSequence(ifOnSeq);
				} else {
					exec.doSequence(ifOffSeq);
				}
			});
			return this;
		}
	}
}
