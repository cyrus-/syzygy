package visit;

import java.io.Serializable;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class Method implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Context.ContextType ctx;
	private String className;
	private String name;
	private String returnType;
	private String []argumentTypes;
	boolean isField = false;
	
	public TypeContext getTypeContext() {
		return new TypeContext(returnType, ctx);
	}
	
	public void setContext(Context.ContextType c) {
		ctx = c;
	}
	
	public String getReturnType(){
		return returnType;
	}
	
	public String getFullName(){
		return className + "." + name;
	}

	private String getFullType(String signature) {
	    String packageName = Signature.getSignatureQualifier(signature);
		String fullName = (packageName.trim().equals("")?"":packageName+".") +
		Signature.getSignatureSimpleName(signature);
		
		return fullName;
	}
	
	public int hashCode()
	{
		return this.className.hashCode() + name.hashCode() + returnType.hashCode();
	}
	
	public Method(IMethod im) throws JavaModelException {
		className = im.getDeclaringType().getFullyQualifiedName();
		name = im.getElementName();
		returnType = getFullType(im.getReturnType());
		argumentTypes = im.getParameterTypes();
		
		argumentTypes = new String[im.getParameterTypes().length];
		for (int i = 0; i < argumentTypes.length; i++) {
			argumentTypes[i] = getFullType(im.getParameterTypes()[i]);
		}
	}
	
	public Method(String c, String n, String r, String[] a) {
		className = c;
		name = n;
		returnType = r;
		argumentTypes = a;
	}
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		
		if(obj instanceof Method) {
			Method other = (Method)obj;
			
			if(!other.className.equals(className))
				return false;
			if(!other.name.equals(name))
				return false;
			if(!other.returnType.equals(returnType))
				return false;
			
			if(other.argumentTypes.length != argumentTypes.length)
				return false;
			int i = 0;
			for(String arg : argumentTypes) {
				if(!arg.equals(other.argumentTypes[i]))
					return false;
				i++;
			}
			return true;
		}
		return false;
	}
	
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
		String base = returnType + " " + className + "." + name;
		
		
		if(!isField) { 
			base += "(";
		
			boolean first = true;
		
			for(String arg : argumentTypes) {
				if(first) {
					first = false;
					base = base + arg;
				} else
					base = base + ", " + arg;
			}
		
			base = base + ")";
		}
		
		return base;
	}
	
	public Method(ASTNode exp, String _className, String _returnType, String _name)
	{
		ctx = Context.findContext(exp);
		className = _className;
		returnType = _returnType;
		name = _name;
		argumentTypes = new String[0];
		isField = true;
	}
	
	public Method(ASTNode exp, IMethodBinding meth)
	{
		ctx = Context.findContext(exp);
		
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
