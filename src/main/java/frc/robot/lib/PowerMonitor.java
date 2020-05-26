package frc.robot.lib;

import frc.robot.interfaces.DashboardUpdater;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
/**
 *	Class to monitor the Power Distribution Panel.
 * This allows us to track if motors are stalling, and to observe where the power is going on the robot.
 * 
 * Currently we don't use this information in interesting ways, we just log it for post match diagnosis.
 */
public class PowerMonitor implements DashboardUpdater {		// no interface, as this is a purely hardware class.
	
	private final boolean enabled;
	/*
	 * REX: We should only sample the values that are "interesting". OR, we should sample slower, with another thread.
	 * I believe that sampling too fast is what is causing the CAN bus timeouts.
	 */
	
	PowerDistributionPanel pdp;
	
	public PowerMonitor (PowerDistributionPanel pdp, int[] channelsToMonitor, boolean enabled) {
		final String name = "Power";
		this.pdp = pdp;
		this.enabled = enabled;
		if (!enabled) {
			Log.debug("PDP not enabled");
			return;
		}
		Log.debug("PDP enabled");
		Chart.register((() -> { return pdp.getTotalEnergy(); } ), "%s/totalEnergy", name);
		Chart.register((() -> { return pdp.getTotalPower(); } ), "%s/totalPower", name);
		Chart.register((() -> { return pdp.getTotalCurrent(); } ), "%s/totalCurrent", name);
		Chart.register((() -> { return pdp.getTemperature(); } ), "%s/temperature", name);
		Chart.register((() -> { return pdp.getVoltage(); } ), "%s/inputVoltage", name);
		for (int i = 0; i < channelsToMonitor.length; i++) {
			final int channel = channelsToMonitor[i];
			Chart.register((() -> { return pdp.getCurrent(channel); } ), "%s/channelCurrent/%d", name, channel);
		}
	}

	@Override
	public void updateDashboard() {
		if (!enabled) return;
		SmartDashboard.putString("PDP Voltage", String.format("%.1f", pdp.getVoltage()));
	}
}

