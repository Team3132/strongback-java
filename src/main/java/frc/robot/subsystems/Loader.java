package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

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
    private double spinnerVelocity = 0;
    final Counter inSensorCount;
    final Counter outSensorCount;
    private int initBallCount = 0;
    final private LEDStripInterface led;

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

        log.register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
                .register(true, () -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name)
                .register(true, () -> spinner.getVelocity(), "%s/spinner/Velocity", name)
                .register(true, () -> spinner.getOutputCurrent(), "%s/spinner/Current", name)
                .register(true, () -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name)
                .register(true, () -> (double) getCurrentCount(), "%s/spinner/CurrentBallCount", name)
                .register(true, () -> (double) inSensorCount.count, "%s/spinner/totalBallsIn", name)
                .register(true, () -> (double) outSensorCount.count, "%s/spinner/totalBallsOut", name)
                .register(true, () -> (double) initBallCount, "%s/spinner/initialBallCount", name)
                .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name);
    }

    @Override
    public void setTargetSpinnerMotorVelocity(final double velocity) {
        final NetworkTablesHelper spinnerHelper = new NetworkTablesHelper("loader/spinnermotor/");
        spinnerHelper.set("targetRPM", velocity);
        spinnerVelocity = velocity;
        log.sub("%s: Setting loader motor velocity to: %f", name, velocity);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        if (velocity == 0) {
            spinner.set(ControlMode.PercentOutput, 0);
        } else {
            spinner.set(ControlMode.Velocity, velocity);
        }
    }

    @Override
    public void setTargetPassthroughMotorOutput(final double percent) {
        log.sub("%s: Setting loader in motor percent output to: %f", name, percent);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        passthrough.set(ControlMode.PercentOutput, percent);

    }
    
    @Override
    public void setInitBallCount(int initBallCount) {
        this.initBallCount = initBallCount;
    }

    public LoaderInterface setPaddleExtended(final boolean extend) {
        if (extend) {
            paddleSolenoid.extend();
        } else {
            paddleSolenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isPaddleExtended() {
        // log.sub("Is intake extended: " + solenoid.isExtended());
        return paddleSolenoid.isExtended();
    }

    @Override
    public boolean isPaddleRetracted() {
        return paddleSolenoid.isRetracted();
    }

    @Override
    public void execute(final long timeInMillis) {
        final NetworkTablesHelper spinnerHelper = new NetworkTablesHelper("loader/spinnermotor/");
        final double p = spinnerHelper.get("p", 0);
        final double i = spinnerHelper.get("i", 0);
        final double d = spinnerHelper.get("d", 0);
        final double f = spinnerHelper.get("f", 0);
        spinner.setPIDF(0, p, i, d, f);
        spinnerHelper.set("actualRPM", spinner.getVelocity());
        inSensorCount.execute(0);
        outSensorCount.execute(0);
        
        // Don't update all the time because the colour wheel may want to use the LEDs
        if (spinner.getSpeed() > 1) { 
            led.setProgressColour(LEDColour.PURPLE, LEDColour.WHITE, getCurrentCount() / 5 * 100);
        }
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader Paddle position",
                isPaddleExtended() ? "extended" : isPaddleRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Loader spinner velocity", spinner.getVelocity());
        dashboard.putNumber("Loader passthrough percent output", passthrough.getOutputPercent());
        inSensorCount.updateDashboard();
        outSensorCount.updateDashboard();
    }

    @Override
    public void disable() {
        spinner.set(ControlMode.PercentOutput, 0);
        passthrough.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public int getCurrentCount() {
        return inSensorCount.getCount() - outSensorCount.getCount() + initBallCount;
    }

    @Override
    public double getTargetSpinnerMotorVelocity() {
        return spinnerVelocity;
    }

    @Override
    public double getTargetPassthroughMotorOutput() {
        return passthrough.getOutputPercent();
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
            if (sensorReading == lastSensorReading)
                return;
            count++;
            lastSensorReading = sensorReading;
        }

        @Override
        public void updateDashboard() {
            dashboard.putNumber(name + " ball count", getCount());
        }
    }
}