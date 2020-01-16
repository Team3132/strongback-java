package frc.robot.lib;

import java.util.ArrayList;
import java.util.HashMap;

import com.ctre.phoenix.ErrorCode;

import org.strongback.Executable;
import org.strongback.components.Clock;
import org.strongback.components.Motor;
import org.strongback.components.TalonSensorCollection;
import org.strongback.hardware.HardwareTalonSRX;

import frc.robot.interfaces.Log;

/**
 * Creates what appears to be a single TalonSRX, but is really a list of
 * them, with some of them being able to lead (ie has encoder), and some
 * only able to follow.
 * 
 * Encoder speed and current are monitored for failure.
 * 
 * Logs when leadership changes or talons are disabled due to high current.
 * 
 * Most of this file is plumbing the TalonSRX methods through to one or more
 * of the underlying talons.
 */
public class RedundantTalonSRX extends HardwareTalonSRX implements Motor, TalonSensorCollection, Executable {

	private ArrayList<HardwareTalonSRX> potentialLeaders;  // All leaders.
	private ArrayList<HardwareTalonSRX> followers;
	private HardwareTalonSRX leader;
	private TalonSensorCollection sensorCollection;
	private ArrayList<HardwareTalonSRX> otherLeaders;  // All possible leaders that aren't currently leading.
	private ArrayList<HardwareTalonSRX> activeTalons; // All possible leaders and the followers.
	private static ArrayList<HardwareTalonSRX> badTalons = new ArrayList<>();  // Talons that have drawn so much current.
	private static ArrayList<HardwareTalonSRX> badEncoders = new ArrayList<>(); // Talons that have bad encoders.
	private Clock clock;
	private static Log log;
	
	// Current draw.
	// Record how long each talon has been an outlier for current draw. If they are long enough,
	// Then they get disabled for safety.
	private HashMap<HardwareTalonSRX, Double> currentOutlierSinceSec = new HashMap<HardwareTalonSRX, Double>();
	public static final double kCurrentOutlierThresholdAmps = 5;
	public static  final double kCurrentOutlierDisableTimeSec = 1;
	
	// Encoder check thresholds
	private HashMap<HardwareTalonSRX, Double> speedOutlierSinceSec = new HashMap<HardwareTalonSRX, Double>();
	public static  final double kSpeedOutlierAbsoluteThresholdTicks = 100;
	public static  final double kSpeedOutlierMinimumRatio = 0.9;  // Encoders returning less than 90% of ticks are bad.
	public static  final double kSpeedOutlierDisableTimeSec = 1;
	
	private final double kMinLoggingIntervalSec = 4;  // Only log message once every n seconds.
	
	// Remember the last values from set(...) so that when the leadership is changed
	// the new leader can be told what to do until set() is called again.
	ControlMode lastMode = ControlMode.Velocity;
	double lastDemand0 = 0;
	
	public RedundantTalonSRX(ArrayList<HardwareTalonSRX> potentialLeaders, ArrayList<HardwareTalonSRX> followers, Clock clock, Log log) {
		super(potentialLeaders.get(0).getTalonSRX());
		this.potentialLeaders = potentialLeaders;
		this.followers = followers;
		this.clock = clock;
		RedundantTalonSRX.log = log;
		activeTalons = new ArrayList<HardwareTalonSRX>();
		activeTalons.addAll(potentialLeaders);
		activeTalons.addAll(followers);
		otherLeaders = new ArrayList<HardwareTalonSRX>();
		changeLeader(0);
		for(HardwareTalonSRX talon : activeTalons) {
			log.register(false, () -> talon.getOutputCurrent(), "Talons/%d/Current", talon.getDeviceID());
		}
		log.register(false, () -> (double)badEncoders.size(), "RedundantTalons/numBadEncoders");
		log.register(false, () -> (double)badTalons.size(), "RedundantTalons/numBadTalons");
		// Ensure execute gets called to check the talons/encoders.
		// Disable for performance.
    	//Strongback.executor().register(this, Priority.LOW);
	}	
	
	public static ArrayList<HardwareTalonSRX> getBadEncoders() {
		return badEncoders;
	}

