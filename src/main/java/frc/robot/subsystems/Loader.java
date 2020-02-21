package frc.robot.subsystems;

import java.util.function.BooleanSupplier;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.NetworkTablesHelper;
import frc.robot.lib.Subsystem;

public class Loader extends Subsystem implements LoaderInterface, Executable, DashboardUpdater {
    private Motor spinner, passthrough;
    private Solenoid paddleSolenoid;
    private BooleanSupplier inSensor, outSensor;
    private double spinnerVelocity = 0;
    private int ballsStored = 0;
    private boolean inSensorPrev = false;
    private boolean outSensorPrev = false;

    public Loader(Motor loaderSpinnerMotor,
                Motor loaderPassthroughMotor, Solenoid paddleSolenoid,
                BooleanSupplier inSensor,
                BooleanSupplier outSensor,
                DashboardInterface dashboard, Log log) {
        super("Loader", dashboard, log);

        
        
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.paddleSolenoid = paddleSolenoid;
        this.inSensor = inSensor;
        this.outSensor = outSensor;
        
        log.register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
               .register(true, () -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name)
               .register(true, () -> spinner.getVelocity(), "%s/spinner/Velocity", name)
               .register(true, () -> spinner.getOutputCurrent(), "%s/spinner/Current", name)
               .register(true, () -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name)
               .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name)
               .register(true, () -> (double) getBallsStored(), "%s/ballsStored", name);
    }

    @Override
    public void update() {
        // Increments if the current state is not equal to the previous state (rising edge)
        if(inSensor.getAsBoolean() && !inSensorPrev) {
            ballsStored += 1;
        }
        if(outSensor.getAsBoolean() && !outSensorPrev) {
            ballsStored -= 1;
        }

        // Updates the state of inSensorPrev if it's not equal to inSensor
        if(inSensor.getAsBoolean() != inSensorPrev) {
            inSensorPrev = inSensor.getAsBoolean();
        }
        // Updates the state of outSensorPrev if it's not equal to outSensor
        if(outSensor.getAsBoolean() != outSensorPrev) {
            outSensorPrev = outSensor.getAsBoolean();
        }
    }

    public int getBallsStored() {
        return ballsStored;
    }

    public boolean isLoaderFull() {
        if(ballsStored >= 5) {
            return true;
        } else {
            return false;
        }
    }
    public boolean isLoaderEmpty() {
        if(ballsStored == 0) {
            return true;
        } else {
            return false;
        }
    }

	@Override
	public void setTargetSpinnerMotorVelocity(double velocity) {
        NetworkTablesHelper spinnerHelper = new NetworkTablesHelper("loader/spinnermotor/");
        spinnerHelper.set("targetRPM", velocity);
        spinnerVelocity = velocity;
        log.sub("%s: Setting loader motor velocity to: %f", name, velocity);
        // If motor is zero in velocity the PID will try and reverse the motor in order to slow down
        if(velocity == 0) {
            spinner.set(ControlMode.PercentOutput, 0);
        } else {
            spinner.set(ControlMode.Velocity, velocity);
        }
    }
    @Override
	public void setTargetPassthroughMotorOutput(double percent) {
        log.sub("%s: Setting loader in motor percent output to: %f", name, percent);
        // If motor is zero in velocity the PID will try and reverse the motor in order to slow down
            passthrough.set(ControlMode.PercentOutput, percent);
        
    }

    public LoaderInterface setPaddleExtended(boolean extend) {
        if (extend) {
            paddleSolenoid.extend();
        } else {
            paddleSolenoid.retract();
        }
        return this;
    }

    @Override
    public boolean isPaddleExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return paddleSolenoid.isExtended();
    }

    @Override
    public boolean isPaddleRetracted() {
        return paddleSolenoid.isRetracted();
    }

    @Override
    public void execute(long timeInMillis) {
        NetworkTablesHelper spinnerHelper = new NetworkTablesHelper("loader/spinnermotor/");
        double p = spinnerHelper.get("p", 0);
        double i = spinnerHelper.get("i", 0);
        double d = spinnerHelper.get("d", 0);
        double f = spinnerHelper.get("f", 0);
        spinner.setPIDF(0, p, i, d, f);
        spinnerHelper.set("actualRPM", spinner.getVelocity());
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader Paddle position", isPaddleExtended() ? "extended" : isPaddleRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Loader spinner velocity", spinner.getVelocity());
        dashboard.putNumber("Loader passthrough percent output", passthrough.getOutputPercent());
    }

    @Override
    public void disable()  {
        spinner.set(ControlMode.PercentOutput, 0);
        passthrough.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public double getTargetSpinnerMotorVelocity() {
        return spinnerVelocity;
    }

    @Override
    public double getTargetPassthroughMotorOutput() {
        return passthrough.getOutputPercent();
    }
}