/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.mock;
import frc.robot.interfaces.NetworkTableHelperInterface;


/**
 * Add your docs here.
 */
public class MockNetworkTableHelper implements NetworkTableHelperInterface {

    
    public MockNetworkTableHelper(String defaultName) {
        
    }

    @Override
    public double set(String key, double defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean set(String key, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public String set(String key, String defaultValue) {
        return defaultValue;
    }


}
