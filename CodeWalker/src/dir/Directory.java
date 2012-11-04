package dir;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;


public class Directory extends BaseNode {
	private ArrayList<BaseNode> children = new ArrayList<BaseNode>();
	
	public void addChild(BaseNode child) {
		children.add(child);
	}

	@Override
	public void accept(ASTVisitor visitor) {
		for(BaseNode child : children) {
			child.accept(visitor);
		}
	}
}
