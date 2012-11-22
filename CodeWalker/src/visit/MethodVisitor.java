package visit;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import java.util.Hashtable;

public class MethodVisitor extends BaseVisitor {
	
	private Hashtable<TypeContext, Hashtable<IMethodBinding, Integer> > frequencies = new Hashtable<TypeContext, Hashtable<IMethodBinding, Integer> >();
	
	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		IMethodBinding meth = mi.resolveMethodBinding();
		TypeContext tctx = new TypeContext(bind.getQualifiedName(), mi);
		
		if(frequencies.containsKey(tctx)) {	
			Hashtable<IMethodBinding, Integer> inner_table = frequencies.get(tctx);
			if(inner_table.containsKey(meth)) {
				inner_table.put(meth, inner_table.get(meth) + 1);
			} else {
				inner_table.put(meth, 1);
			}
		} else {
			Hashtable<IMethodBinding, Integer> inner_table = new Hashtable<IMethodBinding, Integer>();
			inner_table.put(meth, 1);
			frequencies.put(tctx, inner_table);
		}
		
		return true;
	}
	
	public void print()
	{
		for(TypeContext tctx : frequencies.keySet()) {
			System.out.println(tctx);
			Hashtable<IMethodBinding, Integer> inner_table = frequencies.get(tctx);
			for(IMethodBinding meth : inner_table.keySet()) {
				System.out.println("\t" + meth.toString() + " " + inner_table.get(meth));
			}
		}
	}
}
