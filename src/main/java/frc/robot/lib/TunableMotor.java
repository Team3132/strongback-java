package frc.robot.lib;

import java.util.ArrayList;

import org.strongback.Executable;
import org.strongback.Strongback;
import org.strongback.Executor.Priority;
import org.strongback.components.Motor;
import org.strongback.hardware.HardwareTalonSRX;

import frc.robot.Constants;
import frc.robot.interfaces.NetworkTableHelperInterface;
import org.strongback.mock.MockUpdateTimer;

/**
 *  Whenever a motor is created, networkTable for the motor PIDF values are implemented from here.
 *  The PIDF values are initiliased on the network table and the motors on the robot grabs the PIDF
 * 	values once a second.
 */

 class TunableMotor implements Executable {
	 Motor motor;
	 ArrayList<HardwareTalonSRX> motorLists;
	 int id;
	NetworkTableHelperInterface networkTable;
	MockUpdateTimer startTime = new MockUpdateTimer();
	public TunableMotor(Motor motor, int id, double p, double i , double d , double f) {
		this.id = id;
		startTime.init();
	}
	
	public static void tuneMotor(Motor  motor, int id, double p, double i , double d , double f, NetworkTablesHelper networkTable) {
		var tunable = new TunableMotor(motor, id, p, i, d, f);
		networkTable.set("p", p);
		networkTable.set("i", i);
		networkTable.set("d", d);
		networkTable.set("f", f);
		motor.setPIDF(0, p, i, d, f);
		Strongback.executor().register(tunable, Priority.LOW);
	}

	// Executes the command 1 time every 1 second.
	@Override
	public void execute(long timeInMillis) {
		if(startTime.diff() >= Constants.TIME_TUNABLEMOTOR_PERIOD) {
			double p = networkTable.set("p", Constants.DRIVE_P);
			double i = networkTable.set("i", Constants.DRIVE_I);
			double d = networkTable.set("d", Constants.DRIVE_D);
			double f = networkTable.set("f", Constants.DRIVE_F);
			motor.setPIDF(0, p, i, d, f);
			startTime.init();
		}

	}

 }