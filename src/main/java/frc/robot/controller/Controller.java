package frc.robot.controller;
  
import java.util.Iterator;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.strongback.components.Clock;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.ColourWheelType;
import frc.robot.lib.LEDColour;
import frc.robot.lib.WheelColour;
import frc.robot.subsystems.Subsystems;

/**
 * The controller of State Sequences while ensuring the robot is safe at every
 * step.
 * 
 * Allows higher level code to specify just the states that the robot needs to
 * pass through but it doesn't need to care how it gets there - this code will
 * ensure it gets there safely.
 * 
 * This is very similar to commands, with the differences to a command-based
 * approach being: - Unlike commands, the activation logic is concentrated in
 * one place, making it much safer to add new functionality. - Every state
 * doesn't need to be aware of every other state (much simpler). - Creating
 * strings of sequences is much simpler and shorter than commands. - Arbitrary
 * combinations of parallel and sequential commands aren't supported, only a
 * series of parallel operations.
 * 
 * This could be made faster, but we need to be careful it doesn't make it
 * unsafe.
 */
public class Controller implements Runnable, DashboardUpdater {
	private final Subsystems subsystems;
	private final Clock clock;
	private final DashboardInterface dashboard;
	private final Log log;
	private Sequence sequence = new Sequence("idle"); // Current sequence we are working through.
	private boolean sequenceHasChanged = true;
	private boolean sequenceHasFinished = false;
	private String blockedBy = "";
	private boolean isAlive = true; // For unit tests
	private Supplier<WheelColour> fmsColour;

	public Controller(Subsystems subsystems, Supplier<WheelColour> fmsColour) {
		this.subsystems = subsystems;
		this.clock = subsystems.clock;
		this.dashboard = subsystems.dashboard;
		this.log = subsystems.log;
		this.fmsColour = fmsColour;
		(new Thread(this)).start();
	}

	synchronized public void doSequence(Sequence sequence) {
		if (this.sequence == sequence) {
			// Exactly the same same sequence. Only start it again if it has
			// finished. Used in the whileTriggered(...) case.
			// Intentionally using == instead of .equalTo().
			if (!sequenceHasFinished)
				return;
		}
		this.sequence = sequence;
		sequenceHasChanged = true;
		logSub("Sequence has changed to %s sequence", sequence.getName());
		notifyAll(); // Tell the run() method that there is a new sequence.
	}

	/**
	 * Main entry point which applies state.
	 * 
	 * Usually run in a thread.
	 */
	@Override
	public void run() {
		try {
			Iterator<State> iterator = null;
			while (true) {
				State desiredState = null;
				synchronized (this) {
					if (sequenceHasChanged || iterator == null) {
						logSub("State sequence has changed, now executing %s sequence", sequence.getName());
						iterator = sequence.iterator();
						sequenceHasChanged = false;
						sequenceHasFinished = false;
					}
					if (!iterator.hasNext()) {
						logSub("Sequence %s is complete", sequence.getName());
						sequenceHasFinished = true;
						try {
							logSub("Controller waiting for a new sequence to run");
							wait();
							// logSub("Have a new sequence to run");
						} catch (InterruptedException e) {
							logSub("Waiting interrupted %s", e);
						}
						continue; // Restart from the beginning.
					}
					desiredState = iterator.next();
				}
				applyState(desiredState);
			}
		} catch (Exception e) {
			// The controller is dying, write the exception to the logs.
			log.exception("Controller caught an unhandled exception", e);

			// Used by the unit tests to detect if the controller thread is still running
			// see isAlive()
			isAlive = false;
		}
	}

