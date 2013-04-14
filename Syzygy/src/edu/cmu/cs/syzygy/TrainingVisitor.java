package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;


public class TrainingVisitor extends ASTVisitor {
	public TrainingData data;
	
	public boolean visit(NumberLiteral lit)
	{
		System.out.println(lit + " " + lit.resolveTypeBinding());
		
		if(Util.isInt(lit)) {
			data.intData.increment(lit.getToken());
		} else if(Util.isFloat(lit) || Util.isDouble(lit)) {
			data.floatingData.increment(lit.getToken());
		}
		// Must add to table
		
		return false;
	}
	
	public boolean visit(StringLiteral str)
	{
		data.stringData.increment(str.getLiteralValue());
		return false;
	}
	

	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		if(bind == null)
			return true;
		
		IMethodBinding meth = mi.resolveMethodBinding();
		
		if(meth == null)
			return true;
		
		
		data.methods.add(Util.findContext(mi), bind.getQualifiedName(), meth);
		return true;
	}
	
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}
