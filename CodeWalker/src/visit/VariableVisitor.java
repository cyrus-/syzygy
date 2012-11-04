package visit;

import java.util.Hashtable;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;


public class VariableVisitor extends BaseVisitor {
	
	private Hashtable<String, Integer> frequencies = new Hashtable<String, Integer>();
	
	public boolean visit(SimpleName name)
	{
		if(!name.isDeclaration()) {
			IBinding bind = name.resolveBinding();
			ITypeBinding typ = name.resolveTypeBinding();
			if(bind.getKind() == IBinding.VARIABLE && !typ.isEnum()) {
				String typName = typ.getName();
				
				if(frequencies.containsKey(typName)) {
					frequencies.put(typName, frequencies.get(typName) + 1);
				} else {
					frequencies.put(typName, 1);
				}
			}
		}
		return false;
	}
	
	public void print()
	{
		for(String typ : frequencies.keySet()) {
			System.out.println(typ + " " + frequencies.get(typ));
		}
	}
}