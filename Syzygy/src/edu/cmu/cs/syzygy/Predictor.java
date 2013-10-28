package edu.cmu.cs.syzygy;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import edu.cmu.cs.syzygy.methods.ArrayAccessMethod;
import edu.cmu.cs.syzygy.methods.FieldAccessMethod;
import edu.cmu.cs.syzygy.methods.IMethod;
import edu.cmu.cs.syzygy.methods.JDTMethod;
import edu.cmu.cs.syzygy.methods.MethodFactory;

public class Predictor {
	private TrainingData data;
	private CompilationUnit unit = null;

	public Predictor(TrainingData data) {
		this.data = data;
	}
	
	public void setUnit(CompilationUnit u) {
		unit = u;
	}
	
	private enum SyntacticForm {
		LIT, VAR, METHOD;
	}

	/* EXPRESSIONS */
	
	public double prob(Expression e, SyntacticContext ctx, String type) {
		if (e instanceof Annotation) return prob((Annotation)e, ctx, type);
		else if (e instanceof ArrayAccess) return prob((ArrayAccess)e, ctx, type);
		else if (e instanceof ArrayCreation) return prob((ArrayCreation)e, ctx, type);
		else if (e instanceof Assignment) return prob((Assignment)e, ctx, type);
		else if (e instanceof BooleanLiteral) return prob((BooleanLiteral)e, ctx, type);
		else if (e instanceof CastExpression) return prob((CastExpression)e, ctx, type);
		else if (e instanceof CharacterLiteral) return prob((CharacterLiteral)e, ctx, type);
		else if (e instanceof ClassInstanceCreation) return prob((ClassInstanceCreation)e, ctx, type);
		else if (e instanceof ConditionalExpression) return prob((ConditionalExpression)e, ctx, type);
		else if (e instanceof FieldAccess) return prob((FieldAccess)e, ctx, type);
		else if (e instanceof InfixExpression) return prob((InfixExpression)e, ctx, type);
		else if (e instanceof MethodInvocation) return prob((MethodInvocation)e, ctx, type);
		else if (e instanceof QualifiedName) return prob((QualifiedName)e, ctx, type);
		else if (e instanceof SimpleName) return prob((SimpleName)e, ctx, type);
		else if (e instanceof NullLiteral) return prob((NullLiteral)e, ctx, type);
		else if (e instanceof NumberLiteral) return prob((NumberLiteral)e, ctx, type);
		else if (e instanceof PostfixExpression) return prob((PostfixExpression)e, ctx, type);
		else if (e instanceof PrefixExpression) return prob((PrefixExpression)e, ctx, type);
		else if (e instanceof StringLiteral) return prob((StringLiteral)e, ctx, type);
		else if (e instanceof SuperFieldAccess) return prob((SuperFieldAccess)e, ctx, type);
		else if (e instanceof SuperMethodInvocation) return prob((SuperMethodInvocation)e, ctx, type);
		else if (e instanceof ThisExpression) return prob((ThisExpression)e, ctx, type);
		else if (e instanceof TypeLiteral) return prob((TypeLiteral)e, ctx, type);
		else if (e instanceof VariableDeclarationExpression) return prob((VariableDeclarationExpression)e, ctx, type);
		else throw new NotImplementedException("Invalid expression form!");
	}

