package visit;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class LiteralVisitor extends BaseVisitor {
	private Hashtable<Integer, Integer> intFrequencies = new Hashtable<Integer, Integer>();
	private Hashtable<Double, Integer> doubleFrequencies = new Hashtable<Double, Integer>();
	private Hashtable<Float, Integer> floatFrequencies = new Hashtable<Float, Integer>();
	private Hashtable<String, Hashtable<String, Integer> > enumFrequencies = new Hashtable<String, Hashtable<String, Integer> >();
	private Hashtable<String, Integer> stringFrequencies = new Hashtable<String, Integer>();
	
	public boolean visit(Block block)
	{
		List<Object> statements = block.statements();
		for (Object statement : statements) {
			// variable declarations
			if (statement instanceof VariableDeclarationStatement) {
				VariableDeclarationStatement vds = (VariableDeclarationStatement) statement;
				Type typ = vds.getType();
				
				for (Object fragment : vds.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
					if (vdf.getInitializer() != null) {
						Expression init = vdf.getInitializer();
						processExpression(init, typ.toString());
					}
				}
			} else if(statement instanceof ExpressionStatement) {
				ExpressionStatement es = (ExpressionStatement) statement;
				
				
				if (es.getExpression() instanceof MethodInvocation) {
					MethodInvocation mi = (MethodInvocation) es.getExpression();
					
					processMethodInvocation(mi);
				}
			} else if(statement instanceof ReturnStatement) {
				ReturnStatement rs = (ReturnStatement)statement;
				Expression re = (Expression)rs.getExpression();
				processExpression(re, null);
			} else if(statement instanceof IfStatement) {
				IfStatement i = (IfStatement)statement;
				processExpression(i.getExpression(), "boolean");
			} else if(statement instanceof ForStatement) {
				ForStatement fs = (ForStatement)statement;
				List updaters = fs.updaters();
				List inits = fs.initializers();
				for(Object up : updaters) {
					processExpression((Expression)up, null);
				}
				for(Object init : inits) {
					processExpression((Expression)init, null);
				}
				processExpression(fs.getExpression(), "boolean");
			} else {
				System.out.println("Don't know how to handle expressions of type " + statement.getClass().toString());
			}
		}
		return true;
	}
	
	// Method and its return type
	private void processMethodInvocation(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();
		
		for(int i = 0; i < mb.getParameterTypes().length; i++) {
			ITypeBinding type = mb.getParameterTypes()[i];
			Object arg = mi.arguments().get(i);
			
			processExpression((Expression)arg, type.getName());
		}
	}
	
	// Expression and type
	private void processExpression(Expression exp, String typ) {
		// Type a = 25 (number)
		if(exp instanceof NumberLiteral) {
			NumberLiteral literal = (NumberLiteral)exp;
			
			if(typ == null) {
				try {
					final int val = Integer.parseInt(literal.getToken());
					addInteger(val);
					
				} catch(final Exception e1) {
					try {
						final double val = Double.parseDouble(literal.getToken());
						addDouble(val);
					} catch(final Exception e2) {
						
					}
				}
			} else if(typ.equals("int")) {
				int num = Integer.parseInt(literal.getToken());
				addInteger(num);
			} else if(typ.equals("double")) {
				double num = Double.parseDouble(literal.getToken());
				addDouble(num);
			} else if(typ.equals("float")) {
				float num = Float.parseFloat(literal.getToken());
				if(floatFrequencies.containsKey(num))
					floatFrequencies.put(num, floatFrequencies.get(num) + 1);
				else
					floatFrequencies.put(num, 1);
			}
		} else if(exp instanceof CastExpression) {
			CastExpression cast = (CastExpression)exp;
			processExpression(cast.getExpression(), null);
		} else if(exp instanceof QualifiedName) {
			QualifiedName qn = (QualifiedName)exp;
			String option = qn.getName().toString();
			String typName = qn.getQualifier().toString();
			
			if(enumFrequencies.containsKey(typName)) {
				Hashtable<String, Integer> innerTable = enumFrequencies.get(typName);
				if(innerTable.containsKey(option)) {
					innerTable.put(option, innerTable.get(option) + 1);
				} else {
					innerTable.put(option, 1);
				}
			} else {
				Hashtable<String, Integer> innerTable = new Hashtable<String, Integer>();
				innerTable.put(option, 1);
				enumFrequencies.put(typName, innerTable);
			}
		} else if(exp instanceof SimpleName) {
			SimpleName sn = (SimpleName)exp;
			// variable!...
			IBinding bnd = sn.resolveBinding();
			
		} else if(exp instanceof VariableDeclarationExpression) {
			VariableDeclarationExpression vd = (VariableDeclarationExpression)exp;
			for (Object fragment : vd.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) fragment;
				if (vdf.getInitializer() != null) {
					Expression init = vdf.getInitializer();
					processExpression(init, typ);
				}
			}
		} else if(exp instanceof PrefixExpression) {
			// do nothing
		} else if(exp instanceof StringLiteral) {
			StringLiteral lit = (StringLiteral)exp;
			String val = lit.getLiteralValue();
			
			if(stringFrequencies.containsKey(val)) {
				stringFrequencies.put(val, stringFrequencies.get(val) + 1);
			} else {
				stringFrequencies.put(val, 1);
			}
		} else if(exp instanceof InfixExpression) {
			// do nothing
		} else if(exp instanceof MethodInvocation) {
			processMethodInvocation((MethodInvocation)exp);
		} else {
			System.out.println("Don't know what to do with " + exp.getClass().getName());
		}
	}

	private void addDouble(double num) {
		if(doubleFrequencies.containsKey(num))
			doubleFrequencies.put(num,  doubleFrequencies.get(num) + 1);
		else
			doubleFrequencies.put(num, 1);
	}
	
	private void addInteger(int num) {
		if(intFrequencies.containsKey(num))
			intFrequencies.put(num, intFrequencies.get(num) + 1);
		else
			intFrequencies.put(num, 1);
	}

	public void print()
	{
		System.out.println("========================");
		
		System.out.println("Int Frequencies");
		for(Integer num : intFrequencies.keySet()) {
			System.out.println("\t" + num.toString() + " " + intFrequencies.get(num));
		}
		
		System.out.println("Double Frequencies");
		for(Double num : doubleFrequencies.keySet()) {
			System.out.println("\t" + num.toString() + " " + doubleFrequencies.get(num));
		}
		
		System.out.println("Float Frequencies");
		for(Float num : floatFrequencies.keySet()) {
			System.out.println("\t" + num.toString() + " " + floatFrequencies.get(num));
		}
		
		System.out.println("Enum Frequencies");
		for(String typ : enumFrequencies.keySet()) {
			System.out.println(typ);
			Hashtable<String, Integer> innerTable = enumFrequencies.get(typ);
			for(String option : innerTable.keySet()) {
				System.out.println("\t" + option + " " + innerTable.get(option).toString());
			}
		}
		
		System.out.println("String Frequencies");
		for(String val : stringFrequencies.keySet()) {
			System.out.println("\t" + val + " " + stringFrequencies.get(val));
		}
	}
}
