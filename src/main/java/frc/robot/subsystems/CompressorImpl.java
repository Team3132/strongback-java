package frc.robot.subsystems;

import org.strongback.components.PneumaticsModule;
import org.strongback.components.Relay;
import frc.robot.interfaces.Compressor;


/**
 * Subsystem responsible for the for the pneumatic compressor.
 */
public class CompressorImpl implements Compressor {
	private Relay relay;
	public CompressorImpl(PneumaticsModule compressor) {
		this.relay = compressor.automaticMode();
	}
	
	@Override
	public void turnOn() {
		relay.on();
	}
	
	@Override
	public void turnOff() {
		relay.off();
	}
	
	@Override
	public boolean isOn() {
		return relay.isOn();
	}
}

