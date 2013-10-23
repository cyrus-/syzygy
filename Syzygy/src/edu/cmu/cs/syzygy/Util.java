package edu.cmu.cs.syzygy;

import java.util.Hashtable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class Util {
	
	public static String normalizeNumberLiteral(NumberLiteral x, String type) {
		if (type.equals("int")) {
			// XXX: this fails when integer is for example 0x23
			try {
				return Integer.toString(Integer.parseInt(x.getToken()));
			} catch(NumberFormatException e) {
				return x.getToken();
			}
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
	

	// TODO : CHECK/FIX
	public static boolean isEnumLiteral (Name name) {
		ITypeBinding typ = name.resolveTypeBinding();
		
		if(name instanceof SimpleName) {
			SimpleName s = (SimpleName)name;
			if(s.isDeclaration())
				return false;
		}
		
		if (typ == null) return false;
		
		if(name.getParent() instanceof EnumDeclaration || name.getParent() instanceof EnumConstantDeclaration) {
			return false;
		}
		
		if(typ.isEnum()) {
			String option = name.toString();
			
			for(IVariableBinding b : typ.getDeclaredFields()) {
				if(b.getName().equals(option)) return true;
			}
		}
		return false;
	}
	
	
	// TODO : CHECK/FIX
	public static boolean isVar(SimpleName name) {
		if(!name.isDeclaration()) {
			IBinding bind = name.resolveBinding();
			ITypeBinding type = name.resolveTypeBinding();

			if(bind == null) {
				throw new ResolveBindingException(name.toString());
			}
			
			if (type == null) {
				throw new ResolveBindingException(name.toString());
			}
			
			if(bind.getKind() == IBinding.VARIABLE) {
				return true;
			}
		}
		return false;
	}

	// TODO : Integer? what else?
	public static boolean isInt(String type) {
		return (type.equals("int") || type.equals("short") || type.equals("long") 
				|| type.equals("byte"));
	}
	
	/*
	public static boolean isInt(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("int");
	}
	*/
	
	public static boolean isFloat(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("float");
	}
	
	public static boolean isDouble(NumberLiteral lit)
	{
		return lit.resolveTypeBinding().getQualifiedName().equals("double");
	}

	public static SyntacticContext findContext(Expression node)
	{
		ASTNode parent = node.getParent();
		
		if(parent instanceof VariableDeclarationStatement) {
			return SyntacticContext.DECL;
		} else if(parent instanceof VariableDeclarationFragment) {
			return SyntacticContext.DECL;
		} else if(parent instanceof MethodInvocation) {
			// TODO : Check
			if (((MethodInvocation)parent).getExpression() == node) {
				return SyntacticContext.METHOD_TARGET;
			} else {
			  return SyntacticContext.METHOD_ARGUMENT;
			}
		} else if(parent instanceof Block || parent instanceof ExpressionStatement) {
			return SyntacticContext.STMT;
		} else if(parent instanceof VariableDeclarationExpression) {
			return SyntacticContext.DECL;
		} else if(parent instanceof InfixExpression || parent instanceof ConditionalExpression) {
			return SyntacticContext.METHOD_ARGUMENT;
		} else {
			return SyntacticContext.OTHER;
		}
	}
	
	public static SyntacticForm findForm(ASTNode node)
	{
		if(node instanceof NumberLiteral || node instanceof CharacterLiteral ||
				node instanceof StringLiteral || node instanceof BooleanLiteral)
		{
			return SyntacticForm.LIT;
		}
		else if(node instanceof MethodInvocation) {
			return SyntacticForm.METHOD;
		} else if(node instanceof VariableDeclaration) {
			return SyntacticForm.VAR;
		}
		return null;
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
	
	public static ThisExpression newThisExpression(ASTNode n) {
		return n.getAST().newThisExpression();
	}
}
