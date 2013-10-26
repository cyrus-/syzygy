package edu.cmu.cs.syzygy.methods;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

public class MethodFactory {

	private Hashtable<IMethodBinding, JDTMethod> jdtMethods = new Hashtable<IMethodBinding, JDTMethod>();
	private Hashtable<String, ArrayAccessMethod> arrayAccessMethods= new Hashtable<String,ArrayAccessMethod>();
    private Hashtable<IVariableBinding, FieldAccessMethod> fieldAccessMethods= new Hashtable<IVariableBinding,FieldAccessMethod>();
	
	public JDTMethod getJDTMethod(IMethodBinding meth) {
		JDTMethod m = jdtMethods.get(meth);
		
		if (m == null) {
			JDTMethod mnew = new JDTMethod(meth);
			jdtMethods.put(meth, mnew);
			return mnew;
		} else return m;
	}
	
	public FieldAccessMethod getFieldAccessMethod(SuperFieldAccess e){
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		FieldAccessMethod m = fieldAccessMethods.get(field_binding);
		if (m == null) {
			FieldAccessMethod mnew = new FieldAccessMethod(field_binding.getDeclaringClass().getQualifiedName(), field_binding.getType().getQualifiedName(), Modifier.isStatic(field_binding.getModifiers()));
			fieldAccessMethods.put(field_binding, mnew);
			return mnew;
		} else return m;
	}
	
	public FieldAccessMethod getFieldAccessMethod(FieldAccess e){
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		FieldAccessMethod m = fieldAccessMethods.get(field_binding);
		if (m == null) {
			FieldAccessMethod mnew = new FieldAccessMethod(field_binding.getDeclaringClass().getQualifiedName(), field_binding.getType().getQualifiedName(), Modifier.isStatic(field_binding.getModifiers()));
			fieldAccessMethods.put(field_binding, mnew);
			return mnew;
		} else return m;
	}
	
	
	public synchronized ArrayAccessMethod getArrayAccessMethod(String t){
		ArrayAccessMethod m = arrayAccessMethods.get(t);
		if (m == null) {
			ArrayAccessMethod mnew = new ArrayAccessMethod(t);
			arrayAccessMethods.put(t, mnew);
			return mnew;
		} else return m;
	}
}