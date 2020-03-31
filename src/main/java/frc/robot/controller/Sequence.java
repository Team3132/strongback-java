package frc.robot.controller;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list of State instances that the robot should go through and an end state
 * for when the sequence is interrupted.
 * The final state is where the robot will remain once it has done all states.
 * Can be aborted at any time.
 */
public class Sequence implements Iterable<State> {
	private final String name;
	private ArrayList<State> states;
	private State endState;
	
	private Sequence(SequenceBuilder builder) {
		this.name = builder.name;
		this.endState = builder.endState;
		this.states = builder.states;
	}

	public String getName() {
		return name;
	}
	
	public Iterator<State> iterator() {
		return states.iterator();
	}

	public State getEndState() {
		return endState;
	}

	/**
	 * A list of State instances that the robot should go through.
	 * Can be aborted at any time.
	 * 
	 * Every add() call adds a new state that the robot should achieve
	 * before moving to the next state. The final state is where the robot
	 * will remain once it has done all states.
	 * 
	 * Any value not set will cause it to not be changed. eg if we don't set the
	 * lift height, the height will be unchanged.
	 * 
	 * An end state can be created to be applied if the sequence is interrupted,
	 * either autocreated by passing createInterrupt = true
	 * or manually set by calling onInterrupt();
	 * 
	 * Example usage for intaking a cube
	 *   SequenceBuilder builder = new SequenceBuilder("Intake cube", createInterrupt);
	 *   builder.add().setLiftHeight(0).setIntakeConfig(NARROW).setOuttakeOpen(true);
	 *   builder.add().setIntakeMotorOutput(1).setOuttakeHasCube(true);
	 *   builder.add().setOuttakeOpen(false);
	 *   builder.add().setIntakeMotorOutput(0).setIntakeConfig(STOWED);
	 *   return builder.build();
	 */
	public static class SequenceBuilder { 
		private final String name;
		private boolean createInterrupt;
		private ArrayList<State> states = new ArrayList<State>();
		private State endState = new State();
	
		public SequenceBuilder(String name, boolean createInterrupt) {
			this.name = name;
			this.createInterrupt = createInterrupt;
		}

		public SequenceBuilder(String name) {
			this(name, false);
		}
	
		/**
		 * Adds a new state at the end of the sequence.
		 * @return new state
		 */
		public State then() {
			states.add(new State());
			return states.get(states.size() - 1);
		}
	
		/**
		 * Add another sequence to the end of this one, only copying the states.
		 * @param other the sequence to add.
		 */
		public void appendSequence(Sequence other) {
			states.addAll(other.states);
		}
		
		/**
		 * Applies a state when the sequence gets interrupted
		 */
		public State onInterrupt() { 
			return endState;
		}
	
		public Sequence build(){
			if (createInterrupt) { 
				for (State s: states) {
					endState.fillInterrupt(s);
				}	
			}
			return new Sequence(this);
		}
	
	
	}
}
