package frc.robot.mock;

import frc.robot.interfaces.BuddyClimb;

public class MockBuddyClimb implements BuddyClimb {
    private boolean isExtended = false;

    public MockBuddyClimb() {
    }

    @Override
	public BuddyClimb setExtended(boolean extended) {
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