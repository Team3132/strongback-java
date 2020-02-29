package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Solenoid;

import frc.robot.interfaces.BuddyClimbInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

public class BuddyClimb extends Subsystem implements BuddyClimbInterface, Executable, DashboardUpdater {
   private Solenoid solenoid;

   public BuddyClimb(Solenoid solenoid, DashboardInterface dashboard, Log log) {
       super("BuddyClimb", dashboard, log);  
       this.solenoid = solenoid;
       // Motors, sensors here etc….

       log.register(true, () -> isExtended(), "%s/extended", name)
          .register(true, () -> isRetracted(), "%s/retracted", name);
       // More logging here...
   }

   @Override
   public BuddyClimbInterface setExtended(boolean extend) {
       if (extend) {
           solenoid.extend();
       } else {
           solenoid.retract();
       }
       return this; // Allows chaining of calls.
   }

   @Override
   public boolean isExtended() {
       return solenoid.isExtended();
   }

   @Override
   public boolean isRetracted() {
       return solenoid.isRetracted();
   }
  
   // Other methods go here.
  
   /**
    * Update the operator console with the status of the intake subsystem.
    */
   @Override
   public void updateDashboard() {
       dashboard.putString("Intake position",
           isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
       // Motors, sensors here etc….
   }
}
