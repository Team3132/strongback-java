package frc.robot.controller;

import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockPneumaticsModule;
import frc.robot.Constants;
import frc.robot.Constants.LiftSetpoint;
import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.LiftInterface;
import frc.robot.interfaces.Log;
import frc.robot.mock.MockClimber;
import frc.robot.mock.MockDashboard;
import frc.robot.mock.MockDrivebase;
import frc.robot.mock.MockHatch;
import frc.robot.mock.MockLocation;
import frc.robot.mock.MockLog;
import frc.robot.mock.MockLoader;
import frc.robot.mock.MockSpitter;
import frc.robot.simulator.IntakeSimulator;
import frc.robot.simulator.LiftSimulator;
import frc.robot.subsystems.Subsystems;

/**
 * Test cases for the Controller and the Sequences
 * 
 * Mocks out almost everything so that no hardware is needed.
 */
public class TestController {
	// Use 10 ms steps between executions.
	private final long ktestStepMs = 10;
	private final long kRandomSeed = 123456;
	private final double kMaxWaitTimeSeconds = 4;
	protected DashboardInterface dashboard = new MockDashboard();
	protected Log log = new MockLog(true);
	private MockClock clock;
	private Subsystems subsystems;
	// Store direct access to the simulators so the simulator-only
	// methods can be called.
	private IntakeSimulator intake;
	private LiftSimulator lift;
	private TestHelper test;
	// The bit that is being tested under test.
	private Controller exec;
		
	/**
	 * Setup fields used by this test.
	 */
	@Before
	public void setUp() {
		System.out.println("\n******************************");
		clock = new MockClock();
		subsystems = new Subsystems(new MockDashboard(), null, clock, log);

		subsystems.intake = intake = new IntakeSimulator();
		subsystems.lift = lift = new LiftSimulator();
		subsystems.climber = new MockClimber(log);
		subsystems.compressor = new MockPneumaticsModule(); 
		subsystems.drivebase = new MockDrivebase(log);
		subsystems.loader = new MockLoader(log);
		subsystems.spitter = new MockSpitter(log);
		subsystems.hatch = new MockHatch(log);
		subsystems.location = new MockLocation();
		subsystems.leftDriveDistance = () -> 0;
		subsystems.rightDriveDistance = () -> 0;
		
		exec = new Controller(subsystems);

		test = new TestHelper(() -> {
			clock.incrementByMilliseconds(ktestStepMs);
			//long now = clock.currentTimeInMillis();
			//System.out.printf("==== Cycle starting at time %.03fms ====\n", now/1000.);
			subsystems.intake.execute(clock.currentTimeInMillis());
			subsystems.lift.execute(clock.currentTimeInMillis());
			return clock.currentTime();
		},() -> {
			System.out.println(subsystems.intake.toString());
			System.out.println(subsystems.lift.toString());
		});
		// Add safety functions to be called every step off the way.
		test.registerSafetyFunc(() -> checkIntakeVsLift());

		// if the controller dies we should fail the test
		test.registerSafetyFunc(() ->
			assertTrue("The controller has died failing the test. " +
					   "A stack trace should be above",exec.isAlive())); 
	}
	
	/**
	 * Example test.
	 * 
	 * Use this as a template when designing new tests.
	 */
	/*
	@Test
	public void testExampleForCopying() {
		// Update the println statement with your test name.
		System.out.println("testExampleForCopying");
		
		// Setup initial subsystem state. Lift starts at the bottom and the intake stowed. 
		// Set what  you need for the test here. Once the subsystems have been set up,
		// then the test will move on to the next thenSet(), thenAssert() or thenWait()
		// statement.
		test.thenAssert(outtakeOpen(true), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));

		// Run the intaking sequence.
		test.thenSet(sequence(Sequences.getStartIntakingSequence()));
		
		// Then assert that the robot subsystems have eventually been put into the expected states.
		// Having them in one .thenAssert() means that they will all have to be true at once.
		// In this case the intake should be in the narrow configuration, the outtake should be open,
		// the intake motor should have full power forward and the lift should be in the intake position. 
		test.thenAssert(outtakeOpen(true), intakeMotorPower(1), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// The test can then be told to do nothing for some number of seconds.
		test.thenWait(0.5);
		
		// Then you can tell the robot that a cube was detected in the outtake, which
		// may make the sequence move on to the next state.
		// NB, this sensor has been removed, so hasCube() is no longer an option.
		test.thenSet(hasCube(true));
		
		// And then make the test check that the subsystems are updated to be in the correct state.
		test.thenAssert(outtakeOpen(false), intakeMotorPower(0), liftHeight(LiftPosition.INTAKE_POSITION));
		
		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		// This line executes the steps set up above. Note adding println statements
		// will print out the statements when the test is setup, not as it moves through
		// the states.
		assertTrue(test.run());
	}
    */	
		
