package edu.cmu.cs.syzygy.methods;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IMethodBinding;

public class JDTMethodFactory {

	private static Hashtable<IMethodBinding, JDTMethod> ht = new Hashtable<IMethodBinding, JDTMethod>();
	
	public static synchronized JDTMethod getInstance(IMethodBinding meth) {
		JDTMethod m = ht.get(meth);
		
		if (m == null) {
			JDTMethod mnew = new JDTMethod(meth);
			ht.put(meth, mnew);
			return mnew;
		} else return m;
	}
}
