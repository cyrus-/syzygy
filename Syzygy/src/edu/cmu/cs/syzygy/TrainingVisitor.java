package edu.cmu.cs.syzygy;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;


import edu.cmu.cs.syzygy.methods.FieldAccessMethod;
import edu.cmu.cs.syzygy.methods.IMethod;

public class TrainingVisitor extends ASTVisitor {
	public TrainingData data = null;
	
	public void preVisit(ASTNode e) {
		if (e instanceof Expression) {
			visit((Expression)e);
		}
	}
	
	public boolean visit(Expression e) {
		SyntacticContext ctx = Util.findContext(e);
		
		ITypeBinding t = e.resolveTypeBinding();
		
		if (t == null) {
			Debug.print(Debug.Mode.EXCEPTIONS, "Could not resolve binding: " + e.toString());
			return false;
		}
		String type = t.getQualifiedName();
		
		data.incrementTotal(ctx, type);
		
		if (e instanceof BooleanLiteral) {
			train((BooleanLiteral)e, ctx, type);
		} else if (e instanceof ArrayAccess) {
			train ((ArrayAccess)e, ctx, type);
		} else if (e instanceof CharacterLiteral) {
			train((CharacterLiteral)e, ctx, type);
		} else if (e instanceof StringLiteral) {
			train ((StringLiteral)e, ctx, type);
		} else if (e instanceof MethodInvocation) {
			train ((MethodInvocation)e, ctx, type);
		} else if (e instanceof QualifiedName) {
			train ((QualifiedName)e, ctx, type);
		} else if (e instanceof SimpleName) {
			train ((SimpleName)e, ctx, type);
		} else if (e instanceof NullLiteral) {
			train ((NullLiteral)e, ctx, type);
		} else if (e instanceof NumberLiteral) {
			train ((NumberLiteral)e, ctx, type);
		} else if (e instanceof StringLiteral) {
			train ((StringLiteral)e, ctx, type);
		} else if (e instanceof ThisExpression) {
			train ((ThisExpression)e, ctx, type);
		} else if (e instanceof SuperMethodInvocation) {
			train ((SuperMethodInvocation)e, ctx, type);
		} else if (e instanceof FieldAccess) {
			train ((FieldAccess)e, ctx, type);
		} else if (e instanceof SuperFieldAccess) {
			train ((SuperFieldAccess)e, ctx, type);
		}
		
		return true;
		/*
	  if (e instanceof Annotation) return prob((Annotation)e, ctx, type);
	  else if (e instanceof ArrayCreation) return prob((ArrayCreation)e, ctx, type);
	  else if (e instanceof Assignment) return prob((Assignment)e, ctx, type);
	  else if (e instanceof CastExpression) return prob((CastExpression)e, ctx, type);
	  else if (e instanceof ClassInstanceCreation) return prob((ClassInstanceCreation)e, ctx, type);
	  else if (e instanceof ConditionalExpression) return prob((ConditionalExpression)e, ctx, type);
	  else if (e instanceof FieldAccess) return prob((FieldAccess)e, ctx, type);
	  else if (e instanceof InfixExpression) return prob((InfixExpression)e, ctx, type);
	  
	  else if (e instanceof SuperFieldAccess) return prob((SuperFieldAccess)e, ctx, type);
	  else if (e instanceof SuperMethodInvocation) return prob((SuperMethodInvocation)e, ctx, type);
	  
	  else if (e instanceof TypeLiteral) return prob((TypeLiteral)e, ctx, type);
	  else if (e instanceof VariableDeclarationExpression) return prob((VariableDeclarationExpression)e, ctx, type);
	  else throw new RuntimeException("Invalid expression form!");
	  */
    }
	
	private void train(ThisExpression e, SyntacticContext ctx, String type) {
		data.variables.add(ctx, type);
	}

