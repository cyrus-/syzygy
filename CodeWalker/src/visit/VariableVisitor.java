package visit;

import java.io.Serializable;
import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;


public class VariableVisitor extends BaseVisitor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Hashtable<TypeContext, Integer> frequencies = new Hashtable<TypeContext, Integer>();
	
	public boolean visit(SimpleName name)
	{
		if(!name.isDeclaration()) {
			IBinding bind = name.resolveBinding();
			ITypeBinding typ = name.resolveTypeBinding();
			if(bind.getKind() == IBinding.VARIABLE && !typ.isEnum()) {
				String typName = typ.getQualifiedName();
				TypeContext tctx = new TypeContext(typName, name);
				if(frequencies.containsKey(tctx)) {
					frequencies.put(tctx, frequencies.get(tctx) + 1);
				} else {
					frequencies.put(tctx, 1);
				}
			}
		}
		return false;
	}
	
	public void print()
	{
		for(TypeContext tctx : frequencies.keySet()) {
			
			System.out.println(tctx + " " + frequencies.get(tctx));
		}
	}
}