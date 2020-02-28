package frc.robot.lib;

import org.strongback.Executable;
import org.strongback.Strongback;
import org.strongback.Executor.Priority;
import org.strongback.components.Motor;

import frc.robot.Constants;
import frc.robot.interfaces.NetworkTableHelperInterface;

/**
 * Code to allow us to log output current per Spark MAX and set up followers so that
 * it appears as a single motor but can be an arbitary number of motors configured
 * in the per robot configuration.
 * @param canIDs list of canIDs. First entry is the leader and the rest follow it.
 * @param invert change the direction.
 * @param log for registration of the current reporting.
 * @return the leader HardwareTalonSRX
 */

 class TunableMotor implements Executable {
	 Motor motor;
	 int id;
	 Motor left;
	NetworkTableHelperInterface networkTable;
	public TunableMotor(String name, Motor motor, int id, double p, double i , double d , double f) {
		this.motor = motor;
		this.id = id;
		networkTable.get("p",  p);
		networkTable.get("i", i);
		networkTable.get("d", d);
		networkTable.get("f", f);
		left.setPIDF(0, p, i, d, f);

	}

	public static void tuneMotor(String name, Motor motor, int id, double p, double i , double d , double f) {
		var tunable = new TunableMotor(name, motor, id, Constants.DRIVE_P, Constants.DRIVE_I, Constants.DRIVE_D, Constants.DRIVE_F);
		Strongback.executor().register(tunable, Priority.LOW);
	}

	@Override
	public void execute(long timeInMillis) {
		// TODO Auto-generated method stub

	}
 }