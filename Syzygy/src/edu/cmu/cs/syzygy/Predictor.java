package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTNode;
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
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

public class Predictor {
	private TrainingData data;

	public Predictor(TrainingData data) {
		this.data = data;
	}
	
	public double prob(ASTNode n) {
		if (n instanceof Expression) return prob((Expression)n);
		else throw new RuntimeException("Invalid AST node type!");
	}
	
	public double prob(Expression e, SyntacticContext c, String t) {
		     if (e instanceof Annotation) return prob((Annotation)e, c, t);
		else if (e instanceof ArrayAccess) return prob((ArrayAccess)e, c, t);
		else if (e instanceof ArrayCreation) return prob((ArrayCreation)e, c, t);
		else if (e instanceof Assignment) return prob((Assignment)e, c, t);
		else if (e instanceof BooleanLiteral) return prob((BooleanLiteral)e, c, t);
		else if (e instanceof CastExpression) return prob((CastExpression)e, c, t);
		else if (e instanceof CharacterLiteral) return prob((CharacterLiteral)e, c, t);
		else if (e instanceof ClassInstanceCreation) return prob((ClassInstanceCreation)e, c, t);
		else if (e instanceof ConditionalExpression) return prob((ConditionalExpression)e, c, t);
		else if (e instanceof FieldAccess) return prob((FieldAccess)e, c, t);
		else if (e instanceof InfixExpression) return prob((InfixExpression)e, c, t);
		else if (e instanceof MethodInvocation) return prob((MethodInvocation)e, c, t);
		else if (e instanceof QualifiedName) return prob((QualifiedName)e, c, t);
		else if (e instanceof SimpleName) return prob((SimpleName)e, c, t);
		else if (e instanceof NullLiteral) return prob((NullLiteral)e, c, t);
		else if (e instanceof NumberLiteral) return prob((NumberLiteral)e, c, t);
		else if (e instanceof PostfixExpression) return prob((PostfixExpression)e, c, t);
		else if (e instanceof PrefixExpression) return prob((PrefixExpression)e, c, t);
		else if (e instanceof StringLiteral) return prob((StringLiteral)e, c, t);
		else if (e instanceof SuperFieldAccess) return prob((SuperFieldAccess)e, c, t);
		else if (e instanceof SuperMethodInvocation) return prob((SuperMethodInvocation)e, c, t);
		else if (e instanceof ThisExpression) return prob((ThisExpression)e, c, t);
		else if (e instanceof TypeLiteral) return prob((TypeLiteral)e, c, t);
		else if (e instanceof VariableDeclarationExpression) return prob((VariableDeclarationExpression)e, c, t);
		else throw new RuntimeException("Invalid expression type!");
	}
	
	private enum SyntacticForm {
		LIT, VAR, METHOD;
	}
	
	public double formProb(SyntacticForm form, SyntacticContext ctx, String type) {
		int numLit = data.getLiteralCount(ctx, type);
		int numVar = data.getVariableCount(ctx, type);
		int numMethods = data.getVariableCount(ctx, type);
		int total = numLit + numMethods + numVar;
		
		if(total == 0) {
			numLit = data.getLiteralCount(ctx);
			numVar = data.getVariableCount(ctx);
			numMethods = data.getVariableCount(ctx);
			total = numLit + numVar + numMethods;
			
			if (total == 0) {
				numLit = data.getLiteralCount();
				numVar = data.getVariableCount();
				numMethods = data.getMethodCount();
				
				total = numLit + numVar + numMethods;
				
				if (total == 0) {
					throw new InvalidDataException("No form data in training data.");
				}
			}
		}
		
		switch (form) {
		case LIT:
			return numLit / total;
		case METHOD:
			return numMethods / total;
		case VAR:
			return numVar / total;
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
		return (formProb(SyntacticForm.LIT, ctx, type) + 0.5);
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
	
	public double prob(FieldAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("FieldAccess not implemented.");
	}
	
	public double prob(InfixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InfixExpression not implemented.");
	}
	
	public double prob(InstanceofExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InstanceofExpression not implemented.");
	}
	
	public double prob(MethodInvocation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("MethodInvocation not implemented.");
	}
	
	public double prob(QualifiedName e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("QualifiedName not implemented.");
	}
	
	public double prob(SimpleName e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SimpleName not implemented.");
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
	
	public double prob(SuperMethodInvocation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SuperMethodInvocation not implemented.");
	}
	
	public double prob(ThisExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ThisExpression not implemented.");
	}
	
	public double prob(TypeLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("TypeLiteral not implemented.");
	}
	
	public double prob(VariableDeclarationExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("VariableDeclarationExpression not implemented.");
	}
}
