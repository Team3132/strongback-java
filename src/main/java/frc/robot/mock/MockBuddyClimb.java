import frc.robot.interfaces.BuddyClimbInterface;
import frc.robot.interfaces.Log;

public class MockBuddyClimb implements BuddyClimbInterface {
    private boolean isExtended = false;

    public MockBuddyClimb(Log log) {
    }

    @Override
	public BuddyClimbInterface setExtended(boolean extended) {
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
	public String getName() {
		return "MockBuddyClimb";
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