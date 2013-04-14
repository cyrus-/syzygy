package edu.cmu.cs.syzygy.planet;

public class Main {

   public static int number() {
      return 7; /// MISSING
   }

   public static String mystring() {
      return "This is a string";
   }

   public static double method(double flt, int n, Planets.Planet plt) {
      return (double)n * flt;
   }

   public static Planets.Planet somePlanet() {
      return Planets.Planet.VENUS;
   }

   public static void main(String[] args) {
      int twenty = 20;

      double casted = (double)2; // MISSING
      int c = 20 + 30;
      double f = 20.5;
      double f2 = 20.5;
      float flt = (float) 14.5;
      Planets.Planet mars = Planets.Planet.MARS;
      Planets.Planet mars2 = Planets.Planet.MARS;
      Planets.Planet earth = Planets.Planet.EARTH;
      Planets.Planet copy = earth;
      double f3 = method(f, c, Planets.Planet.JUPITER);

      method(3.14, 2, Planets.Planet.EARTH);
      method(1.0, 1, somePlanet());
      
      if(f3 == 1.0) {
    	  
      } else {
    	  for(int i = 0; i < 10; ++i) {
    	  
    	  }
      }
   }
}
