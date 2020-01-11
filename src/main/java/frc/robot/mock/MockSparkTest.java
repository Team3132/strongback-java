package frc.robot.mock;

import frc.robot.interfaces.Log;
import frc.robot.interfaces.SparkTestInterface;


/**
 * Mock subsystem responsible for the spark test subsystem.
 * This was used to qualify the Spark MAX motor controllers.
 */
public class MockSparkTest implements SparkTestInterface {
	private double output = 0;

	public MockSparkTest(Log log) {
	}


	@Override
	public void setMotorOutput(double current){
		output = current;
	}
	
	public double getMotorOutput(){
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