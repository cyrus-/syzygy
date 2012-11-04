package dir;

import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class BaseNode {
	
	public abstract void accept(ASTVisitor visitor);
}