	/**
	 * For use by unit tests only.
	 * 
	 * @return if an unhandled excpetions has occured in the controller
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * Does the simple, dumb and most importantly, safe thing.
	 * 
	 * Note if the step asks for something which will cause harm to the robot, the
	 * request will be ignored. For example if the lift was moved into a position
	 * the intake could hit it and then the intake was moved into the lift, the
	 * intake move would be ignored.
	 * 
	 * @param desiredState The state to leave the robot in.
	 */
	private void applyState(State desiredState) {
		if (desiredState == null) {
			System.out.println("Desired state is null!!!");
		}

		logSub("Applying requested state: %s", desiredState);
		//logSub("Waiting subsystems to finish moving before applying state");

		// Get the current state of the subsystems.
		State currentState = new State(subsystems, clock);
		logSub("Current state: %s", currentState);
		// Fill in the blanks in the desired state.
		desiredState = State.calculateUpdatedState(desiredState, currentState);
		if (desiredState.colourAction.movingToUnknownColour()) { // If the colour wheel is set to positional but the colour is unknown, work out the desired colour using FMS.
			desiredState.colourAction = new ColourAction(ColourWheelType.POSITION, fmsColour.get());
		}
		logSub("Calculated new 'safe' state: %s", desiredState);

		// The time beyond which we are allowed to move onto the next state
		double endTime = desiredState.timeAction.calculateEndTime(clock.currentTime());

		
		// Start driving if necessary.
		subsystems.drivebase.setDriveRoutine(desiredState.drive);
		subsystems.drivebase.applyBrake(desiredState.climberBrakeApplied);
	
		subsystems.intake.setExtended(desiredState.intakeExtended);
		subsystems.intake.setTargetRPS(desiredState.intakeRPS);

		subsystems.loader.setTargetSpinnerMotorRPS(desiredState.loaderSpinnerMotorRPS);
		subsystems.loader.setTargetPassthroughMotorOutput(desiredState.loaderPassthroughMotorOutput);
		subsystems.loader.setPaddleBlocking(desiredState.loaderPaddleBlocking);

		subsystems.colourWheel.setArmExtended(desiredState.extendColourWheel);
		subsystems.colourWheel.setDesiredAction(desiredState.colourAction);

		subsystems.shooter.setTargetRPS(desiredState.shooterRPS);
		subsystems.shooter.setHoodExtended(desiredState.shooterHoodExtended);

		// Toggle buddy climb if needed
		if (desiredState.buddyClimbToggle) {
			subsystems.buddyClimb.setExtended(!subsystems.buddyClimb.isExtended());
		}

		// Toggle between drive and climb modes if needed
		if (desiredState.driveClimbModeToggle) {
			subsystems.drivebase.activateClimbMode(!subsystems.drivebase.isClimbModeEnabled());
		}

		//subsystems.jevois.setCameraMode(desiredState.cameraMode);
		maybeWaitForBalls(desiredState.expectedNumberOfBalls);
		waitForIntake();
		waitForBlocker();
		waitForShooterHood();

		// set the LEDs to purple if we are trying to wait for the shooter to reach 0 rps
		if (desiredState.shooterUpToSpeed != null && desiredState.shooterUpToSpeed && desiredState.shooterRPS == 0) {
			subsystems.ledStrip.setColour(LEDColour.PURPLE);
			logSub("Should never be waiting for the shooter to reach 0 RPS. Running the empty sequence");
			doSequence(Sequences.getEmptySequence()); // TODO: replace this with a set leds to X colour sequence (remeber to update the logSub when this happens)
		}

		maybeWaitForShooter(desiredState.shooterUpToSpeed);
		maybeWaitForColourWheel();
		// Wait for driving to finish if needed.
		// If the sequence is interrupted it drops back to arcade.
		maybeWaitForAutoDriving();

		// Last thing: wait for the delay time if it's set.
		waitForTime(endTime);
	}

	private void maybeWaitForAutoDriving() {
		try {
			waitUntilOrAbort(() -> subsystems.drivebase.hasFinished(), "auto driving");
		} catch (SequenceChangedException e) {
			logSub("Sequence changed while driving, switching drivebase back to arcade");
			subsystems.drivebase.setArcadeDrive();
		}
	}

	/**
	 * Blocks waiting till the intake is in position.
	 */
	private void waitForIntake() {
		waitUntil(() -> subsystems.intake.isRetracted() || subsystems.intake.isExtended(), "intake to finish moving");
	}

	/**
	 * Blocks waiting till the blocker is in position.
	 */
	private void waitForBlocker() {
		waitUntil(() -> subsystems.loader.isPaddleBlocking() || subsystems.loader.isPaddleNotBlocking(), "blocking to finish moving");
	}

	/**
	 * Blocks waiting till the shooter hood is in position.
	 */
	private void waitForShooterHood() {
		waitUntil(() -> subsystems.shooter.isHoodExtended() || subsystems.shooter.isHoodRetracted(), "hood to finish moving");
	}

	/**
	 * Maybe wait for the shooter to get up to the target speed.
	 * @param shooterUpToSpeed if not null, blocks waiting for shooter to achieve target speed.
	 */
	private void maybeWaitForShooter(Boolean shooterUpToSpeed) {
		if (shooterUpToSpeed == null) {
			// Don't wait.
			return;
		}
		
		try {
			waitUntilOrAbort(() -> subsystems.shooter.isAtTargetSpeed(), "shooter");
		} catch (SequenceChangedException e) {
			logSub("Sequence changed while spinning up shooter, stopping shooter");
			subsystems.shooter.setTargetRPS(0);
		}
	}
	
