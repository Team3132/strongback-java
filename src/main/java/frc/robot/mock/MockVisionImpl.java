package frc.robot.mock;

import frc.robot.interfaces.Vision;

public class MockVisionImpl implements Vision {

	TargetDetails details = new TargetDetails();

	public MockVisionImpl() {
		details.targetFound = false;
		details.imageTimestamp = 0;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public TargetDetails getTargetDetails() {
		return details;
	}
}
