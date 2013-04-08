package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.NumberLiteral;

public class Util {
	
	public static String normalizeNumberLiteral(NumberLiteral x, String type) {
		if (type.equals("int")) {
			return Integer.toString(Integer.parseInt(x.getToken()));
		} else if (type.equals("short")) {
			return Short.toString(Short.parseShort(x.getToken()));
		} else if (type.equals("long")) {
			return Long.toString(Long.parseLong(x.getToken()));
		} else if (type.equals("byte")) {
			return Byte.toString(Byte.parseByte(x.getToken()));
		} else if (type.equals("float")) {
			return Float.toString(Float.parseFloat(x.getToken()));
		} else if (type.equals("double")) {
			return Double.toString(Double.parseDouble(x.getToken()));
		} else throw new RuntimeException("weird number literal: " + x.getToken());
	}

}
