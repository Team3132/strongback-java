package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.LEDColour;

// LED Strip Subsystem 2020

public class LEDStrip implements LEDStripInterface {
    public AddressableLED ledStrip;
    public AddressableLEDBuffer ledStripBuffer;
    private final int numberOfLEDs;

    public LEDStrip(int PWM_Port, int numberOfLEDs, Log log) {   
        this.numberOfLEDs = numberOfLEDs;

        ledStrip = new AddressableLED(PWM_Port);
        ledStripBuffer = new AddressableLEDBuffer(numberOfLEDs);
        ledStrip.setLength(ledStripBuffer.getLength());
    
        // Set the data
        ledStrip.setData(ledStripBuffer);
        ledStrip.start();

    }

    @Override
    public void setColour(LEDColour c) {
        for (int i = 0; i < numberOfLEDs; i++) {
            ledStripBuffer.setRGB(i, c.r, c.g, c.b);
        }
        setData();
    }

    /**
     * Input 2 colours and an int from 0-100 like a percentage.
     * The first colour will fill in that percentage of the led strip and the rest will be filled in
     * with the second colour.
     */
    @Override
    public void setProgressColour(LEDColour c1, LEDColour c2, int percent) {
        percent = cap(percent, 0, 100); //Capping range from 0 - 100
        int leds = (percent * numberOfLEDs) / 100;
        for (int i = 0; i < leds; i++) {
            setLEDColour(i, c1); 
        }
        for (int i = leds; i < numberOfLEDs; i++) {
            setLEDColour(i, c2);
        }
        setData();
    }

    @Override
    public void setIdle() {
        for (int i = 0; i < numberOfLEDs; i++) {
            setLEDColour(i, i % 2 == 0 ? LEDColour.GOLD : LEDColour.GREEN);
        }
        setData();
    } 

    private void setData() {
        ledStrip.setData(ledStripBuffer);
    }

    private void setLEDColour(int index, LEDColour c) {
        ledStripBuffer.setRGB(index, c.r, c.g, c.b);
    }

    private int cap(int value, int min, int max) {
        return Math.max(0, Math.min(100, value));
    }
}