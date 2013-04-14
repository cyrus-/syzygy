package edu.cmu.cs.syzygy.planet;

public class Planets {
	public static enum Planet {
	      MERCURY,
	      VENUS,
	      EARTH,
	      MARS,
	      JUPITER,
	      SATURN,
	      URANUS,
	      NEPTUNE
	   }
	
	public static Planets.Planet somePlanet() {
	      return Planets.Planet.VENUS;
	}
	
	public int distanceto(Planet planet) {
		switch(planet) {
		case EARTH: return 0;
		case MARS: return 7;
		default: return 7;
		}
	}
	
	public static String mystring() {
	      return "This is a string";
	   }
	
	public double distancefrom(Planet planet) {
		double f = 20.5;
		Planets.Planet mars = Planet.MARS;
	      Planets.Planet mars2 = Planet.MARS;
	      Planets.Planet earth = Planet.EARTH;
		switch(planet) {
		case EARTH: return 20.5;
		case MARS: return 14.5;
		default: return 3.14;
		}
	}
}
