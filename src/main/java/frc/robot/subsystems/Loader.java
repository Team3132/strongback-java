package frc.robot.subsystems;

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
    private Motor spinner, passthrough, feeder;
    private Solenoid paddleSolenoid;
    private double spinnerVelocity = 0;
    private double passthroughVelocity = 0;

    public Loader(Motor loaderSpinnerMotor, Motor loaderPassthroughMotor, Motor loaderFeederMotor, Solenoid paddleSolenoid, DashboardInterface dashboard, Log log) {
        super("Loader", dashboard, log);
        
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.feeder = loaderFeederMotor;
        this.paddleSolenoid = paddleSolenoid;
        log.register(true, () -> feeder.getOutputCurrent(), "%s/feeder/Current", name)
               .register(true, () -> feeder.getOutputPercent(), "%s/feeder/PercentOut", name)
               .register(true, () -> passthrough.getVelocity(), "%s/passthrough/Velocity", name)
               .register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
               .register(true, () -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name)
               .register(true, () -> spinner.getVelocity(), "%s/spinner/Velocity", name)
               .register(true, () -> spinner.getOutputCurrent(), "%s/spinner/Current", name)
               .register(true, () -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name)
               .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name);
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
	public void setTargetPassthroughMotorVelocity(double velocity) {
        NetworkTablesHelper passthroughHelper = new NetworkTablesHelper("loader/passthroughmotor/");
        passthroughHelper.set("targetRPM", velocity);
        passthroughVelocity = velocity;
        log.sub("%s: Setting loader in motor velocity to: %f", name, velocity);
        // If motor is zero in velocity the PID will try and reverse the motor in order to slow down
        if(velocity == 0) {
            passthrough.set(ControlMode.PercentOutput, 0);
        } else {
            passthrough.set(ControlMode.Velocity, velocity);
        }
    }
    @Override
	public void setTargetFeederMotorOutput(double outPercent) {
        log.sub("%s: Setting loader feeder motor percentage output to: %f", name, outPercent);
        feeder.set(ControlMode.PercentOutput, outPercent);
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
        double p = spinnerHelper.set("p", 0);
        double i = spinnerHelper.set("i", 0);
        double d = spinnerHelper.set("d", 0);
        double f = spinnerHelper.set("f", 0);
        spinner.setPIDF(0, p, i, d, f);
        spinnerHelper.set("actualRPM", spinner.getVelocity());

        NetworkTablesHelper passthroughHelper = new NetworkTablesHelper("loader/loaderinmotor/");
        p = passthroughHelper.set("p", 0);
        i = passthroughHelper.set("i", 0);
        d = passthroughHelper.set("d", 0);
        f = passthroughHelper.set("f", 0);
        passthrough.setPIDF(0, p, i, d, f);
        passthroughHelper.set("actualRPM", passthrough.getVelocity());
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader Paddle position", isPaddleExtended() ? "extended" : isPaddleRetracted() ? "retracted" : "moving");
        dashboard.putNumber("Loader spinner velocity", spinner.getVelocity());
        dashboard.putNumber("Loader passthrough velocity", passthrough.getVelocity());
        dashboard.putNumber("Loader feeder current", feeder.getOutputCurrent());
    }

    @Override
    public void disable()  {
        spinner.set(ControlMode.Velocity, 0);
        passthrough.set(ControlMode.Velocity, 0);
        feeder.set(ControlMode.PercentOutput, 0);
    }

    @Override
    public double getTargetFeederMotorOutput() {
        return feeder.getOutputPercent();
    }

    @Override
    public double getTargetSpinnerMotorVelocity() {
        return spinnerVelocity;
    }

    @Override
    public double getTargetPassthroughMotorVelocity() {
        return passthroughVelocity;
    }
}