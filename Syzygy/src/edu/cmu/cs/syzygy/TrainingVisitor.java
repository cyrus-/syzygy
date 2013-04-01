package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTVisitor;

public class TrainingVisitor extends ASTVisitor {
	public TrainingData data;
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}
