package edu.cmu.cs.syzygy.methods;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Modifier;

import edu.cmu.cs.syzygy.NotImplementedException;

public class JDTMethod implements IMethod {

	private IMethodBinding m;
	
	public JDTMethod(IMethodBinding meth) {
		m = meth;
	}
	
	@Override
	public String getTargetType() {
		if (m.getDeclaringClass() == null) {
			System.out.println("wtf, no declaring class for method: " + m);
			throw new NotImplementedException("No declaring class for method");
		}
		return m.getDeclaringClass().getQualifiedName();
	}

	@Override
	public String getReturnType() {
		return m.getReturnType().getQualifiedName();
	}

	@Override
	public String[] getParameterTypes() {
		// TODO Auto-generated method stub
		String[] p = new String[m.getParameterTypes().length];
		for (int i = 0; i < m.getParameterTypes().length; i++) {
			p[i] = m.getParameterTypes()[i].getQualifiedName();
		}
		return p;
	}
	
	public boolean equals(Object o) {
		return m.equals(o);
	}
	
	public int hashCode() {
		return m.hashCode();
	}
	
	public boolean isStatic() {
		return Modifier.isStatic(m.getModifiers());
	}

}
