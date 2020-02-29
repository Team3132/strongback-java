package frc.robot.interfaces;


public interface NetworkTableHelperInterface extends DashboardUpdater {
    /**
     * Gets a named double value from the network tables. If not found
     * the defaultValue is returned.
     * @param key the name to look up in the table
     * @param defaultValue will be returned if key not found.
     * @return the value in the table if found, defaultValue otherwise.
     */
    public double get(String key, double defaultValue);
    public boolean get(String key, boolean defaultValue);
    public String get(String key, String defaultValue);

    /**
     * Sets a named double value from the network tables.
     * @param key the name to look up in the table.
     * @param value the value set to the network table.
     */
    public void set(String key, double value);
}