	public double formProb(SyntacticForm form, SyntacticContext ctx, String type) {
		int numLit = data.getLiteralFreq(ctx, type);
		int numVar = data.getVariableFreq(ctx, type);
		int numMethods = data.getMethodFreq(ctx, type);
		int total = data.getTotalFreq(ctx, type);
		
		//assert(total == numLit + numVar + numMethods);
		
		if(total == 0) {
			numLit = data.getLiteralFreq(ctx);
			numVar = data.getVariableFreq(ctx);
			numMethods = data.getMethodFreq(ctx);
			total = data.getTotalFreq(ctx, type);
			
			
			if (total == 0) {
				numLit = data.getLiteralFreq();
				numVar = data.getVariableFreq();
				numMethods = data.getMethodFreq();
				
				total = data.getTotalFreq(); //numLit + numVar + numMethods;
				
				assert(total == numLit + numVar + numMethods);
				
				if (total == 0) {
					throw new InvalidDataException("No form data in training data : " + form.toString() + " , " + ctx.toString() + " , " + type);
				}
			}
		}
		
		assert (numLit < total);
		assert (numMethods < total);
		assert (numVar < total);
		
		switch (form) {
		case LIT:
		{
			Debug.print(Debug.Mode.INFO, "Form prob LIT : " + (((double)numLit) / ((double)total)));
			return Math.log(numLit) - Math.log(total);
		}
		case METHOD:
		{
			Debug.print(Debug.Mode.INFO, "Form prob METHOD : " + (((double)numMethods) / ((double)total)));
			return Math.log(numMethods) - Math.log(total);
		}
		case VAR:
		{
			Debug.print(Debug.Mode.INFO, "Form prob VAR : " + (((double)numVar) / ((double)total)));
			return Math.log(numVar) - Math.log(total);
		}
	    default:
	    	throw new RuntimeException("form switching didn't work.. blah");
		}
	}

	
	/* different predict methods for different subtypes of AST Node 
	 * public double predict();
	 */
	public double prob(NumberLiteral x, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.LIT, ctx, type);
		
