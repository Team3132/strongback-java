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
    private Motor spinner, passthrough;
    private Solenoid paddleSolenoid;
    private double spinnerVelocity = 0;
    private double passthroughVelocity = 0;

    public Loader(Motor loaderSpinnerMotor, Motor loaderPassthroughMotor, Solenoid paddleSolenoid, DashboardInterface dashboard, Log log) {
        super("Loader", dashboard, log);
        
        this.spinner = loaderSpinnerMotor;
        this.passthrough = loaderPassthroughMotor;
        this.paddleSolenoid = paddleSolenoid;
        log.register(true, () -> passthrough.getOutputCurrent(), "%s/passthrough/Current", name)
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
    public double getTargetSpinnerMotorOutput() {
        return spinner.getOutputPercent();
    }

    @Override
    public double getTargetPassthroughMotorOutput() {
        return passthrough.getOutputPercent();
    }
}