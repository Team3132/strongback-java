package frc.robot.subsystems;

import org.strongback.components.Solenoid;

import frc.robot.interfaces.BuddyClimbInterface;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.lib.Subsystem;
import frc.robot.lib.chart.Chart;

public class BuddyClimb extends Subsystem implements BuddyClimbInterface {
   private Solenoid solenoid;

   public BuddyClimb(Solenoid solenoid, DashboardInterface dashboard) {
       super("BuddyClimb", dashboard);  
       this.solenoid = solenoid;

       Chart.register(() -> isExtended(), "%s/extended", name);
       Chart.register(() -> isRetracted(), "%s/retracted", name);
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
    * Update the operator console with the status of the buddy climb subsystem.
    */
   @Override
   public void updateDashboard() {
       dashboard.putString("Buddy climb position",
           isExtended() ? "extended" : isRetracted() ? "retracted" : "moving");
   }
}
