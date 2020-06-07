package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.Loader;
import frc.robot.interfaces.Dashboard;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.LEDStrip;
import frc.robot.lib.LEDColour;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;
import frc.robot.lib.log.Log;

public class LoaderImpl extends Subsystem implements Loader {
    final private Motor spinner, passthrough;
    final private Solenoid paddleSolenoid;
    private double spinnerRPS = 0;
    final Counter inSensorCount;
    final Counter outSensorCount;
    private int initBallCount = 0;
    final private LEDStrip led;
    private double targetPassthroughDutyCycle = 0;

    public LoaderImpl(final Motor loaderSpinnerMotor, final Motor loaderPassthroughMotor, final Solenoid paddleSolenoid,
            final BooleanSupplier inSensor, final BooleanSupplier outSensor, LEDStrip led, final Dashboard dashboard) {
        super("Loader", dashboard);
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.paddleSolenoid = paddleSolenoid;
        this.led = led;
        inSensorCount = new Counter("loader:inSensor", inSensor);
        outSensorCount = new Counter("loader:outSensor", outSensor);

        Chart.register(() -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name);
        Chart.register(() -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name);
        Chart.register(() -> getSpinnerMotorRPS(), "%s/spinner/rps", name);
        Chart.register(() -> getTargetSpinnerRPS(), "%s/spinner/targetRPS", name);
        Chart.register(() -> spinner.getOutputCurrent(), "%s/spinner/Current", name);
        Chart.register(() -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name);
        Chart.register(() -> (double) getCurrentBallCount(), "%s/spinner/CurrentBallCount", name);
        Chart.register(() -> (double) inSensorCount.count, "%s/spinner/totalBallsIn", name);
        Chart.register(() -> (double) outSensorCount.count, "%s/spinner/totalBallsOut", name);
        Chart.register(() -> (double) initBallCount, "%s/spinner/initialBallCount", name);
        Chart.register(() -> isPaddleBlocking(), "%s/paddleRetracted", name);
        Chart.register(() -> inSensor.getAsBoolean(), "%s/spinner/inSensorState", name);
        Chart.register(() -> outSensor.getAsBoolean(), "%s/spinner/outSensorState", name);
    }

    @Override
    public void setTargetSpinnerRPS(final double rps) {

        spinnerRPS = rps;
        Log.debug("%s: Setting loader motor rps to: %f", name, rps);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        if (rps == 0) {
            spinner.set(ControlMode.DutyCycle, 0);
        } else {
            spinner.set(ControlMode.Speed, rps);
        }
    }

    @Override
    public double getTargetSpinnerRPS() {
        return spinnerRPS;
    }

    public double getSpinnerMotorRPS() {
        return spinner.getSpeed();
    }

    @Override
    public void setTargetPassthroughDutyCycle(double percent) {
        Log.debug("%s: Setting loader in motor percent output to: %f", name, percent);
        // If motor is zero in velocity the PID will try and reverse the motor in order
        // to slow down
        targetPassthroughDutyCycle = percent;
        passthrough.set(ControlMode.DutyCycle, percent);
    }

    @Override
    public double getTargetPassthroughDutyCycle() {
        return targetPassthroughDutyCycle;
    }
    
    @Override
    public void setInitBallCount(int initBallCount) {
        this.initBallCount = initBallCount;
    }

    public Loader setPaddleBlocking(final boolean block) {
        if (block) {
            paddleSolenoid.extend();
        } else {
            paddleSolenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isPaddleNotBlocking() {
        // Logger.debug("Is intake extended: " + solenoid.isExtended());
        return paddleSolenoid.isRetracted();
    }

    @Override
    public boolean isPaddleBlocking() {
        return paddleSolenoid.isExtended();
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
        dashboard.putNumber("Loader spinner rps", getSpinnerMotorRPS());
        dashboard.putNumber("Loader spinner target rps", getTargetSpinnerRPS());
        dashboard.putNumber("Loader passthrough percent output", passthrough.getOutputPercent());
        dashboard.putNumber("Current number of balls", getCurrentBallCount());
        inSensorCount.updateDashboard();
        outSensorCount.updateDashboard();
    }

    @Override
    public void enable() {
        spinner.set(ControlMode.DutyCycle, 0);
        passthrough.set(ControlMode.DutyCycle, 0);
    }

    @Override
    public void disable() {
        spinner.set(ControlMode.DutyCycle, 0);
        passthrough.set(ControlMode.DutyCycle, 0);
    }

    @Override
    public int getCurrentBallCount() {
        return inSensorCount.getCount() - outSensorCount.getCount() + initBallCount;
    }

    private class Counter implements DashboardUpdater, Executable {
        final private String name;
        final private BooleanSupplier sensor;
        private int count = 0;
        private boolean lastSensorReading = false;

        public Counter(final String name, final BooleanSupplier sensor) {
            this.name = name;
            this.sensor = sensor;
            Chart.register(() -> (double) getCount(), "%s/count", name);
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
            dashboard.putBoolean(name + " sensor state", sensor.getAsBoolean());
            }
    }
}