	private void maybeWaitForColourWheel() {
		try {
			waitUntilOrAbort(() -> subsystems.colourWheel.isFinished(), "colour wheel finished");
		} catch (SequenceChangedException e) {
			logSub("Sequence changed while moving colour wheel");
			// The sequence has changed, setting action to null.
			subsystems.colourWheel.setDesiredAction(new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN));
			subsystems.colourWheel.setArmExtended(false);
		}
	}

	/**
	 * Waits until loader has specific number of balls, or sequence is aborted.
	 * @param expectBalls the number of balls to wait for. If null, it won't wait.
	 */
	private void maybeWaitForBalls(Integer expectBalls) {
		if (expectBalls == null) {
			// This state doesn't specify the number of balls to wait for.
			return;
		}
		if (subsystems.loader.getCurrentBallCount() == expectBalls) return;
		logSub("Waiting for balls");
		try {
			waitUntilOrAbort(() -> subsystems.loader.getCurrentBallCount() == expectBalls, "numBalls");
		} catch (SequenceChangedException e) {
			// Desired state has changed underneath us, give up waiting
			//and return.
			return;
		}
	}
	
	/**
	 * Blocks waiting until endtime has passed.
	 */
	private void waitForTime(double endTimeSec) {
		if (clock.currentTime() < endTimeSec) {
			//logSub("Waiting for %.1f seconds", endTimeSec - clock.currentTime());
		}
		waitUntil(() -> clock.currentTime() > endTimeSec, "time");
	}

	/**
	 * Waits for func to return true or the sequence has changed.
	 * @param func returns when this function returns true.
	 * @throws SequenceChangedException if the sequence has changed.
	 */
	private void waitUntilOrAbort(BooleanSupplier func, String name) throws SequenceChangedException {
		double startTimeSec = clock.currentTime();
		double waitDurationSec = 1;
		double nextLogTimeSec = startTimeSec + waitDurationSec;
		// Wait until func returns true or the desired state changed.
		while (!func.getAsBoolean()) {
			synchronized (this) {
				if (sequenceHasChanged) {
					throw new SequenceChangedException();
				}
			}
			double now = clock.currentTime();
			if (now > nextLogTimeSec) {
				logSub("Controller waiting on %s, has waited %fs so far", name, now - startTimeSec);
				blockedBy = name;  // Update the dashboard with what the controller is waiting for.
				waitDurationSec *= 2;
				nextLogTimeSec = now + waitDurationSec;
			}
			clock.sleepMilliseconds(10);
		}
		blockedBy = "";
		if (clock.currentTime() - nextLogTimeSec > 1) {
			// Print a final message.
			logSub("Controller done waiting on %s", name);
		}
	}
	
	/**
	 * Waits for func to return true.
	 * @param func returns when this function returns true.
	 */
	private void waitUntil(BooleanSupplier func, String name) {
		double startTimeSec = clock.currentTime();
		double waitDurationSec = 1;
		double nextLogTimeSec = startTimeSec + waitDurationSec;
		// Keep waiting until func returns true
		while (!func.getAsBoolean()) {
			double now = clock.currentTime();
			if (now > nextLogTimeSec) {
				logSub("Controller waiting on %s, has waited %fs so far", name, now - startTimeSec);
				blockedBy = name;  // Update the dashboard with what the controller is waiting for.
				waitDurationSec *= 2;
				nextLogTimeSec = now + waitDurationSec;
			}
			clock.sleepMilliseconds(10);
		}
		blockedBy = "";
		if (clock.currentTime() - nextLogTimeSec > 1) {
			// Print a final message.
			logSub("Controller done waiting on %s", name);
		}
	}

	private class SequenceChangedException extends Exception {
		private static final long serialVersionUID = 1L;
		public SequenceChangedException() {
			super("SequenceChanged");
		}
	}
	
	private void logSub(String message, Object... args) {
		String time_str = String.format("%.3f controller: ", clock.currentTime());
		log.sub(time_str + message, args);
	}

	private void logErr(String message, Object... args) {
		String time_str = String.format("%.3f controller: ", clock.currentTime());
		log.error(time_str + message, args);
	}

	public synchronized void updateDashboard() {
		String name = "None";
		if (sequence != null) {
			name = sequence.getName();
		}
		dashboard.putString("Controller: Current sequence", name);
		dashboard.putBoolean("Controller: Sequence finished", sequenceHasFinished);
		dashboard.putString("Controller: Blocked by", blockedBy);
	}
}