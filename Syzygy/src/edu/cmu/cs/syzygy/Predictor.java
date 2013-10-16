package edu.cmu.cs.syzygy;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import visit.LiteralVisitor;

public class Predictor {
	private TrainingData data;
	private CompilationUnit unit = null;

	public Predictor(TrainingData data) {
		this.data = data;
	}
	
	public void setUnit(CompilationUnit u) {
		unit = u;
	}
	
	public double predict(ASTNode n) {
		if (n instanceof Expression) return predict((Expression)n);
		else throw new RuntimeException("Invalid AST node type!");
	}
	
	public double predict(Expression e, SyntacticContext c, String t) {
		     if (e instanceof Annotation) return predict((Annotation)e, c, t);
		else if (e instanceof ArrayAccess) return predict((ArrayAccess)e, c, t);
		else if (e instanceof ArrayCreation) return predict((ArrayCreation)e, c, t);
		else if (e instanceof Assignment) return predict((Assignment)e, c, t);
		else if (e instanceof BooleanLiteral) return predict((BooleanLiteral)e, c, t);
		else if (e instanceof CastExpression) return predict((CastExpression)e, c, t);
		else if (e instanceof CharacterLiteral) return predict((CharacterLiteral)e, c, t);
		else if (e instanceof ClassInstanceCreation) return predict((ClassInstanceCreation)e, c, t);
		else if (e instanceof ConditionalExpression) return predict((ConditionalExpression)e, c, t);
		else if (e instanceof FieldAccess) return predict((FieldAccess)e, c, t);
		else if (e instanceof InfixExpression) return predict((InfixExpression)e, c, t);
		else if (e instanceof MethodInvocation) return predict((MethodInvocation)e, c, t);
		else if (e instanceof QualifiedName) return predict((QualifiedName)e, c, t);
		else if (e instanceof SimpleName) return predict((SimpleName)e, c, t);
		else if (e instanceof NullLiteral) return predict((NullLiteral)e, c, t);
		else if (e instanceof NumberLiteral) return predict((NumberLiteral)e, c, t);
		else if (e instanceof ParenthesizedExpression) return predict(((ParenthesizedExpression)e).getExpression(), c, t);
		else if (e instanceof PostfixExpression) return predict((PostfixExpression)e, c, t);
		else if (e instanceof PrefixExpression) return predict((PrefixExpression)e, c, t);
		else if (e instanceof StringLiteral) return predict((StringLiteral)e, c, t);
		else if (e instanceof SuperFieldAccess) return predict((SuperFieldAccess)e, c, t);
		else if (e instanceof SuperMethodInvocation) return predict((SuperMethodInvocation)e, c, t);
		else if (e instanceof ThisExpression) return predict((ThisExpression)e, c, t);
		else if (e instanceof TypeLiteral) return predict((TypeLiteral)e, c, t); //Make this a snew syntactic form.
		else if (e instanceof VariableDeclarationExpression) return predict((VariableDeclarationExpression)e, c, t);
		else throw new RuntimeException("Invalid expression type!");
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
		else throw new RuntimeException("Invalid expression form!");
	}

	public double formProb(SyntacticForm form, SyntacticContext ctx, String type) {
		int numLit = data.getLiteralFreq(ctx, type);
		int numVar = data.getVariableFreq(ctx, type);
		int numMethods = data.getMethodFreq(ctx, type);
		int total = numLit + numMethods + numVar;
		
		if(total == 0) {
			numLit = data.getLiteralFreq(ctx);
			numVar = data.getVariableFreq(ctx);
			numMethods = data.getMethodFreq(ctx);
			total = numLit + numVar + numMethods;
			
			if (total == 0) {
				numLit = data.getLiteralFreq();
				numVar = data.getVariableFreq();
				numMethods = data.getMethodFreq();
				
				total = numLit + numVar + numMethods;
				
				if (total == 0) {
					throw new InvalidDataException("No form data in training data.");
				}
			}
		}
		
		switch (form) {
		case LIT:
			return Math.log(numLit / total);
		case METHOD:
			return Math.log(numMethods / total);
		case VAR:
			return Math.log(numVar / total);
	    default:
	    	throw new RuntimeException("form switching didn't work.. blah");
		}
	}

	private boolean isInt(String type) {
		return (type.equals("int") || type.equals("short") || type.equals("long") 
				|| type.equals("byte"));
	}
	