	/**
	 * Test the lift setpoints.
	 * 
	 * Setpoints are fixed points that can be jumped between.
	 */
	@Test
	public void testLiftSetpoints() {
		System.out.println("testLiftSetpoints");
		// Setup initial state, starting on a setpoint.
		test.thenSet(liftHeight(LiftSetpoint.LIFT_BOTTOM_HEIGHT.height));
		
		// Tell it to move up to the next setpoint.
		test.thenSet(sequence(Sequences.liftSetpointUp()));
		
		// Should now be at switch height.
		//test.thenAssert(liftHeight(Constants.LIFT_CARGO_SHIP_HEIGHT));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Tell it to move up to the next setpoint.
		test.thenSet(sequence(Sequences.liftSetpointUp()));
		
		// Should now be at scale height.
		test.thenAssert(liftHeight(LiftSetpoint.LIFT_BOTTOM_HEIGHT.height));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Drop it down more than the lift tolerance and tell it to go back
		// up to the same setpoint.
		test.thenSet(sequence(Sequences.getMicroAdjustSequence(-4)));
		test.thenSet(sequence(Sequences.liftSetpointUp()));
		
		// Should now be back at the scale height.
		test.thenAssert(liftHeight(LiftSetpoint.LIFT_BOTTOM_HEIGHT.height));
		
		// Wait for the sequence to finish before running it again.
		test.thenWait(1);

		// Move down a setpoint.
		test.thenSet(sequence(Sequences.liftSetpointDown()));

		// Should now be at switch height.
		test.thenAssert(liftHeight(Constants.LIFT_DEFAULT_MIN_HEIGHT));

		// Lift it up more than the lift tolerance and tell it to go back
		// down to the same setpoint.
		test.thenSet(sequence(Sequences.getMicroAdjustSequence(4)));
		test.thenSet(sequence(Sequences.liftSetpointDown()));

		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assertTrue(test.run());
	}

	/**
	 * Pretends to be a crazy operator that keeps changing their mind.
	 * 
	 * This allows it to check that the robot is always in a safe configuration
	 * as the safety checker is checking the state of the robot every time.
	 * It sleeps for a random amount of time between desired state changes to
	 * allow the robot to get either fully into the new state, or part way.
	 * 
	 * There is no checking that the robot is actually doing anything useful
	 * here, only that it doesn't hurt itself.
	 */
	@Test
	public void testCrazyOperatorFuzzTest() {
		System.out.println("testCrazyOperatorFuzzTest");
		// Seed the random number generator so that the same
		// random numbers are generated every time.
		Random generator = new Random(kRandomSeed);
		
		// Build a large number random steps.
		for (int i=0; i<10 /*0*/; i++) {
			// Ask for a random desired state.
			test.thenSet(sequence(getRandomDesiredSequence(generator)));
			test.thenWait(generator.nextDouble() * kMaxWaitTimeSeconds);				
		}

		// Walk through setting the states and asserting that the robot eventually
		// moves through the required state.
		assertTrue(test.run());
	}
	

	// Helpers only from this point onwards.

	private Sequence getRandomDesiredSequence(Random generator) {
		return Sequences.allSequences[generator.nextInt(Sequences.allSequences.length)];
	}

