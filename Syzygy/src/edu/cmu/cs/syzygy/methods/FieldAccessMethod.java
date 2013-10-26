package edu.cmu.cs.syzygy.methods;

public class FieldAccessMethod implements IMethod {
	String target_type;
	String return_type;
	boolean is_static;
	
	@Override
	public String getTargetType() {
		// TODO Auto-generated method stub
		return target_type;
	}

	@Override
	public String getReturnType() {
		// TODO Auto-generated method stub
		return return_type;
	}

	@Override
	public String[] getParameterTypes() {
		// TODO Auto-generated method stub
		return new String[]{};
	}

	@Override
	public boolean isStatic() {
		// TODO Auto-generated method stub
		return is_static;
	}
	
	public FieldAccessMethod(String t, String r, boolean s) {
		target_type = t; return_type = r; is_static = s;
	}

}
