/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.interfaces;

public interface NetworkTableHelperInterface extends DashboardUpdater {
    public double set(String key, double defaultValue);
    public boolean set(String key, boolean defaultValue);
    public String set(String key, String defaultValue);
}
