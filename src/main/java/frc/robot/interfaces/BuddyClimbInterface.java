package frc.robot.interfaces;
import org.strongback.Executable;

/* The buddy climb is a bar that another robot can latch onto in an effort to bring them up
    with us when we climb. It has a single solenoid that is extended when the subsystem is
    deployed.*/
public interface BuddyClimbInterface extends SubsystemInterface, Executable, DashboardUpdater {
    /**
     * @extended extend or retract the intake solenoid.
     */
    public BuddyClimbInterface setExtended(boolean extended);

    /**
        * Gets the state of the solenoid. While moving, both will return false.
     * @return the state of the intake solenoid.
     */
    public boolean isExtended();
    public boolean isRetracted();

       // Other intake methods here….
}