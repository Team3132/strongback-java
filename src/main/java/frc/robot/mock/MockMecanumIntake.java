package frc.robot.mock;

import frc.robot.interfaces.Intake;


/**
 * Mock subsystem responsible for the intake
 */
public class MockMecanumIntake implements Intake {
	private double output = 0;
	private boolean isExtended = false;

	public MockMecanumIntake() {
	}

	@Override
	public Intake setExtended(boolean extended) {
		isExtended = extended;
		return this;
	}

	@Override
	public boolean isExtended() {
		return isExtended;
	}

	@Override
	public boolean isRetracted() {
		return !isExtended;
	}

	@Override
	public Intake setTargetRPS(double rps){
		output = rps;
		return this;
	}
	
	@Override
	public double getTargetRPS(){
		return output;
	}

	
	@Override
	public String getName() {
		return "MockIntake";
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public void execute(long timeInMillis) {
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void cleanup() {
	}	
}