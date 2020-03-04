package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LEDStripInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.LEDColour;
import frc.robot.lib.NetworkTablesHelper;
import frc.robot.lib.Subsystem;

public class Loader extends Subsystem implements LoaderInterface {
    final private Motor spinner, passthrough;
    final private Solenoid paddleSolenoid;
    private double spinnerRPM = 0;
    final Counter inSensorCount;
    final Counter outSensorCount;
    private int initBallCount = 0;
    final private LEDStripInterface led;
    private double targetPassthroughMotorOutput;

    public Loader(final Motor loaderSpinnerMotor, final Motor loaderPassthroughMotor, final Solenoid paddleSolenoid,
            final BooleanSupplier inSensor, final BooleanSupplier outSensor, LEDStripInterface led, final DashboardInterface dashboard,
            final Log log) {
        super("Loader", dashboard, log);
        this.led = led;
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.paddleSolenoid = paddleSolenoid;
        inSensorCount = new Counter("loader:inSensor", inSensor);
        outSensorCount = new Counter("loader:outSensor", outSensor);
        SmartDashboard.putBoolean("In Sensor State", inSensor.getAsBoolean());
        SmartDashboard.putBoolean("Out Sensor State", outSensor.getAsBoolean());

        log.register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
                .register(true, () -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name)
                .register(true, () -> spinner.getVelocity(), "%s/spinner/RPM", name)
                .register(true, () -> spinner.getOutputCurrent(), "%s/spinner/Current", name)
                .register(true, () -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name)
                .register(true, () -> (double) getCurrentBallCount(), "%s/spinner/CurrentBallCount", name)
                .register(true, () -> (double) inSensorCount.count, "%s/spinner/totalBallsIn", name)
                .register(true, () -> (double) outSensorCount.count, "%s/spinner/totalBallsOut", name)
                .register(true, () -> (double) initBallCount, "%s/spinner/initialBallCount", name)
                .register(true, () -> isPaddleBlocking(), "%s/paddleRetracted", name)
                .register(true, () -> inSensor.getAsBoolean(), "%s/spinner/inSensorState", name)
                .register(true, () -> outSensor.getAsBoolean(), "%s/spinner/outSensorState", name);
    }

    @Override
    public void setTargetSpinnerMotorRPM(final double rpm) {

        spinnerRPM = rpm;
        log.sub("%s: Setting loader motor rpm to: %f", name, rpm);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        if (rpm == 0) {
            spinner.set(ControlMode.PercentOutput, 0);
        } else {
            spinner.set(ControlMode.Velocity, rpm);
        }
    }

    @Override
    public void setTargetPassthroughMotorOutput(double percent) {
        log.sub("%s: Setting loader in motor percent output to: %f", name, percent);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        targetPassthroughMotorOutput = percent;
        passthrough.set(ControlMode.PercentOutput, percent);

    }
    
    @Override
    public void setInitBallCount(int initBallCount) {
        this.initBallCount = initBallCount;
    }

    public LoaderInterface setPaddleNotBlocking(final boolean extend) {
        if (extend) {
            paddleSolenoid.extend();
        } else {
            paddleSolenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isPaddleNotBlocking() {
        // log.sub("Is intake extended: " + solenoid.isExtended());
        return paddleSolenoid.isExtended();
    }

    @Override
    public boolean isPaddleBlocking() {
        return paddleSolenoid.isRetracted();
    }

    @Override
    public void execute(final long timeInMillis) {
        
        inSensorCount.execute(0);
        outSensorCount.execute(0);
        
        // Don't update all the time because the colour wheel may want to use the LEDs
        if (spinner.getSpeed() > 1) { 
            led.setProgressColour(LEDColour.PURPLE, LEDColour.WHITE, getCurrentBallCount() / 5 * 100);
        }
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader Paddle position",
                isPaddleNotBlocking() ? "not blocking" : isPaddleBlocking() ? "blocking" : "moving");
        dashboard.putNumber("Loader spinner velocity", spinner.getVelocity());
        dashboard.putNumber("Loader passthrough percent output", passthrough.getOutputPercent());
        dashboard.putNumber("Current Number of Balls", getCurrentBallCount());
        inSensorCount.updateDashboard();
        outSensorCount.updateDashboard();
    }

    @Override
    public void disable() {
        spinner.set(ControlMode.PercentOutput, 0);
        passthrough.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public int getCurrentBallCount() {
        return inSensorCount.getCount() - outSensorCount.getCount() + initBallCount;
    }

    @Override
    public double getTargetSpinnerMotorRPM() {
        return spinnerRPM;
    }

    @Override
    public double getTargetPassthroughMotorOutput() {
        log.sub("%s: passthrough output is currently %f", name, targetPassthroughMotorOutput);
        return targetPassthroughMotorOutput;
    }

    private class Counter implements DashboardUpdater, Executable {
        final private String name;
        final private BooleanSupplier sensor;
        private int count = 0;
        private boolean lastSensorReading = false;

        public Counter(final String name, final BooleanSupplier sensor) {
            this.name = name;
            this.sensor = sensor;
            log.register(false, () -> (double) getCount(), "%s/count", name);
        }

        public int getCount() {
            return count;
        }

        @Override
        public void execute(final long timeInMillis) {
            final boolean sensorReading = sensor.getAsBoolean();
            if (sensorReading && !lastSensorReading)
                count++;
            lastSensorReading = sensorReading;
        }

        @Override
        public void updateDashboard() {
            dashboard.putNumber(name + " ball count", getCount());
        }
    }
}
