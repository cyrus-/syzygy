package visit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodVisitor extends BaseVisitor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Hashtable<TypeContext, Hashtable<Method, Integer>> frequencies = new Hashtable<TypeContext, Hashtable<Method, Integer> >();
	private Hashtable<String, ArrayList<Method>> methods = new Hashtable<String, ArrayList<Method>>();
	
	private Method findMethod(IMethodBinding meth)
	{
		String className = meth.getDeclaringClass().getQualifiedName();
		
		if(methods.containsKey(className)) {
			ArrayList<Method> ls = methods.get(className);
			
			for(Method m : ls) {
				if(m.equal(meth)) {
					return m;
				}
			}
			
			Method newm = new Method(meth);
			ls.add(newm);
			
			return newm;
		} else {
			Method newm = new Method(meth);
			ArrayList<Method> ls = new ArrayList<Method>();
			
			ls.add(newm);
			
			methods.put(className, ls);
			
			return newm;
		}
	}
	
	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		IMethodBinding meth = mi.resolveMethodBinding();
		TypeContext tctx = new TypeContext(bind.getQualifiedName(), mi);
		Method method = findMethod(meth);
		
		if(frequencies.containsKey(tctx)) {	
			Hashtable<Method, Integer> inner_table = frequencies.get(tctx);
			if(inner_table.containsKey(method)) {
				inner_table.put(method, inner_table.get(method) + 1);
			} else {
				inner_table.put(method, 1);
			}
		} else {
			Hashtable<Method, Integer> inner_table = new Hashtable<Method, Integer>();
			inner_table.put(method, 1);
			frequencies.put(tctx, inner_table);
		}
		
		return true;
	}
	
	public void print()
	{
		for(TypeContext tctx : frequencies.keySet()) {
			System.out.println(tctx);
			Hashtable<Method, Integer> inner_table = frequencies.get(tctx);
			for(Method meth : inner_table.keySet()) {
				System.out.println("\t" + meth.toString() + " " + inner_table.get(meth));
			}
		}
	}
}
