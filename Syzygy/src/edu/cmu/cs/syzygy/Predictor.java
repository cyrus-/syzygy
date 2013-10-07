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
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
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
	
	public double calculateFormProb(SyntacticForm form, SyntacticContext ctx, String type) throws InvalidDataException {
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
	public double predict(NumberLiteral x, SyntacticContext ctx, String type) throws InvalidDataException {
		double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
		
		if (isInt(type)) {
			return formProb + data.intData.lnProb(Util.normalizeNumberLiteral(x, type));
		} else {
			return formProb + data.floatingData.lnProb(Util.normalizeNumberLiteral(x, type));
		}
	}
	
	public double predict(StringLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
		double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
		
		return formProb + data.stringData.lnProb(s.getLiteralValue());
	}
	
	public double predict(BooleanLiteral b, SyntacticContext ctx, String type) throws InvalidDataException {
		// TRUE = FALSE ???
		return (calculateFormProb(SyntacticForm.LIT, ctx, type) + Math.log(0.5));
	}

	
    public double predict(CharacterLiteral s, SyntacticContext ctx, String type) throws InvalidDataException {
    	double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
    	
    	return formProb + data.charData.lnProb(s.getEscapedValue());
	}

    public double predict(Annotation a, SyntacticContext ctx, String type) {
		throw new NotImplementedException("Annotation not implemented.");
	}
	
	public double predict(ArrayAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayAccess not implemented.");
	}
	
	public double predict(ArrayCreation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayCreation not implemented.");
	}
	
	public double predict(ArrayInitializer e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ArrayInitializer not implemented.");
	}
	
	public double predict(Assignment e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("Assignment not implemented.");
	}
	
	public double predict(CastExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("CastExpression not implemented.");
	}
	
	public double predict(ClassInstanceCreation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ClassInstanceCreation not implemented.");
	}
	
	public double predict(ConditionalExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ConditionalExpression not implemented.");
	}
	
	public double predict(FieldAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("FieldAccess not implemented.");
	}
	
	public double predict(InfixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InfixExpression not implemented.");
	}
	
	public double predict(InstanceofExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("InstanceofExpression not implemented.");
	}
	
	public double predict(MethodInvocation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("MethodInvocation not implemented.");
	}
	
	public double predict(QualifiedName e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("QualifiedName not implemented.");
	}
	
	public double predict(SimpleName e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SimpleName not implemented.");
	}
	
	public double predict(NullLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("NullLiteral not implemented.");
	}
	
	public double predict(PostfixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("PostfixExpression not implemented.");
	}
	
	public double predict(PrefixExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("PrefixExpression not implemented.");
	}
	
	public double predict(SuperFieldAccess e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SuperFieldAccess not implemented.");
	}
	
	public double predict(SuperMethodInvocation e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("SuperMethodInvocation not implemented.");
	}
	
	public double predict(ThisExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("ThisExpression not implemented.");
	}
	
	public double predict(TypeLiteral e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("TypeLiteral not implemented.");
	}
	
	public double predict(VariableDeclarationExpression e, SyntacticContext ctx, String type) {
		throw new NotImplementedException("VariableDeclarationExpression not implemented.");
	}
}
