package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.LEDStripInterface.LEDAction.Type;
import frc.robot.lib.Subsystem;

// LED Strip Subsystem 2020

public class LEDStrip extends Subsystem implements LEDStripInterface {
    private int PWM_Port;
    private int numberOfLEDs;
    private long startTime;
    private boolean firstLoop = true;
    private LEDAction action = new LEDAction(Type.NONE);

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

    @Override
    public void execute(long timeInMillis) {
        switch (action.type) {
        case POSTPROGRESS:
            break;
        case NONE:
            setDefault();
            break;
        default:
            log.error("%s: Unknown Type %s", name, action.type);
            break;
        }
        setData();
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

    // TODO: Set all
}
     public LEDStripInterface setColour(int index, int r, int g, int b) {
         ledStripBuffer.setRGB(index, r, g, b);




}
    public LEDStripInterface setProgressColour(int current, int total) {
        int leds = current/total*numberOfLEDs-1;
        if (current != total){
            setColour(0, leds, 255, 0, 0);
        } else {
            if (firstLoop) {
                startTime = System.currentTimeMillis(); // TODO: use clock instead of system
                firstLoop = false;
            }
            if ((System.currentTimeMillis() - startTime) < 1000) {
                setColour(0, numberOfLEDs, 0, 255, 0);
            } else {
                setDefault();
            }
        }

        return this;
    }

    public LEDStripInterface setDefault() {
        ledStrip.stop();
        return this;
    } 

    public LEDStripInterface setData() {
        ledStrip.setData(ledStripBuffer);
        return this;
    }

    public void updateDashboard() {
		// do nothing by default
    }
    

    @Override
    public LEDStripInterface setDesiredAction(LEDAction action) {
        this.action = action;
        firstLoop = true;
        return this;
    }
}