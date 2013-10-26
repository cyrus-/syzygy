package edu.cmu.cs.syzygy.methods;

public interface IMethod {
	public String getTargetType();
	public String getReturnType();
	public String[] getParameterTypes();
	public boolean isStatic();
}
