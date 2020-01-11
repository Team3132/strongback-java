package frc.robot.interfaces;

public interface TapeInterface {
    /**
     * These are the different configurations that the Tape Subsystem can 
     * recommend to the robot to move.
     */
    public enum Direction {
        STRAIGHT ("STRAIGHT", 0),
        LEFT ("LEFT", -1),
        RIGHT ("RIGHT", 1),
        NO_LINE_DETECTED ("NO LINE DETECTED", 0);
        
        public final String name;
        public final int direction;

        private Direction(String name, int direction) {
            this.name = name;
            this.direction = direction;
        }
    }

    /** 
     * From the values from the colour sensor the method gives a 
     * "recommendation" for the robot to move. The options of the code can be either
     * STRAIGHT, LEFT, RIGHT and NO_LINE_DETECTED.
     */
    public Direction getRecommendation();

    public boolean isCarpet(double sensorValue);
    public boolean isTape(double sensorValue);
    
    public boolean moveToLeft();
    public boolean moveToRight();
    public boolean moveStraight();
}