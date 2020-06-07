package frc.robot.mock;

import frc.robot.interfaces.Compressor;

public class MockCompressorImpl implements Compressor {

	private boolean enabled = true;
	@Override
	public void turnOn() {
		enabled = true;
	}

	@Override
	public void turnOff() {
		enabled = false;
	}

	@Override
	public boolean isOn() {
		return enabled;
	}
}