	/**
	 * Either sets the lift carriage to be deployed or retracted, OR asserts it's deployed or
	 * retracted depending if it's in a thenSet() or a thenAssert().
	 * @param deployed which is the desired state.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter liftDeployed(boolean deployed) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("LiftExtended(%s)", deployed);
			}
			@Override
			public void setState() {
				if (deployed) {
					subsystems.lift.deploy();
				} else {
					subsystems.lift.retract();
				}
			}
			@Override
			public void assertState() throws AssertionError {
				boolean isDeployed = subsystems.lift.isDeployed();
				if (isDeployed == deployed) {
					if (deployed) {
						throw new AssertionError("Expected lift to be deployed, but it's retracted.");
					} else {
						throw new AssertionError("Expected lift to be retracted, but it's deployed");
					}
				}
			}
		};
	}

	/**
	 * Either sets the intake motor power, OR asserts the power the motor has
	 * been set to, depending if it's in a thenSet() or a thenAssert().
	 * @param power to set/expect to/from the motor.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter intakeMotorPower(double power) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("IntakeMotorPower(%.1f)", power);
			}
			@Override
			public void setState() {
				subsystems.intake.setMotorOutput(power);
			}
			@Override
			public void assertState() throws AssertionError {
				if (Math.abs(subsystems.intake.getMotorOutput() - power) > 0.1) {
					throw new AssertionError("Expected intake motor to have power " + power + " but it is "
							+ subsystems.intake.getMotorOutput());
				}
			}
		};
	}

	/**
	 * Either sets the lift height, OR asserts the lift height has
	 * been set to, depending if it's in a thenSet() or a thenAssert().
	 * @param height the position of the lift.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter liftHeight(double height) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("LiftHeight(%f)", height);
			}
			@Override
			public void setState() {
				// Use the override.
				lift.setLiftHeightActual(height);
			}
			@Override
			public void assertState() throws AssertionError {
				if (Math.abs(subsystems.lift.getHeight() - height) > Constants.LIFT_DEFAULT_TOLERANCE) {
					//System.out.println("Expected lift to be at position " + pos.toString() + "(" + pos.value
					//		+ ") but it is " + subsystems.lift.getLiftHeight());
					throw new AssertionError("Expected lift to be at position " + height + "(" + height
							+ ") but it is " + subsystems.lift.getHeight());
				}
			}
		};
	}

	/**
	 * Tells the Controller to run the desired sequence. Only makes sense in a
	 * thenSet(), not a thenAssert().
	 * @param sequence the sequence to execute.
	 * @return a setter or asserter object to pass to the TestHelper.
	 */
	private StateSetterOrAsserter sequence(Sequence sequence) {
		return new StateSetterOrAsserter() {
			@Override
			public String name() {
				return String.format("Sequence(%s)", sequence.getName());
			}
			@Override
			public void setState() {
				exec.doSequence(sequence);
			}
			@Override
			public void assertState() throws AssertionError {
				throw new AssertionError("Invalid usage of sequence() in thenAssert()");
			}
		};
	}

	/**
	 * Safety function.
	 * Check that the lift and the intake aren't colliding.
	 * If the lift is low, then the intake needs to be either in
	 * the stowed or the wide configuration. Anything else will
	 * mean that any cube will catch on the intake as the cube is
	 * lifted.
	 */
	private void checkIntakeVsLift() throws AssertionError {
		/* FIXME: Update for 2019 code - the lift can't be at the bottom and deployed.
		if (subsystems.lift.isAboveIntakeThreshold()) return;
		if (subsystems.lift.getDesiredHeight() < Constants.LIFT_INTAKE_HEIGHT + Constants.LIFT_DEFAULT_TOLERANCE) return;
		// Lift is below intake threshold.
		IntakeConfiguration config = subsystems.intake.getConfiguration();
		if (config == IntakeConfiguration.STOWED) return;
		if (config == IntakeConfiguration.WIDE) return;
		// Intake isn't in stowed or wide, this is a problem.
		throw new AssertionError("Lift (" + subsystems.lift.getHeight() + ") is below intake threshold (" + subsystems.lift.getHeight() + ") and intake is in configuration " + config);
		*/
	}
}
