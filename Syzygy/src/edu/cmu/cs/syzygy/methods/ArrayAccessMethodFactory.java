package edu.cmu.cs.syzygy.methods;

import java.util.Hashtable;

public class ArrayAccessMethodFactory {
	
	private static Hashtable<String, ArrayAccessMethod> ht = new Hashtable<String,ArrayAccessMethod>();
	
	public static synchronized ArrayAccessMethod getInstance(String t){
		ArrayAccessMethod m = ht.get(t);
		if (m == null) {
			ArrayAccessMethod mnew = new ArrayAccessMethod(t);
			ht.put(t, mnew);
			return mnew;
		} else return m;
	}
}