	/* different predict methods for different subtypes of AST Node 
	 * public double predict();
	 */
	public double prob(NumberLiteral x, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.LIT, ctx, type);
		
		if (isInt(type)) {
			return formProb + data.intData.lnProb(Util.normalizeNumberLiteral(x, type));
		} else {
			return formProb + data.floatingData.lnProb(Util.normalizeNumberLiteral(x, type));
		}
	}
	
	public double prob(StringLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
		double formProb = formProb(SyntacticForm.LIT, ctx, type);
		
		return formProb + data.stringData.lnProb(s.getLiteralValue());
	}
	
	public double prob(BooleanLiteral b, SyntacticContext ctx, String type) throws InvalidDataException {
		// TRUE = FALSE ???
		return (formProb(SyntacticForm.LIT, ctx, type) + Math.log(0.5));
	}

    public double prob(CharacterLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
    	double formProb = formProb(SyntacticForm.LIT, ctx, type);
    	
    	return formProb + data.charData.lnProb(s.getEscapedValue());
	}

    public double prob(Annotation a, SyntacticContext ctx, String type) {
		throw new NotImplementedException("Annotation not implemented.");
	}
	
	public double prob(ArrayAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayAccess not implemented.");
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

	public double methodProb(Expression invocation, IMethodBinding m, SyntacticContext ctx, String type) {
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

	public double methodExpressionProb (Expression invocation, IMethodBinding m, Expression methodTarget, List arguments, SyntacticContext ctx, String type) {
		double formProb = formProb(SyntacticForm.METHOD, ctx, type);
		
		double mProb = methodProb(invocation, m, ctx, type);
		
		double tProb = prob(methodTarget, SyntacticContext.METHOD_TARGET, m.getReturnType().getQualifiedName());
		
		double aProb = 0;
		for (int i = 0; i < arguments.size(); i++) {
			aProb += prob((Expression)arguments.get(i), SyntacticContext.METHOD_ARGUMENT, m.getParameterTypes()[i].getQualifiedName());
		}
		
		return formProb + mProb + tProb + aProb;
	}
	
	public double prob(MethodInvocation e, SyntacticContext ctx, String type) {
		
		IMethodBinding m = e.resolveMethodBinding();
		
		if (m == null) {
			throw new ResolveBindingException(e.getName().toString());
		}
		
		Expression methodTarget = e.getExpression();
		
		if (methodTarget == null) {
			// Ideally, create a ThisExpression. Unfortunately, ThisExpression cannot be initialized by clients. 
			// Probably use a custom class that extends Expression 
		}
		return methodExpressionProb(e, m, e.getExpression(), e.arguments(), ctx, type);
	}

	public double prob(SuperMethodInvocation e, SyntacticContext ctx, String type) {
		IMethodBinding m = e.resolveMethodBinding();
		
		if (m == null) {
			throw new ResolveBindingException(e.getName().toString());
		}
		
		// TODO: Create a method target "super"
		return methodExpressionProb(e, m, null, e.arguments(), ctx, type);
	}
	
	
	public double prob(FieldAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("FieldAccess not implemented.");
	}
	

	// TODO : CHECK/FIX
	private boolean isEnumLiteral (Name name) {
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
	private boolean isVar(SimpleName name) {
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
	
	private int accessibleVars(Expression e, String type) {
		if(unit == null) {
			// because I'm lazy
			throw new NotImplementedException("Compilation Unit is not set.");
		}

		VariableFinder f = new VariableFinder(unit);
		IBinding[] binds = f.getVariables(e.getStartPosition());
		int count = 0;

		for(IBinding b : binds) {
			String typeOther = b.getName();
			if(type.equals(typeOther)) {
				count++;
			}
		}
		
		return count;
	}
	
	public double prob(QualifiedName e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("QualifiedName not implemented.");
	}
	
	public double prob(SimpleName e, SyntacticContext ctx, String type) {
		if (isEnumLiteral(e)) {
			double formProb = formProb(SyntacticForm.LIT, ctx, type);
			
			// TODO : need more than this. Use something for unseen literals as well. Maybe same as methods?
			double p = (double)data.enumLiterals.getFreq(ctx, type, e.toString()) / (double)data.enumLiterals.getCount(ctx, type);
			
			return Math.log(formProb) + Math.log(p);
			
		} else if (isVar(e)) {
			double formProb = formProb(SyntacticForm.VAR, ctx, type);
			
			return formProb - Math.log(accessibleVars(e, type));
		} else {
			throw new NotImplementedException("Name: " + e.toString() + "is neither enum literal nor variable");
		}
	}
	
	public double prob(NullLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("NullLiteral not implemented.");
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
		throw new NotImplementedException("ThisExpression not implemented.");
	}

	public double prob(TypeLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("TypeLiteral not implemented.");
	}

	public double prob(VariableDeclarationExpression e, SyntacticContext ctx, String type)
	{
		throw new NotImplementedException("VariableDeclarationExpression not implemented.");
	}
	
	/* STATEMENTS */
	public double prob(Statement s) {
		if (s instanceof AssertStatement) return prob((AssertStatement)s);
		else if (s instanceof BreakStatement) return prob((BreakStatement)s);
		else if (s instanceof ConstructorInvocation) return prob((ConstructorInvocation)s);
		else if (s instanceof ContinueStatement) return prob((ContinueStatement)s);
		else if (s instanceof DoStatement) return prob((DoStatement)s);
		else if (s instanceof EnhancedForStatement) return prob((EnhancedForStatement)s);
		else if (s instanceof ExpressionStatement) return prob((ExpressionStatement)s);
		else if (s instanceof ForStatement) return prob((ForStatement)s);
		else if (s instanceof IfStatement) return prob((IfStatement)s);
		else if (s instanceof LabeledStatement) return prob((LabeledStatement)s);
		else if (s instanceof ReturnStatement) return prob((ReturnStatement)s);
		else if (s instanceof SuperConstructorInvocation) return prob((SuperConstructorInvocation)s);
		else if (s instanceof SwitchCase) return prob((SwitchCase)s);
		else if (s instanceof SwitchStatement) return prob((SwitchStatement)s);
		else if (s instanceof SynchronizedStatement) return prob((SynchronizedStatement)s);
		else if (s instanceof ThrowStatement) return prob((ThrowStatement)s);
		else if (s instanceof TryStatement) return prob((TryStatement)s);
		else if (s instanceof TypeDeclarationStatement) return prob((TypeDeclarationStatement)s);
		else if (s instanceof VariableDeclarationStatement) return prob((VariableDeclarationStatement)s);
		else if (s instanceof WhileStatement) return prob((WhileStatement)s);
		else throw new RuntimeException("Invalid statement form!");
	}
	
	public double prob(AssertStatement s) {
		throw new NotImplementedException("AssertStatement not implemented.");
	}
	
	public double prob(BreakStatement s) {
		throw new NotImplementedException("BreakStatement not implemented.");
	}
	
	public double prob(ConstructorInvocation s) {
		throw new NotImplementedException("ConstructorInvocation not implemented.");
	}
	
	public double prob(ContinueStatement s) {
		throw new NotImplementedException("ContinueStatement not implemented.");
	}
	
	public double prob(DoStatement s) {
		throw new NotImplementedException("DoStatement not implemented.");
	}
	
	public double prob(EnhancedForStatement s) {
		throw new NotImplementedException("EnhancedForStatement not implemented.");
	}
	
	public double prob(ExpressionStatement s) {
		throw new NotImplementedException("ExpressionStatement not implemented.");
	}
	
	public double prob(ForStatement s) {
		throw new NotImplementedException("ForStatement not implemented.");
	}
	
	public double prob(IfStatement s) {
		throw new NotImplementedException("IfStatement not implemented.");
	}
	
	public double prob(LabeledStatement s) {
		throw new NotImplementedException("LabeledStatement not implemented.");
	}
	
	public double prob(ReturnStatement s) {
		throw new NotImplementedException("ReturnStatement not implemented.");
	}
	
	public double prob(SuperConstructorInvocation s) {
		throw new NotImplementedException("SuperConstructorInvocation not implemented.");
	}
	
	public double prob(SynchronizedStatement s) {
		throw new NotImplementedException("SynchronizedStatement not implemented.");
	}
	
	public double prob(ThrowStatement s) {
		throw new NotImplementedException("ThrowStatement not implemented.");
	}
	
	public double prob(TryStatement s) {
		throw new NotImplementedException("TryStatement not implemented.");
	}
	
	public double prob(TypeDeclarationStatement s) {
		throw new NotImplementedException("TypeDeclarationStatement not implemented.");
	}
	
	public double prob(VariableDeclarationStatement s) {
		throw new NotImplementedException("VariableDeclarationStatement not implemented.");
	}
	
	public double prob(WhileStatement s) {
		throw new NotImplementedException("WhileStatement not implemented.");
	}
}
