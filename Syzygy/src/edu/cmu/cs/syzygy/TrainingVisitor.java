package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NumberLiteral;

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
		
		return false;
	}
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}
