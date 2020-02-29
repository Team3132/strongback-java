package frc.robot.lib;

import org.strongback.Executable;
import org.strongback.Strongback;
import org.strongback.Executor.Priority;
import org.strongback.components.Motor;

import frc.robot.Constants;
import frc.robot.interfaces.NetworkTableHelperInterface;

/**
 * Whenever a motor is created, networkTable for the motor PIDF values are implemented from here
 * 
 * 
 */

 class TunableMotor implements Executable {
	 Motor motor;
	 int id;
	 Motor left;
	NetworkTableHelperInterface networkTable;
	public TunableMotor(Motor motor, int id, double p, double i , double d , double f) {
		this.id = id;
		
	}

	public static void tuneMotor(Motor motor, int id, double p, double i , double d , double f, NetworkTableHelperInterface networkTable) {
		var tunable = new TunableMotor(motor, id, p, i, d, f);
		networkTable.get("p",  p);
		networkTable.get("i", i);
		networkTable.get("d", d);
		networkTable.get("f", f);
		motor.setPIDF(0, p, i, d, f);
		Strongback.executor().register(tunable, Priority.LOW);
	}

	@Override
	public void execute(long timeInMillis) {
		// TODO Auto-generated method stub

	}
 }