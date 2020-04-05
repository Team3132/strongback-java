package frc.robot.lib;

import frc.robot.Constants;

/*
 * The position class defines a position on the field.
 * The rules for a position are:
 * X is positive in the forward direction, Y is positive to the left hand side.
 * Angles are held in degrees positive in an anticlockwise direction.
 * See Location.java for more details.
 * 
 * Notes:
 *  - Distances are in inches (to match the rules).
 *  - Angles are always in degrees (to make them easy to reason about).
 */
public class Position {
	public double x;		// +ve is forwards in inches.
	public double y;		// +ve is to the left of the robot in inches.
	public double heading;  // Angle in degrees from initial direction (can be positive or negative and multiple turns)
	                        // +ve angle is anticlockwise.
    public double speed;	// current speed (metres/second)
    public double timeSec;	// time of this location (ms after start).
            
    public Position(double x, double y) {
    	this(x, y, 0, 0, 0);
	}
    
    public Position(double x, double y, double headingDegrees) {
    	this(x, y, headingDegrees, 0, 0);
	}
    
    public Position(double x, double y, double headingDegrees, double speed) {
    	this(x, y, headingDegrees, speed, 0);
	}   
    
    public Position(double x, double y, double headingDegrees, double speed, double time) {
    	this.x = x;
    	this.y = y;
    	this.heading = headingDegrees;
    	this.speed = speed;
    	this.timeSec = time;
	}

	public Position(Position position) {
    	this.x = position.x;
    	this.y = position.y;
    	this.heading = position.heading;
    	this.speed = position.speed;
    	this.timeSec = position.timeSec;
	}

	/**
	 * Take two field oriented positions and returns a position relative to the second position.
	 * Normally used to find the position of a vision target relative to the robot itself.
	 */
    public Position getRelativeToPosition(Position position) {
        // Subtract off the robots position from the position.
        double newX = x - position.x;
		double newY = y - position.y;
		// Angle to the target.
		double angle = MathUtil.radiansToDegrees(Math.atan2(newY, newX));
        // Rotate to match the robots orientation.
        double result[] = MathUtil.rotateVector(newX, newY, -position.heading);
        // Subtract off the robots angle from the original angle so the angle is also relative.
        Position loc = new Position(result[0], result[1], angle - position.heading, 0, 0);
        return loc;
    }
    
    // Add another positon on to the current position.
    public Position add(Position other) {
        // Rotate to match the current heading.
        double result[] = MathUtil.rotateVector(other.x, other.y, heading);
    	return new Position(x + result[0], y + result[1], heading - other.heading);
    }
    
    @Override
    public String toString() {
        return String.format("X(%.3f),Y(%.3f),H(%.3f),S(%3f)", x, y, heading, speed);
    }
    
    public String toCompactString() {
        return String.format("P(%.1f,%.1f@%.1f\u00B0@%.1f\"/s)", x, y, heading, speed);
    }
    
    public void copyFrom(Position other) {
        x = other.x;
        y = other.y;
        heading = other.heading;
        speed = other.speed;
        timeSec = other.timeSec;
    }
	
	/**
	 * Add distance inches in the direction of angle, relative to heading.
	 * Heading doesn't change.
	 * 
	 * @param distance  how many inches to add
	 * @param angle  the angle to add on to heading when changing the x and y values.
	 * @return
	 */
    public final Position addVector(double distance, double angle) {
    	/*
    	 * Change a location by the vector supplied.
    	 * The final heading is unchanged.
    	 * 
    	 * Angle is in degrees.
    	 */
		return new Position(x + distance * MathUtil.cos(angle+heading),
						    y + distance * MathUtil.sin(angle+heading), heading);
    }
    
    /**
     * Return the bearing from this position to the destination position. The bearing is from -180 to 180 degrees.
     * If the destination has the same X and Y co-ordinates the bearing is defined as the destination heading normalised.
     * @param target
     * @return bearing (-180 to 180) from our current position to the destination position.
     */
	public double bearingTo(Position target) {
		double diffX = target.x - x;				// diffX and diffY are the distance to the destination from the current point.
		double diffY = target.y - y;

		if (diffX == 0.0 && diffY == 0.0) {
			// Special case. The heading to the same point stays as the current heading
			return MathUtil.normalise(target.heading, Constants.FULL_CIRCLE);
		}
		return MathUtil.normalise(MathUtil.radiansToDegrees(Math.atan2(diffY, diffX)) - heading, Constants.FULL_CIRCLE);
	}

	public double distanceTo(Position dest) {
		double diffX = dest.x - x;
		double diffY = dest.y - y;
		// See, learning about Pythagoras was useful :)
		return Math.sqrt((diffX * diffX) + (diffY * diffY));
	}
	
	/**
	 * Calculate the angle between two positions
	 * @param dest the destination angle as the heading value
	 * @return the normalised angle between the two headings
	 */
	public double angleBetweenBearings(Position dest) {
		return Math.abs(MathUtil.normalise(dest.heading - heading, Constants.FULL_CIRCLE));
	}
	
	public String getDygraphHeader(String name) {
		return String.format("%1$s/X,%1$s/Y,%1$s/Heading,%1$s/Speed,", name);
	}
	
	public String getDygraphData() {
		return String.format("%f,%f,%f,%f", x, y, heading, speed);
	}
	
	private boolean doubleEqual(double a, double b) {
		return Math.abs(a - b) < 0.1;
	}
	
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Position)) {
			return false;
		}
		Position p = (Position)o;		
		return doubleEqual(x, p.x) && doubleEqual(y, p.y) && doubleEqual(heading, p.heading) && doubleEqual(speed, p.speed);  
	}
}
