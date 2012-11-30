package visit;

import java.util.Hashtable;
import java.io.Serializable;
import java.util.List;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class LiteralVisitor extends BaseVisitor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Hashtable<Integer, Hashtable<Context.ContextType, Integer> > intFrequencies = new Hashtable<Integer, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<Double, Hashtable<Context.ContextType, Integer> > doubleFrequencies = new Hashtable<Double, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<Float, Hashtable<Context.ContextType, Integer> > floatFrequencies = new Hashtable<Float, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<String, Hashtable<String, Hashtable<Context.ContextType, Integer> > > enumFrequencies = new Hashtable<String, Hashtable<String, Hashtable<Context.ContextType, Integer> > >();
	private Hashtable<String, Hashtable<Context.ContextType, Integer> > stringFrequencies = new Hashtable<String, Hashtable<Context.ContextType, Integer> >();
	
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
				System.out.println("Don't know how to handle expressions of type " + statement.getClass().toString() + " " + statement);
			}
		}
		return true;
	}
	
	// Method and its return type
	private void processMethodInvocation(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();
		
		if(mb == null) {
			System.err.println("failed to resolve binding of file " + mi);
			return;
		}
		
		if(mb.getParameterTypes().length != mi.arguments().size()) {
			System.err.println("Arguments don't agree with types in " + mi + " " + mb);
			return;
		}
		
		for(int i = 0; i < mb.getParameterTypes().length; i++) {
			ITypeBinding type = mb.getParameterTypes()[i];
			Object arg = mi.arguments().get(i);
			
			processExpression((Expression)arg, type.getName());
		}
	}
	
	// Expression and type
	private void processExpression(Expression exp, String typ) {
		
		if(exp == null) {
			System.err.println("exp is null");
			return;
		}
		
		// Type a = 25 (number)
		if(exp instanceof NumberLiteral) {
			NumberLiteral literal = (NumberLiteral)exp;
			
			if(typ == null) {
				try {
					final int val = Integer.parseInt(literal.getToken());
					addInteger(val, literal);
					
				} catch(final Exception e1) {
					try {
						final double val = Double.parseDouble(literal.getToken());
						addDouble(val, literal);
					} catch(final Exception e2) {
						
					}
				}
			} else if(typ.equals("int")) {
				try {
					int num = Integer.parseInt(literal.getToken());
					addInteger(num, literal);
				} catch(final Exception e1) {
					
				}
			} else if(typ.equals("double")) {
				double num = Double.parseDouble(literal.getToken());
				addDouble(num, literal);
			} else if(typ.equals("float")) {
				float num = Float.parseFloat(literal.getToken());
				addFloat(num, literal);
			}
		} else if(exp instanceof CastExpression) {
			CastExpression cast = (CastExpression)exp;
			processExpression(cast.getExpression(), null);
		} else if(exp instanceof QualifiedName) {
			QualifiedName qn = (QualifiedName)exp;
			addEnum(qn);
		} else if(exp instanceof SimpleName) {
			
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
			
			addString(val, lit);
		} else if(exp instanceof InfixExpression) {
			// do nothing
		} else if(exp instanceof MethodInvocation) {
			processMethodInvocation((MethodInvocation)exp);
		} else {
			System.out.println("Don't know what to do with " + exp.getClass().getName());
		}
	}

	private void addEnum(QualifiedName qn) {
		String option = qn.getName().toString();
		String typName = qn.getQualifier().toString();
		Context.ContextType ctx = Context.findContext(qn);
		
		if(enumFrequencies.containsKey(typName)) {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = enumFrequencies.get(typName);
			if(innerTable.containsKey(option)) {
				Hashtable<Context.ContextType, Integer> table = innerTable.get(option);
				if(table.containsKey(ctx))
					table.put(ctx, table.get(ctx) + 1);
				else
					table.put(ctx, 1);
			} else {
				Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
				table.put(ctx, 1);
				innerTable.put(option, table);
			}
		} else {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = new Hashtable<String, Hashtable<Context.ContextType, Integer> >();
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			innerTable.put(option, table);
			enumFrequencies.put(typName, innerTable);
		}
		
	}

	private void addDouble(double num, NumberLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		
		if(doubleFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			doubleFrequencies.put(num, table);
		}
	}
	
	private void addInteger(int num, NumberLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		if(intFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			intFrequencies.put(num, table);
		}
	}
	
	private void addFloat(float num, NumberLiteral lit)
	{
		Context.ContextType ctx = Context.findContext(lit);
		if(floatFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			floatFrequencies.put(num, table);
		}
	}
	
	private void addString(String val, StringLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		if(stringFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(val);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			stringFrequencies.put(val, table);
		}
	}

	public void print()
	{
		System.out.println("========================");
		
		System.out.println("Int Frequencies");
		for(Integer num : intFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Double Frequencies");
		for(Double num : doubleFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Float Frequencies");
		for(Float num : floatFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Enum Frequencies");
		for(String typ : enumFrequencies.keySet()){
			System.out.println(typ);
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = enumFrequencies.get(typ);
			for(String option : innerTable.keySet()) {
				System.out.println("\t" + option);
				Hashtable<Context.ContextType, Integer> table = innerTable.get(option);
				for(Context.ContextType ctx : table.keySet()) {
					System.out.println("\t\t" + ctx.toString() + " " + table.get(ctx));
				}
			}
		}
		
		
		System.out.println("String Frequencies");
		for(String val : stringFrequencies.keySet()) {
			System.out.println("\t" + val.toString());
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(val);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
	}
}