	public static ArrayList<HardwareTalonSRX> getBadTalons() {
		return badTalons;
	}
	
	public static void clearFailures() {
		badEncoders.clear();
		badTalons.clear();
	}

	@Override
	public void execute(long timeInMillis) {
		if (activeTalons.size() < 2) return;
		checkCurrentDraw();
		checkEncoders();
	}
	
	/**
	 * Check the current draw of each talon against the average of all the
	 * talons and disable it if is too different.
	 * @param clock for the current time.
	 */
	public void checkCurrentDraw() {
		final double nowSec = clock.currentTime();
		// Walk through all talons and if there is an outlier, then flag it
		// as being broken so it can be disabled.
		HashMap<HardwareTalonSRX, Double> currentHash = new HashMap<HardwareTalonSRX, Double>();
		double sum = 0;
		int count = 0;
		for (HardwareTalonSRX talon : activeTalons) {
			double current = Math.abs(talon.getOutputCurrent());
			currentHash.put(talon, current);
			sum += current;
			count++;
		}
		if (count < 2) return;  // Nothing to do here.
		double average = sum/count;
		// Look for any talon that is consistently drawing more than 5A than the rest.
		// Iterate over the currentHash instead of activeTalons so activeTalons
		// can be mutated.
		for (HardwareTalonSRX talon: currentHash.keySet()) {
			double difference = currentHash.get(talon) - average;
			if (difference < kCurrentOutlierThresholdAmps) {
				// Not an outlier, remove it if it has been flagged in the past.
				currentOutlierSinceSec.remove(talon);
				continue;
			}
			// Now an outlier, add it to the outlier list if it's not already there.
			if (!currentOutlierSinceSec.containsKey(talon)) {
				// New entry.
				currentOutlierSinceSec.put(talon, nowSec);
				continue;
			}
			// Existing entry, check it's age.
			double ageSec = nowSec - currentOutlierSinceSec.get(talon);
			if (ageSec > kCurrentOutlierDisableTimeSec) {
				if (talon == leader && otherLeaders.size() == 0) {
					limitedError("current: only leader",
							"TalonSRX with CAN ID %d is drawing %f amps more than the average draw but is the only leader so it can't be disabled",
							talon.getDeviceID(), difference);
					currentOutlierSinceSec.remove(talon);  // Remove it so that there is only one log message per interval.
					continue;
				}
				limitedError(String.format("cur: %d", talon.getDeviceID()),
						"TalonSRX with CAN ID %d is drawing %f amps more than the average draw",
						talon.getDeviceID(), difference);
				disableTalon(talon);
			}
		}
	}
	
	/**
	 * Check the current draw of each talon against the average of all the
	 * talons and disable it if is too different.
	 * @param clock for the current time.
	 */
	public void checkEncoders() {
		final double nowSec = clock.currentTime();
		// Walk through all potential leaders and if there is an outlier,
		// then flag it as being broken so that the leadership can be changed..
		HashMap<HardwareTalonSRX, Double> speedHash = new HashMap<HardwareTalonSRX, Double>();
		double sum = 0;
		int count = 0;
		for (HardwareTalonSRX talon : potentialLeaders) {
			double speed = Math.abs(talon.getVelocity());
			speedHash.put(talon, speed);
			sum += speed;
			count++;
		}
		if (count < 2) return;  // Nothing to do here.
		double average = sum/count;
		// Ignore values if the average is too low.
		if (average < kSpeedOutlierAbsoluteThresholdTicks) return; // Going too slow.
		// Look for any encoder that is returning .
		// Iterate over the currentHash instead of activeTalons so activeTalons
		// can be mutated.
		for (HardwareTalonSRX talon: speedHash.keySet()) {
			double ratio = speedHash.get(talon) / average;
			if (ratio > kSpeedOutlierMinimumRatio) {
				// Returning at least ~90% of the ticks.
				// Not an outlier, remove it if it has been.
				speedOutlierSinceSec.remove(talon);
				continue;
			}
			// Now an outlier, add it to the outlier list if it's not already there.
			if (!speedOutlierSinceSec.containsKey(talon)) {
				// New entry.
				speedOutlierSinceSec.put(talon, nowSec);
				continue;
			}
			// Existing entry, check it's age.
			double ageSec = nowSec - speedOutlierSinceSec.get(talon);
			if (ageSec > kSpeedOutlierDisableTimeSec) {
				if (talon == leader && otherLeaders.size() == 0) {
					limitedError("encoder: only leader",
							"The encoder on TalonSRX with CAN ID %d is reporting %.1f%% of the average value, but is the only leader so it can't be disabled",
							talon.getDeviceID(), 100 * ratio);
					speedOutlierSinceSec.remove(talon);  // Remove it so that there is only one log message per interval.
					continue;
				}
				limitedError(String.format("enc: %d", talon.getDeviceID()),
						"The encoder on TalonSRX with CAN ID %d is reporting %.1f%% of the average speed - that's bad",
						talon.getDeviceID(), 100 * ratio);
				disableEncoder(talon);
			}
		}
	}

