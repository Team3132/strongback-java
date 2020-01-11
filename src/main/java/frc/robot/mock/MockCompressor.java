package frc.robot.mock;

import frc.robot.interfaces.CompressorInterface;

public class MockCompressor implements CompressorInterface {

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