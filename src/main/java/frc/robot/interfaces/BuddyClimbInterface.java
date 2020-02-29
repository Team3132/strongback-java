package frc.robot.interfaces;
import org.strongback.Executable;

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

















