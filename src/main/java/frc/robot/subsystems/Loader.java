package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

public class Loader extends Subsystem implements LoaderInterface, Executable, DashboardUpdater {
    private Motor spinner, passthrough, feeder;
    private Solenoid loaderS, paddleS;
    

    public Loader(int teamNumber, Motor loaderSpinnerMotor, Motor loaderPassthroughMotor, Motor loaderFeederMotor, Solenoid loaderSolenoid, Solenoid paddleSolenoid, DashboardInterface dashboard, Log log) {
        super("Passthrough", dashboard, log);
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.feeder = loaderFeederMotor;
        this.loaderS = loaderSolenoid;
        this.paddleS = paddleSolenoid;
        log.register(true, () -> feeder.getOutputCurrent(), "%s/feeder/Current", name)
               .register(true, () -> feeder.getOutputPercent(), "%s/feeder/PercentOut", name)
            //    .register(true, () -> feeder.getVelocity(), "%s/motorIn/Velocity", name)
               .register(true, () -> passthrough.getVelocity(), "%s/passthrough/Velocity", name)
               .register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
               .register(true, () -> passthrough.getOutputPercent(), "%s/passthrough/PercentOut", name)
               .register(true, () -> spinner.getVelocity(), "%s/spinner/Velocity", name)
               .register(true, () -> spinner.getOutputCurrent(), "%s/spinner/Current", name)
               .register(true, () -> spinner.getOutputPercent(), "%s/spinner/PercentOut", name)
               .register(true, () -> isLoaderRetracted(), "%s/loaderRetracted", name)
               .register(true, () -> isPaddleRetracted(), "%s/paddleRetracted", name);
    }

	@Override
	public double getTargetSpinnerMotorOutput() {
		return spinner.getOutputPercent();
	}

	@Override
	public void setTargetSpinnerMotorVelocity(double velocity) {
        log.sub("%s: Setting loader motor velocity to: %f", velocity);
        spinner.set(ControlMode.Velocity, velocity);
    }
    @Override
	public void setTargetPassthroughMotorVelocity(double velocity) {
        log.sub("%s: Setting loader in motor velocity to: %f", velocity);
        passthrough.set(ControlMode.Velocity, velocity);
    }
    @Override
	public void setTargetFeederMotorOutput(double outCurrent) {
        log.sub("%s: Setting loader out motor output to: %f", outCurrent);
        feeder.set(ControlMode.PercentOutput, outCurrent);
    }

    public LoaderInterface setLoaderExtended(boolean extend) {
        if (extend) {
            loaderS.extend();
        } else {
            loaderS.retract();
        }
        return this;
    }

    public LoaderInterface setPaddleExtended(boolean extend) {
        if (extend) {
            paddleS.extend();
        } else {
            paddleS.retract();
        }
        return this;
    }


    @Override
    public boolean isLoaderExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return loaderS.isExtended();
    }

    @Override
    public boolean isLoaderRetracted() {
        return loaderS.isRetracted();
    }

    @Override
    public boolean isPaddleExtended() {
        //log.sub("Is intake extended: " +  solenoid.isExtended());
        return paddleS.isExtended();
    }

    @Override
    public boolean isPaddleRetracted() {
        return paddleS.isRetracted();
    }
    /**
     * Update the operator console with the status of the hatch subsystem.
     */
    @Override
    public void updateDashboard() {
        dashboard.putString("Loader position", isLoaderExtended() ? "extended" : isLoaderRetracted() ? "retracted" : "moving");
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
}