	private void train(NullLiteral e, SyntacticContext ctx, String type) {
		data.literals.add(ctx, type);
	}

	
	public void train(Name e, SyntacticContext ctx, String type) {
		if (Util.isEnumLiteral(e)) {
			data.enumLiterals.add(ctx, type, Util.getFullName(e));
		} else if (e instanceof SimpleName && Util.isVar((SimpleName)e)) {
			data.variables.add(ctx, type);
		} else {
			// Field Access
			IBinding binding = e.resolveBinding();
			
			if (e instanceof SimpleName) {
				if (((SimpleName)e).isDeclaration()) return;
			}
			
			if (binding == null) throw new ResolveBindingException("Could not resolve Binding: " + e.toString());
			
			if (binding instanceof IVariableBinding) {
				if (Modifier.isStatic(binding.getModifiers())) {
					throw new NotImplementedException("Static field");
				}
				
				data.methods.add(ctx, type, data.method_factory.getFieldAccessMethod((IVariableBinding)binding));
			} else {
				// Don't know what to do
				return;
			}
		}		
	}
	
	// TODO : Check
	private void train(SimpleName e, SyntacticContext ctx, String type) {
		train((Name)e, ctx, type);
	}

	private void train(QualifiedName e, SyntacticContext ctx, String type) {
		train((Name)e, ctx, type);
	}

	private void train(BooleanLiteral e, SyntacticContext ctx, String type) {
		// TODO Auto-generated method stub
		
		data.addBoolean(e.booleanValue());
		data.literals.add(ctx, type);
	}

	
	public void train(NumberLiteral lit, SyntacticContext ctx, String type)
	{
		Debug.print(Debug.Mode.NUMBERLITERAL, lit + " " + lit.resolveTypeBinding());
		
	    if(Util.isInt(type)) {
			data.intData.increment(lit.getToken());
		} else if(type.equals("float") || type.equals("double")) {
			data.floatingData.increment(lit.getToken());
		}
		
		data.literals.add(ctx, type);
		
	}
	
	
	public void train(StringLiteral str, SyntacticContext ctx, String type)
	{
		data.stringData.increment(str.getLiteralValue());
		data.literals.add(ctx, type);
	}
	
	
	public void train(CharacterLiteral chr, SyntacticContext ctx, String type)
	{
		data.charData.increment(Character.toString(chr.charValue()));
		data.literals.add(ctx, type);
	}
	

	private void train(ArrayAccess e, SyntacticContext ctx, String type) {
		data.methods.add(ctx, type, data.method_factory.getArrayAccessMethod(type));
	}
	
	private void train(SuperFieldAccess e, SyntacticContext ctx, String type) {
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		if (field_binding == null || Modifier.isStatic(field_binding.getModifiers())) {
			return;
		}
		
	    data.variables.add(SyntacticContext.METHOD_TARGET, field_binding.getDeclaringClass().getQualifiedName());
		
		data.methods.add(ctx, type, data.method_factory.getFieldAccessMethod(e));
	}
	

	private void train(FieldAccess e, SyntacticContext ctx, String type) {
		IVariableBinding field_binding = e.resolveFieldBinding();
		
		if (field_binding == null || Modifier.isStatic(field_binding.getModifiers())) {
			return;
		}
		
		if (e.getExpression() == null) {
			// Implicit 'this'
			data.variables.add(SyntacticContext.METHOD_TARGET, field_binding.getDeclaringClass().getQualifiedName());
		}
		
		data.methods.add(ctx, type, data.method_factory.getFieldAccessMethod(e));
	}

	
	public void train(MethodInvocation mi, SyntacticContext ctx, String type)
	{
		IMethodBinding meth = mi.resolveMethodBinding();

		if(meth == null)
			return;
		
		if (!Modifier.isStatic(meth.getModifiers())) {
		

		if (mi.getExpression() == null) {
			// Assume a this expression
			data.variables.add(SyntacticContext.METHOD_TARGET, meth.getDeclaringClass().getQualifiedName());
		}
		
		data.methods.add(ctx, type, data.method_factory.getJDTMethod(meth));
		}
	}
	
	
	private void train(SuperMethodInvocation e, SyntacticContext ctx, String type) {
		IMethodBinding meth = e.resolveMethodBinding();
		
		if (meth == null)
			return;
		
		// TODO : what to do with super? add to variables?
		data.variables.add(SyntacticContext.METHOD_TARGET, meth.getDeclaringClass().getQualifiedName());
		
		data.methods.add(ctx, type, data.method_factory.getJDTMethod(meth));
	}

	
	public TrainingData getData()
	{
		return data;
	}
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}
