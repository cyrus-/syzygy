package edu.cmu.cs.syzygy;

public class Debug {

	
	public static enum Mode {
		ENUMLITERALS,
		METHODS,
		VARIABLES,
		BASICBUGS, NUMBERLITERAL, INFO, EXCEPTIONS
	}
	
	private static Mode[] allowed_modes = {};
	
	public static void print(Mode m, String message) {
		for (Mode allowed : allowed_modes) {
			if (m.equals(allowed)) {
				System.out.println(message);
			}
		}
	}
}
