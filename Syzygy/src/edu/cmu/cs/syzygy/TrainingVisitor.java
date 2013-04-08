package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class TrainingVisitor extends ASTVisitor {
	public TrainingData data;
	
	private boolean visit(NumberLiteral lit)
	{
		data.intData.increment(lit.getToken());
		return false;
	}
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}
