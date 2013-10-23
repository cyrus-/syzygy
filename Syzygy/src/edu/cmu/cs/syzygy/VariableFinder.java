package edu.cmu.cs.syzygy;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.corext.dom.ScopeAnalyzer;

@SuppressWarnings("restriction")
public class VariableFinder {
	private ScopeAnalyzer sa = null;

	public VariableFinder(CompilationUnit unit)
	{
		sa = new ScopeAnalyzer(unit);
	}

	public IBinding[] getVariables(int offset)
	{
		return sa.getDeclarationsInScope(offset, ScopeAnalyzer.VARIABLES);
	}

	public IBinding[] getMethods(int offset)
	{
		return sa.getDeclarationsInScope(offset, ScopeAnalyzer.METHODS);
	}
	
	public IBinding[] getTypes(int offset)
	{
		return sa.getDeclarationsInScope(offset, ScopeAnalyzer.TYPES);
	}
	
	public ArrayList<IMethodBinding> getStaticMethods(int offset)
	{
		ArrayList<IMethodBinding> ls = new ArrayList<IMethodBinding>();
		
		for(IBinding b : getTypes(offset)) {
			if (b instanceof ITypeBinding) {
				ITypeBinding t = (ITypeBinding)b;
				IMethodBinding[] mbs = t.getDeclaredMethods();
				for(IMethodBinding mb : mbs) {
					int mod = mb.getModifiers();
					if((mod & Modifier.STATIC) == Modifier.STATIC) {
						ls.add(mb);
					}
				}
			}
		}
		return ls;
	}
	
	public int countStaticMethods(int offset)
	{
		int count = 0;
		
		for(IBinding b : getTypes(offset)) {
			if (b instanceof ITypeBinding) {
				ITypeBinding t = (ITypeBinding)b;
				IMethodBinding[] mbs = t.getDeclaredMethods();
				for(IMethodBinding mb : mbs) {
					int mod = mb.getModifiers();
					if((mod & Modifier.STATIC) == Modifier.STATIC) {
						count++;
					}
				}
			}
		}
		
		return count;
	}
}