	/**
	 * Log error message if it hasn't been logged in the last kMinLoggingIntervalSec.
	 * The error message is the same if it has the same key.
	 * @param key uniquely identifies this error message.
	 * @param msg message to put in the log.
	 */
	private void limitedError(String key, String msg, Object... args) {
		double now = clock.currentTime();
		// Has it been long enough since the last log message?
		if (now - lastErrorTime.getOrDefault(key, 0.) < kMinLoggingIntervalSec) return;
		// It has. Log now.
		log.error(msg, args);
		// Update the last log time.
		lastErrorTime.put(key, now);		
	}
	private HashMap<String, Double> lastErrorTime = new HashMap<String, Double>();
		
	public void disableTalon(HardwareTalonSRX talon) {
		badTalons.add(talon);
		potentialLeaders.remove(talon);
		activeTalons.remove(talon);
		otherLeaders.remove(talon);
		followers.remove(talon);
		// If it was the leader, then find a new leader.
		if (leader == talon) {
			changeLeader(0);
		}
		// Disable it now that it's not the leader.
		talon.set(ControlMode.Disabled, 0);
		log.error("Disabled TalonSRX with CAN ID %d", talon.getDeviceID());
	}
	
	/**
	 * Remove this talon/encoder from the list of potential leaders and
	 * switch leadership away if it's the current leader.
	 * @param talon the talon to ignore the encoder for.
	 */
	public void disableEncoder(HardwareTalonSRX talon) {
		badEncoders.add(talon);
		potentialLeaders.remove(talon);
		otherLeaders.remove(talon);
		followers.add(talon);  // It can now only follow.
		// If it was the leader, then find a new leader.
		if (leader == talon) {
			changeLeader(0);
			log.error("Bad encoder on leader TalonSRX %d, changed leadership", talon.getDeviceID());
		} else {
			log.error("Converted potential leader TalonSRX %d with bad encoder to a follower only", talon.getDeviceID());

		}
		// It will have been setup as a follower now.
	}
	
	public void changeLeader(int index) {
		assert(index < potentialLeaders.size());
		otherLeaders.clear();
		for (int i=0; i<potentialLeaders.size(); i++) {
			if (i == index) {
				leader = potentialLeaders.get(i);
				sensorCollection = leader.getSensorCollection();
				continue;
			}
			otherLeaders.add(potentialLeaders.get(i));
		}
		// Tell the new leader not to follow anyone by giving it a non-follow
		// instruction.
		leader.set(lastMode, lastDemand0);
		// Tell the rest to follow the new leader.
		otherLeaders.forEach((talon) -> talon.set(ControlMode.Follower, leader.getDeviceID()));
		followers.forEach((talon) -> talon.set(ControlMode.Follower, leader.getDeviceID()));
	}
	
	/**
	 * Print out any issues seen across all RedundantTalonSRX's.
	 */
	public static void printStatus() {
		if (log == null) return;
		if (!badEncoders.isEmpty()) {
			log.error("The following talons have a bad encoder:");
			for (HardwareTalonSRX talon : badEncoders) {
				log.error("  Talon SRX %d", talon.getDeviceID());
			}
		}
		if (!badTalons.isEmpty()) {
			log.error("The following talons or motors have drawn a large amount of current:");
			for (HardwareTalonSRX talon : badTalons) {
				log.error("  Talon SRX %d", talon.getDeviceID());
			}
		}
		if (badTalons.isEmpty() && badEncoders.isEmpty()) {
			log.info("No bad talons or encoders found");
		}
	}
	
