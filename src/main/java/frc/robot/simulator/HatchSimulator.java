package frc.robot.simulator;

import frc.robot.Constants;
import frc.robot.interfaces.HatchInterface;
import frc.robot.lib.MovementSimulator;
import frc.robot.interfaces.Log;

/**
 * Very basic hatch simulator used for unit testing.
 * Does not do gravity etc.
 */
public class HatchSimulator implements HatchInterface {
	private String name = "HatchSimulator";
	private Log log;
	
	private final double kMaxSpeed = 5;  // inches/sec
	private final double kMaxAccel = 2;   // inches/sec/sec
	private final double kMinPos = 0;
	private final double kMaxPos = 2 * 12; // Two feet wide.
	private final double kTolerance = 0.5;
	private HatchAction action = new HatchAction(HatchAction.Type.CALIBRATE, 0);
	private MovementSimulator posCalc = new MovementSimulator("hatchPos", kMaxSpeed, kMaxAccel, kMinPos, kMaxPos, kTolerance);
	private boolean isHeld = false;
		
	public HatchSimulator(Log log) {
		this.log = log;
	}

    @Override
    public void setAction(HatchAction action) {
		this.action = action;
		double targetPosition = posCalc.getTargetPos();
		switch (action.type) {
		case SET_POSITION:
			targetPosition = action.value;
			break;
		case ADJUST_POSITION:
			targetPosition += action.value;
		default:
			return;
		}
		if (posCalc.getTargetPos() == targetPosition)
			return;
		log.sub("%s Setting hatch position to %.1f\n", name, targetPosition);
		posCalc.setTargetPos(targetPosition);

    }

    @Override
    public HatchAction getAction() {
        return action;
    }
	
	/**
	 * Overrides the hatch position ignoring time and simulator.
	 * @param postion
	 * @return
	 */
	public HatchInterface setHatchHeightActual(double position) {
		posCalc.setPos(position);
		posCalc.setSpeed(0);  // Reset speed.
		return this;
	}

	@Override
	public boolean isInPosition() {
		return Math.abs(posCalc.getPos() - posCalc.getTargetPos()) < Constants.HATCH_POSITION_TOLERANCE;
	}

	@Override
	public boolean setHeld(boolean held) {
		// These should simulate a solenoid/cyclinder taking time to move...
		isHeld = held;
		return isHeld;
	}

	@Override
	public boolean getHeld() {
		return isHeld;
	}
		
	@Override
	public boolean getReleased() {
		return !isHeld;
	}
		
	@Override
	public String toString() {
		return posCalc.toString();
	}

	@Override
	public String getName() {
		return "HatchSimulator";
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
		return true;
	}

	@Override
	public void cleanup() {
	}
}