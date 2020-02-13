package frc.robot.lib;

public class OperatorBoxButtons {
	/*
	 * Buttons 0 - 22 appear to be valid buttons, with special cases:
	 *  5  -  8 is a rotary selector
	 *  13 - 17 is a rotary selector
	 *  19 - 22 is a rotary selector
	 *  Leaving 23 - 12 = 11 buttons.
	 *  
	 *  The disable/manual/auto button switches are as follows:
	 *  23/24, 25/26, 27/28, 29/30, 31/32
	 *  
	 *  Unassigned buttons: 12,
	 *  
	 *  Note that this box also contains four pots for the axis.
	 */
	
	public static final int BUTTON0 = 0;  // Should this exist????
	public static final int WHITE_BUTTON1 = 1;
	public static final int WHITE_BUTTON2 = 2;

	public static final int RED_BUTTON1 = 3;
	public static final int RED_BUTTON2 = 4;
	public static final int RED_BUTTON3 = 5;
	public static final int RED_BUTTON4 = 6;
	public static final int RED_BUTTON5 = 7;

	public static final int YELLOW_BUTTON1 = 8;
	public static final int YELLOW_BUTTON2 = 9; 
	public static final int YELLOW_BUTTON3 = 10;
	public static final int YELLOW_BUTTON4 = 11;
	public static final int YELLOW_BUTTON5 = 12;

	public static final int GREEN_BUTTON1 = 13;
	public static final int GREEN_BUTTON2 = 14;
	public static final int GREEN_BUTTON3 = 15;
	public static final int GREEN_BUTTON4 = 16;
	public static final int GREEN_BUTTON5 = 17;

	public static final int BLUE_BUTTON1 = 18;
	public static final int BLUE_BUTTON2 = 19;
	public static final int BLUE_BUTTON3 = 20;
	public static final int BLUE_BUTTON4 = 21;
	public static final int CLEAR_BUTTON1 = 22;
	
	// Rotary buttons. Only one of these can be pressed at any one time.
	// Not clear how many positions there are, but there may be an
	// unbuttoned one that is implicit if none of the others is pressed.
	// Maybe these should have been done in binary?

	

	
	// Pot as joystick axis.
	public static final int WHITE_POT = 0;
	public static final int RED_POT = 1;
	public static final int YELLOW_POT = 2;
	public static final int GREEN_POT = 3;
	public static final int BLUE_POT = 4;

	// 3-way switches. If neither manual or auto is pressed, then
	// it is assumed to be in auto mode.
	public static final int WHITE_MANUAL = 23;
	public static final int WHITE_DISABLE = 24;
	public static final int RED_MANUAL = 25;
	public static final int RED_DISABLE = 26;
	public static final int YELLOW_MANUAL = 27;
	public static final int YELLOW_DISABLE = 28;
	public static final int GREEN_MANUAL = 29;
	public static final int GREEN_DISABLE = 30;
	public static final int BLUE_MANUAL = 31;
	public static final int BLUE_DISABLE = 32;

	// This years game-specific mappings
	// Spitter
	public static final int SPITTER_DISABLE = WHITE_DISABLE;
	public static final int SPITTER_MANUAL = WHITE_MANUAL;
	public static final int SPITTER_POT = WHITE_POT;
	public static final int SPITTER_SPEED = WHITE_BUTTON1;

	// Climber
	public static final int CLIMBER_DISABLE = RED_DISABLE;
	public static final int CLIMBER_MANUAL = RED_MANUAL;
	public static final int CLIMBER_POT = RED_POT;
	public static final int CLIMBER_FRONT_HEIGHT = RED_BUTTON1;
	public static final int CLIMBER_REAR_HEIGHT = RED_BUTTON2;
	public static final int CLIMBER_BOTH_HEIGHT = RED_BUTTON3;
	public static final int CLIMBER_DRIVE_SPEED = RED_BUTTON4;

	// Intake (shares with passthru)
	public static final int INTAKE_DISABLE = YELLOW_DISABLE;
	public static final int INTAKE_MANUAL = YELLOW_MANUAL;
	public static final int INTAKE_POT = YELLOW_POT;

	// Passthru (shares with intake)
	public static final int PASSTHRU_DISABLE = YELLOW_DISABLE;
	public static final int PASSTHRU_MANUAL = YELLOW_MANUAL;
	public static final int INTAKE_MOTOR = YELLOW_BUTTON1;
	public static final int INTAKE_EXTEND = YELLOW_BUTTON3;
	public static final int INTAKE_RETRACT = YELLOW_BUTTON4;
	// Loader
	public static final int LOADER_PADDLE_RETRACT = 0; //Undecided/Unassigned Buttons
	public static final int LOADER_PADDLE_EXTEND = 0;
	public static final int LOADER_RETRACT = 0;
	public static final int LOADER_EXTEND = 0;
	public static final int LOADER_IN_MOTOR = 0;
	public static final int LOADER_IN_POT = 0; // Potentiometer needs to be assigned
	public static final int LOADER_MAIN_MOTOR = YELLOW_BUTTON2;
	public static final int LOADER_MAIN_POT = YELLOW_POT;

	// Hatch
	public static final int HATCH_DISABLE = GREEN_DISABLE;
	public static final int HATCH_MANUAL = GREEN_MANUAL;
	public static final int HATCH_POT = GREEN_POT;
	public static final int HATCH_MOVE_LEFT = GREEN_BUTTON1;
	public static final int HATCH_MOVE_RIGHT = GREEN_BUTTON2;
	public static final int HATCH_HOLD = GREEN_BUTTON3;
	public static final int HATCH_RELEASE = GREEN_BUTTON4;

	// Lift (shared with spark test, ensure that one or the other is disabled).
	public static final int LIFT_DISABLE = BLUE_DISABLE;
	public static final int LIFT_MANUAL = BLUE_MANUAL;
	public static final int LIFT_POT = BLUE_POT;
	public static final int LIFT_SET_HEIGHT = BLUE_BUTTON1;
	public static final int LIFT_DEPLOY_CARRIAGE = BLUE_BUTTON2;
	public static final int LIFT_RETRACT_CARRIAGE = BLUE_BUTTON3;  // Button 3 is broken?

	// Spark test (shared with lift, ensure that one or the other is disabled).
	public static final int SPARK_DISABLE = BLUE_DISABLE;
	public static final int SPARK_MANUAL = BLUE_MANUAL;
	public static final int SPARK_POT = BLUE_POT;
	public static final int SPARK_SET_SPEED = BLUE_BUTTON1;
}
