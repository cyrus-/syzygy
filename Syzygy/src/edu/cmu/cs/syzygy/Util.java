package edu.cmu.cs.syzygy;

<<<<<<< HEAD
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
=======
import java.util.Hashtable;

>>>>>>> a8a0461ef1a5e61c09bcf78dfba9455d69c063f5
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import visit.Context.ContextType;

public class Util {
	
	public static String normalizeNumberLiteral(NumberLiteral x, String type) {
		if (type.equals("int")) {
			return Integer.toString(Integer.parseInt(x.getToken()));
		} else if (type.equals("short")) {
			return Short.toString(Short.parseShort(x.getToken()));
		} else if (type.equals("long")) {
			return Long.toString(Long.parseLong(x.getToken()));
		} else if (type.equals("byte")) {
			return Byte.toString(Byte.parseByte(x.getToken()));
		} else if (type.equals("float")) {
			return Float.toString(Float.parseFloat(x.getToken()));
		} else if (type.equals("double")) {
			return Double.toString(Double.parseDouble(x.getToken()));
		} else throw new RuntimeException("weird number literal: " + x.getToken());
	}
	
	public static boolean isInt(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("int");
	}
	
	public static boolean isFloat(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("float");
	}
	
	public static boolean isDouble(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("double");
	}

<<<<<<< HEAD
	public static SyntacticContext findContext(ASTNode node)
	{
		ASTNode parent = node.getParent();
		
		if(parent instanceof VariableDeclarationStatement) {
			return SyntacticContext.DECL;
		} else if(parent instanceof VariableDeclarationFragment) {
			return SyntacticContext.DECL;
		} else if(parent instanceof MethodInvocation) {
			return SyntacticContext.CALL;
		} else if(parent instanceof Block || parent instanceof ExpressionStatement) {
			return SyntacticContext.STMT;
		} else if(parent instanceof VariableDeclarationExpression) {
			return SyntacticContext.DECL;
		} else if(parent instanceof InfixExpression || parent instanceof ConditionalExpression) {
			return SyntacticContext.CALL;
		} else {
			return SyntacticContext.OTHER;
		}
	}

	public static <T> void htInc (Hashtable<T, Integer> ht, T t) {
		Integer tmp = ht.get(t);
		if (tmp == null) tmp = 0;
		ht.put(t, tmp + 1);
	}
	
	public static <T> Integer htGetZero (Hashtable<T, Integer> ht, T t) {
		Integer tmp = ht.get(t);
		if (tmp == null) tmp = 0;
		return tmp;
	}
}
