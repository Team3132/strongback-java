/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.mock;
import frc.robot.interfaces.NetworkTableHelper;


/**
 * Add your docs here.
 */
public class MockNetworkTableHelperImpl implements NetworkTableHelper {

    
    public MockNetworkTableHelperImpl(String defaultName) {
        
    }

    @Override
    public double get(String key, double defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean get(String key, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public String get(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public void set(String key, double value) {
    }


}
