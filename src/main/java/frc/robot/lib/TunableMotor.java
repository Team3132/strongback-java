package frc.robot.lib;

import org.strongback.Executable;
import org.strongback.Executor.Priority;
import org.strongback.Strongback;
import org.strongback.components.Motor;

import frc.robot.Constants;
import frc.robot.interfaces.NetworkTableHelperInterface;

/**
 *  Whenever a motor is created, networkTable for the motor PIDF values are implemented from here.
 *  The PIDF values are initiliased on the network table and and the motors are updated from the PIDF 
 *  values in the network tables once per second. This allows the values to be updated on the fly by changing 
 * the values in the network tables.
 */

 class TunableMotor implements Executable {
	private final Motor motor;
	private final NetworkTableHelperInterface networkTable;
	private double lastUpdateSec = 0;

	public static void tuneMotor(Motor  motor, int id, double p, double i , double d , double f, NetworkTablesHelper networkTable) {
		var tunable = new TunableMotor(motor, id, p, i, d, f, networkTable);
		networkTable.set("p", p);
		networkTable.set("i", i);
		networkTable.set("d", d);
		networkTable.set("f", f);
		Strongback.executor().register(tunable, Priority.LOW);
	}

	public TunableMotor(Motor motor, int id, double p, double i, double d, double f, NetworkTablesHelper networkTable) {
		this.motor = motor;
		this.networkTable = networkTable;
	}
	
	// Executes the command 1 time every 1 second.
	@Override
	public void execute(long timeInMillis) {
		double now = Strongback.timeSystem().currentTime();
		if (now < lastUpdateSec + Constants.DASHBOARD_UPDATE_INTERVAL_SEC)
			return;
		update();
		lastUpdateSec = now;
	}

	private void update() {
		double p = networkTable.get("p", Constants.DRIVE_P);
		double i = networkTable.get("i", Constants.DRIVE_I);
		double d = networkTable.get("d", Constants.DRIVE_D);
		double f = networkTable.get("f", Constants.DRIVE_F);
		motor.setPIDF(0, p, i, d, f);
	}
}