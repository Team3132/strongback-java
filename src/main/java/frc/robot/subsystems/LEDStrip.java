package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

// LED Strip Subsystem 2020

public class LEDStrip extends Subsystem implements LEDStripInterface, DashboardUpdater {
    private int PWM_Port;
    private int numberOfLEDs;

    public AddressableLED ledStrip;
    public AddressableLEDBuffer ledStripBuffer;
    

    public LEDStrip(int PWM_Port, int numberOfLEDs, DashboardInterface dashboard, Log log) {
        super("LED Strip", dashboard, log);   
        this.PWM_Port = PWM_Port;
        this.numberOfLEDs = numberOfLEDs;

        ledStrip = new AddressableLED(this.PWM_Port);
        ledStripBuffer = new AddressableLEDBuffer(this.numberOfLEDs);
        ledStrip.setLength(ledStripBuffer.getLength());
    
        // Set the data
        ledStrip.setData(ledStripBuffer);
        ledStrip.start();

    }

    public LEDStripInterface setColour(int index, int r, int g, int b) {
        ledStripBuffer.setRGB(index, r, g, b);
        return this;
    }

    public LEDStripInterface setColour(int indexS, int indexE, int r, int g, int b) {
        for (int i = indexS; i < indexE; i++ ) {
            ledStripBuffer.setRGB(i, r, g, b);  
        }
        return this;
    }

    public LEDStripInterface setData() {
        ledStrip.setData(ledStripBuffer);
        return this;
    }

    public void updateDashboard() {
		// do nothing by default
	}
    
}