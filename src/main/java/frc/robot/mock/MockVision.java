package frc.robot.mock;

import frc.robot.interfaces.VisionInterface;

public class MockVision implements VisionInterface {

	TargetDetails details = new TargetDetails();

	public MockVision() {
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
