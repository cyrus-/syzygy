package edu.cmu.cs.syzygy.methods;

public class ArrayAccessMethod implements IMethod {
	private String type;
	
	public ArrayAccessMethod(String t) {
		type = t;
	}
	
	@Override
	public String getTargetType() {
		return type.concat("[]");
	}

	@Override
	public String getReturnType() {
		return type;
	}

	@Override
	public String[] getParameterTypes() {
		return (new String[]{"int"});
	}
	
	public boolean isStatic() {
		return false;
	}
}
