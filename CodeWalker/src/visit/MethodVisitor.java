package visit;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import java.util.Hashtable;

public class MethodVisitor extends BaseVisitor {
	
	private Hashtable<String, Hashtable<IMethodBinding, Integer> > frequencies = new Hashtable<String, Hashtable<IMethodBinding, Integer> >();
	
	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		String typName = bind.getQualifiedName();
		IMethodBinding meth = mi.resolveMethodBinding();
		
		if(frequencies.containsKey(typName)) {
			Hashtable<IMethodBinding, Integer> inner_table = frequencies.get(typName);
			if(inner_table.containsKey(meth)) {
				inner_table.put(meth, inner_table.get(meth) + 1);
			} else {
				inner_table.put(meth, 1);
			}
		} else {
			Hashtable<IMethodBinding, Integer> inner_table = new Hashtable<IMethodBinding, Integer>();
			inner_table.put(meth, 1);
			frequencies.put(typName, inner_table);
		}
		
		System.out.println(mi + " " + bind.getQualifiedName() + " " + bind.getName());
		return true;
	}
	
	public void print()
	{
		for(String typName : frequencies.keySet()) {
			System.out.println(typName);
			Hashtable<IMethodBinding, Integer> inner_table = frequencies.get(typName);
			for(IMethodBinding meth : inner_table.keySet()) {
				System.out.println("\t" + meth.toString() + " " + inner_table.get(meth));
			}
		}
	}
}
