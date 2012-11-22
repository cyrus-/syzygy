package visit;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Context {
	public enum ContextType {
		VAR_DECLARATION,
		STATEMENT,
		METHOD_ARGUMENT,
		OTHER_CONTEXT
	};
	
	public static ContextType findContext1(ASTNode node)
	{
		ASTNode parent = node.getParent();
		
		if(parent instanceof VariableDeclarationStatement) {
			return ContextType.VAR_DECLARATION;
		} else if(parent instanceof VariableDeclarationFragment) {
			return ContextType.VAR_DECLARATION;
		} else if(parent instanceof MethodInvocation) {
			return ContextType.METHOD_ARGUMENT;
		} else if(parent instanceof Block || parent instanceof ExpressionStatement) {
			return ContextType.STATEMENT;
		} else if(parent instanceof VariableDeclarationExpression) {
			return ContextType.VAR_DECLARATION;
		} else if(parent instanceof InfixExpression || parent instanceof ConditionalExpression) {
			return ContextType.METHOD_ARGUMENT;
		} else {
			System.out.println(parent.getClass().toString());
			return ContextType.OTHER_CONTEXT;
		}
	}
	
	/* for debug purposes we may want to print the context of each expression */
	public static ContextType findContext(ASTNode node)
	{
		ContextType type = findContext1(node);
		
		/*
		if(type == ContextType.VAR_DECLARATION) {
			System.out.println(node.toString() + " -> var declaration");
		} else if(type == ContextType.METHOD_ARGUMENT) {
			System.out.println(node.toString() + " -> method argument");
		} else if(type == ContextType.STATEMENT) {
			System.out.println(node.toString() + " -> statement");
		} else {
			System.out.println(node.toString() + " -> other");
		}*/
		
		return type;
	}
}