	// Main entry point for talon.
	
	@Override
	public void set(ControlMode mode, double demand) {
		lastMode = mode;
		lastDemand0 = demand;
		// Master only.
		leader.set(mode, demand);
	}

	// Boring pass-thru for each of the methods follows. //////////////////////
	
	@Override
	public Motor setSensorPhase(boolean phaseSensor) {
		otherLeaders.forEach((talon) -> talon.setSensorPhase(phaseSensor));
		leader.setSensorPhase(phaseSensor);
		return this;
	}

	@Override
	public Motor setInverted(boolean invert) {
		otherLeaders.forEach((talon) -> talon.setInverted(invert));
		leader.setInverted(invert);
		return this;
	}

	@Override
	public boolean getInverted() {
		return leader.getInverted();
	}

	@Override
	public double getBusVoltage() {
		return leader.getBusVoltage();
	}

	@Override
	public double getOutputVoltage() {
		return leader.getOutputVoltage();
	}

	@Override
	public double getOutputCurrent() {
		return leader.getOutputCurrent();
	}

	@Override
	public double getTemperature() {
		return leader.getTemperature();
	}


	@Override
	public double getPosition() {
		return leader.getPosition();
	}

	@Override
	public double getVelocity() {
		return leader.getVelocity();
	}

	@Override
    public Motor setPIDF(int slotIdx, double p, double i, double d, double f) {
		return leader.setPIDF(slotIdx, p, i, d, f);
	}

	@Override
	public Motor setScale(double scale) {
		otherLeaders.forEach((talon) -> talon.setScale(scale));
		return leader.setScale(scale);
	}

	///// TalonSensorCollection methods from here.
	// Note that this is done in case someone stores the sensor collection of the leader then
	// and the leadership changes.
	
	@Override
	public int getAnalogIn() {
		return sensorCollection.getAnalogIn();
	}

	@Override
	public int getAnalogInRaw() {
		return sensorCollection.getAnalogInRaw();
	}

	@Override
	public int getAnalogInVel() {
		return sensorCollection.getAnalogInVel();
	}

	@Override
	public boolean getPinStateQuadA() {
		return sensorCollection.getPinStateQuadA();
	}

	@Override
	public boolean getPinStateQuadB() {
		return sensorCollection.getPinStateQuadB();
	}

	@Override
	public boolean getPinStateQuadIdx() {
		return sensorCollection.getPinStateQuadIdx();
	}

	@Override
	public int getPulseWidthPosition() {
		return sensorCollection.getPulseWidthPosition();
	}

	@Override
	public int getPulseWidthRiseToFallUs() {
		return sensorCollection.getPulseWidthRiseToFallUs();
	}

	@Override
	public int getPulseWidthRiseToRiseUs() {
		return sensorCollection.getPulseWidthRiseToRiseUs();
	}

	@Override
	public int getPulseWidthVelocity() {
		return sensorCollection.getPulseWidthVelocity();
	}

	@Override
	public double getQuadraturePosition() {
		return sensorCollection.getQuadraturePosition();
	}

	@Override
	public int getQuadratureVelocity() {
		return sensorCollection.getQuadratureVelocity();
	}

	@Override
	public boolean isFwdLimitSwitchClosed() {
		return sensorCollection.isFwdLimitSwitchClosed();
	}

	@Override
	public boolean isRevLimitSwitchClosed() {
		return sensorCollection.isRevLimitSwitchClosed();
	}

	@Override
	public ErrorCode setAnalogPosition(int newPosition, int timeoutMs) {
		return sensorCollection.setAnalogPosition(newPosition, timeoutMs);
	}

	@Override
	public ErrorCode setPulseWidthPosition(int newPosition, int timeoutMs) {
		return sensorCollection.setPulseWidthPosition(newPosition, timeoutMs);
	}

	@Override
	public ErrorCode setQuadraturePosition(double newPosition, int timeoutMs) {
		return sensorCollection.setQuadraturePosition(newPosition, timeoutMs);
	}
}