		if (Util.isInt(type)) {
			return formProb + data.intData.lnProb(x.getToken());
		} else {
			return formProb + data.floatingData.lnProb(x.getToken());
		}
	}
	
	public double prob(StringLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
		double formProb = formProb(SyntacticForm.LIT, ctx, type);
		
		return formProb + data.stringData.lnProb(s.getLiteralValue());
	}
	
	public double prob(BooleanLiteral b, SyntacticContext ctx, String type) throws InvalidDataException {
		double p;
		if (data.booleanLiteral.fst.equals(0)) {
			p = Math.log(0.5);
		} else if (b.booleanValue()) {
			p = Math.log((double) data.booleanLiteral.snd / (double) data.booleanLiteral.fst);
		} else {
			p = Math.log(((double) (data.booleanLiteral.fst - data.booleanLiteral.snd)) / (double) data.booleanLiteral.fst);
		}
		return (formProb(SyntacticForm.LIT, ctx, type) + p);
	}

    public double prob(CharacterLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
    	double formProb = formProb(SyntacticForm.LIT, ctx, type);
    	
    	return formProb + data.charData.lnProb(s.getEscapedValue());
	}

    public double prob(Annotation a, SyntacticContext ctx, String type) {
		throw new NotImplementedException("Annotation not implemented.");
	}
	
	
	public double prob(ArrayCreation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayCreation not implemented.");
	}
	
	public double prob(ArrayInitializer e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayInitializer not implemented.");
	}
	
	public double prob(Assignment e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("Assignment not implemented.");
	}
	
	public double prob(CastExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("CastExpression not implemented.");
	}
	
	public double prob(ClassInstanceCreation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ClassInstanceCreation not implemented.");
	}
	
	public double prob(ConditionalExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ConditionalExpression not implemented.");
	}

	
	public double prob(InfixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InfixExpression not implemented.");
	}

	public double prob(InstanceofExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InstanceofExpression not implemented.");
	}

	// How many methods *could* be called at this location?
	public int callableMethods(Expression invocation) {
		if(unit == null)
			throw new NotImplementedException("Compilation Unit not defined.");
		
		VariableFinder f = new VariableFinder(unit);
		return f.getMethods(invocation.getStartPosition()).length;
	}

	public double methodProb(Expression invocation, IMethod m, SyntacticContext ctx, String type) {
		// Number of different methods that have been used in this context with this type
		int numSeen = data.methods.getCount(ctx, type);
		// Number of times some method has been used in this context with this type
		int total = data.methods.getFreq(ctx, type);
		
		// numMethods different methods, so the rest are methods that were seen before.
		int freqSeen = total - numSeen;
		
		if (total == 0) {
			// Therefore, probability of using a seen method = 0, since no methods have been seen
			
			// Then, uniform probability over all possibilities.
			return Math.log(1 / (double) callableMethods(invocation));
		} else {
			int unseenMethods = callableMethods(invocation) - numSeen;
			
			double mProb = ((double)data.methods.getFreq(ctx, type, m)) / ((double)total);
			
			
			if (unseenMethods == 0) {
				// No unseen methods exist. We are necessarily using a previously seen method
				return Math.log(mProb);
			}
			
			// Otherwise, probability of a previously seen method being used
			double pseen = (double)freqSeen/(double)total;
			double punseen = 1 - pseen;
			
			return Math.log(punseen * (1/((double)unseenMethods)) + pseen * mProb);
		}
	}

	public double methodExpressionProb (Expression invocation, IMethod m, Expression methodTarget, List arguments, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.METHOD, ctx, type);
		
		double mProb = methodProb(invocation, m, ctx, type);
		
		double tProb = 0;
		if (!m.isStatic()) {
			if (methodTarget == null) {
				// Implicit 'this' variable
				variableProb(invocation, SyntacticContext.METHOD_TARGET, m.getTargetType());
			}
			else {
				ITypeBinding target_type = methodTarget.resolveTypeBinding();
				if (target_type == null) throw new ResolveBindingException("Could not resolve binding: " + target_type);
				
				tProb = prob(methodTarget, SyntacticContext.METHOD_TARGET, target_type.getQualifiedName());
			}
		}
		
		double aProb = 0;
		for (int i = 0; i < arguments.size(); i++) {
			aProb += prob((Expression)arguments.get(i), SyntacticContext.METHOD_ARGUMENT, m.getParameterTypes()[i]);
		}
		
		return formProb + mProb + tProb + aProb;
	}
	

	public double prob(FieldAccess e, SyntacticContext ctx, String type) {
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		if (field_binding == null) {
			throw new ResolveBindingException(e.getName().toString());
		}
		
		if (Modifier.isStatic(field_binding.getModifiers())) {
			throw new NotImplementedException("Static field");
		}
		
		FieldAccessMethod m = data.method_factory.getFieldAccessMethod(e);
		
		return methodExpressionProb(e, m, e.getExpression(), new ArrayList(), ctx, type);
	}


	public double prob(ArrayAccess e, SyntacticContext ctx, String type) {
		ArrayAccessMethod m = data.method_factory.getArrayAccessMethod(type);
		List args = new ArrayList();
		args.add(e.getIndex());
		return methodExpressionProb (e, m, e.getArray(), args, ctx, type);
	}
	
	public double prob(MethodInvocation e, SyntacticContext ctx, String type) {
		
		IMethodBinding m = e.resolveMethodBinding();

		if (m == null) {
			throw new ResolveBindingException(e.getName().toString());
		}
		
		if (Modifier.isStatic(m.getModifiers())) {
			throw new NotImplementedException("Static method");
		}
		
		JDTMethod meth = data.method_factory.getJDTMethod(m);
		
		return methodExpressionProb(e, meth, e.getExpression(), e.arguments(), ctx, type);
	}

	
	public double prob(SuperMethodInvocation e, SyntacticContext ctx, String type) {
		IMethodBinding m = e.resolveMethodBinding();
		
		if (m == null) {
			throw new ResolveBindingException(e.getName().toString());
		}
		
		
		return methodExpressionProb(e, data.method_factory.getJDTMethod(m), null, e.arguments(), ctx, type);
	}
	
	
	

	
	private int accessibleVars(Expression e, String type) {
		if(unit == null) {
			// because I'm lazy
			throw new NotImplementedException("Compilation Unit is not set.");
		}

		VariableFinder f = new VariableFinder(unit);
		IBinding[] binds = f.getVariables(e.getStartPosition());
		int count = 0;
		
		for(IBinding b : binds) {
			if(b instanceof IVariableBinding) {
				IVariableBinding vb = (IVariableBinding)b;
				ITypeBinding tb = vb.getType();
				
				
				if(tb != null) {
					String typeOther = tb.getQualifiedName();
					if(type.equals(typeOther)) {
						count++;
					}
				}
			} else {
			}
		}
		
		return count;
	}
	
	public double prob(Name e, SyntacticContext ctx, String type) {

		// should just eliminate this case
		if (e instanceof SimpleName) {
			if (((SimpleName)e).isDeclaration()) throw new NotImplementedException("declaration");
		}
		
		if (Util.isEnumLiteral(e)) {
			Debug.print(Debug.Mode.ENUMLITERALS, Util.getFullName(e));
			double formProb = formProb(SyntacticForm.LIT, ctx, type);
			
			// TODO : need more than this. Use something for unseen literals as well. Maybe same as methods?
			
			double literalCount = (double)data.enumLiterals.getFreq(ctx, type);
			double literalFreq = (double)data.enumLiterals.getFreq(ctx, type, Util.getFullName(e));
			
			if (literalCount == 0) {
				literalCount = data.enumLiterals.getFreq(type);
				if (literalCount == 0) {
					return Math.log(0);
				}
				literalFreq = data.enumLiterals.getFreq(type, Util.getFullName(e));
			}
			
			Debug.print(Debug.Mode.ENUMLITERALS, "Count: " + Double.toString(literalCount));
			Debug.print(Debug.Mode.ENUMLITERALS, "Freq: " + Double.toString(literalFreq));
			
			return formProb + Math.log(literalFreq) - Math.log(literalCount);
			
		} else if (e instanceof SimpleName && Util.isVar((SimpleName)e)) {
		    return variableProb(e, ctx, type);
		} else {
			// Field Access
			IBinding binding = e.resolveBinding();
			
			if (binding == null) throw new ResolveBindingException("Could not resolve Binding: " + e.toString());
			
			if (binding instanceof IVariableBinding) {
				if (Modifier.isStatic(binding.getModifiers())) {
					throw new NotImplementedException("Static field");
				}
				
				FieldAccessMethod m = data.method_factory.getFieldAccessMethod((IVariableBinding)binding);
				
				if (e instanceof QualifiedName) {
				  return methodExpressionProb(e, m, ((QualifiedName)e).getQualifier(), new ArrayList(), ctx, type);
				} else {
				  return methodExpressionProb(e, m, null, new ArrayList(), ctx, type);
				}
					
			} else {
				// Don't know what to do
				throw new NotImplementedException("Name: " + e.toString() + "is not enum literal, variable or field");
			}
		}		
	}
	

	public double prob(SimpleName e, SyntacticContext ctx, String type) {
		return prob((Name)e, ctx, type);
	}
	
	public double prob(QualifiedName e, SyntacticContext ctx, String type) {
		return prob((Name)e, ctx, type);
	}
	
	private double variableProb(Expression e, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.VAR, ctx, type);
		int accVar = accessibleVars(e, type);
		
		Debug.print(Debug.Mode.VARIABLES, "Variable: " + e.toString() + " , form prob : " + formProb + ", number = " + accVar);
		if (accVar == 0) {
			return Math.log(0);
		}
		
		
		return formProb - Math.log(accVar);
	}
	
	
	public double prob(NullLiteral e, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.LIT, ctx, type);
		
		if (Util.isNumber(type)) {
			return formProb + data.intData.lnProb("0");
		} else if (type.equals("String")) {
			return formProb + data.stringData.lnProb("");
		} else {
			return formProb;
		}
	}
	
	public double prob(PostfixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("PostfixExpression not implemented.");
	}

	public double prob(PrefixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("PrefixExpression not implemented.");
	}

	public double prob(SuperFieldAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SuperFieldAccess not implemented.");
	}

	public double prob(ThisExpression e, SyntacticContext ctx, String type) {
		return variableProb(e, ctx, type);
	}

	public double prob(TypeLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("TypeLiteral not implemented.");
	}

	public double prob(VariableDeclarationExpression e, SyntacticContext ctx, String type)
	{
		throw new NotImplementedException("VariableDeclarationExpression not implemented.");
	}
}
