package edu.cmu.cs.syzygy;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
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
}
