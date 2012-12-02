package visit;

import java.io.Serializable;
import java.util.Hashtable;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;


public class VariableVisitor extends BaseVisitor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Hashtable<TypeContext, Integer> frequencies = new Hashtable<TypeContext, Integer>();
	
	public Integer getCount(TypeContext tctx) {
		if(frequencies.containsKey(tctx)) {
			return frequencies.get(tctx);
		} else {
			return 0;
		}
	}
	
	public boolean visit(ImportDeclaration decl) {
		return false;
	}
	
	public boolean visit(PackageDeclaration unit) {
		return false;
	}
	
	public static boolean isVar(SimpleName name) {
		if(!name.isDeclaration() && !LiteralVisitor.isEnumLiteral(name)) {
			IBinding bind = name.resolveBinding();
			ITypeBinding typ = name.resolveTypeBinding();
			if(bind == null) {
				//System.err.println("Could not resolve binding of " + name + " " + name.getParent().getParent().getParent().getClass());
				return false;
			}
			
			if(typ == null) {
				//System.err.println("Could not resolve binding of " + name + " " + name.getParent().getParent().getParent().getClass());
				return false;
			}
			
			if(bind.getKind() == IBinding.VARIABLE) {
				return true;
			}
		}
		return false;
	}
		
	
	public boolean visit(SimpleName name)
	{
		if (VariableVisitor.isVar(name)) {
			String typName = typ.getQualifiedName();
			TypeContext tctx = new TypeContext(typName, name);
			if(frequencies.containsKey(tctx)) {
				frequencies.put(tctx, frequencies.get(tctx) + 1);
			} else {
				frequencies.put(tctx, 1);
			}	
			return false;
		} else 	return true;
	}
	
	public void print()
	{
		for(TypeContext tctx : frequencies.keySet()) {
			System.out.println(tctx + " " + frequencies.get(tctx));
		}
	}
}
