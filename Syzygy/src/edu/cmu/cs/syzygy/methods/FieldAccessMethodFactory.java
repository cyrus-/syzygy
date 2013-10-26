package edu.cmu.cs.syzygy.methods;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

public class FieldAccessMethodFactory {
	
	private static Hashtable<IVariableBinding, FieldAccessMethod> ht = new Hashtable<IVariableBinding,FieldAccessMethod>();
	
	public static synchronized FieldAccessMethod getInstance(FieldAccess e){
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		FieldAccessMethod m = ht.get(field_binding);
		if (m == null) {
			FieldAccessMethod mnew = new FieldAccessMethod(field_binding.getDeclaringClass().getQualifiedName(), field_binding.getType().getQualifiedName(), Modifier.isStatic(field_binding.getModifiers()));
			ht.put(field_binding, mnew);
			return mnew;
		} else return m;
	}
}
