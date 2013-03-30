package visit;

import java.io.Serializable;

import org.eclipse.jdt.core.dom.ASTNode;

public class TypeContext implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String fullTypeName;
	public Context.ContextType contextType;
	
	public String toString()
	{
		return fullTypeName + " " + contextType.toString();
	}
	
	public boolean equals(Object other)
	{
		if(this == other) return true;
		if(!(other instanceof TypeContext)) return false;
		
		TypeContext other1 = (TypeContext)other;
		return other1.fullTypeName.equals(fullTypeName) && other1.contextType.equals(contextType);
 	}
	
	public int hashCode()
	{
		return fullTypeName.hashCode() + contextType.hashCode();
	}
	
	TypeContext(String fullType, ASTNode node)
	{
		fullTypeName = fullType;
		contextType = Context.findContext(node);
	}
	
	TypeContext(String fullType, Context.ContextType ctx)
	{
		fullTypeName = fullType;
		contextType = ctx;
	}
}
