package visit;

import java.io.Serializable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class Method implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String className;
	private String name;
	private String returnType;
	private String []argumentTypes;
	
	public boolean equal(Object obj)
	{
		if(obj instanceof IMethodBinding) {
			IMethodBinding meth = (IMethodBinding)obj;
			
			if(!meth.getDeclaringClass().getQualifiedName().equals(className))
				return false;
			
			if(!meth.getName().equals(name))
				return false;
			
			ITypeBinding []typs = meth.getParameterTypes();
			if(typs.length != argumentTypes.length)
				return false;
			
			int i = 0;
			for(ITypeBinding typ : typs) {
				if(!typ.getQualifiedName().equals(argumentTypes[i]))
					return false;
				++i;
			}
			
			return true;
		} else {
			return false;
		}
	}
	
	public String toString()
	{
		String base = returnType + " " + className + "." + name + "(";
		
		boolean first = true;
		
		for(String arg : argumentTypes) {
			if(first) {
				first = false;
				base = base + arg;
			} else
				base = base + ", " + arg;
		}
		
		base = base + ")";
		
		return base;
	}
	
	public Method(IMethodBinding meth)
	{
		className = meth.getDeclaringClass().getQualifiedName();
		returnType = meth.getReturnType().getQualifiedName();
		ITypeBinding []typs = meth.getParameterTypes();
		name = meth.getName();
		
		argumentTypes = new String[typs.length];
		
		int i = 0;
		for(ITypeBinding typ : meth.getParameterTypes()) {
			argumentTypes[i] = typ.getQualifiedName();
			++i;
		}
	}
}
