package edu.cmu.cs.syzygy;

import java.util.Hashtable;

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
	
	public static boolean isInt(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("int");
	}
	
	public static boolean isFloat(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("float");
	}
	
	public static boolean isDouble(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("double");
	}

	public static <T> void htInc (Hashtable<T, Integer> ht, T t) {
		Integer tmp = ht.get(t);
		if (tmp == null) tmp = 0;
		ht.put(t, tmp + 1);
	}
	
	public static <T> Integer htGetZero (Hashtable<T, Integer> ht, T t) {
		Integer tmp = ht.get(t);
		if (tmp == null) tmp = 0;
		return tmp;
	}
}
