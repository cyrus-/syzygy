package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class Trainer {
	public Trainer(CompilationUnit[] trainingUnits) {
		this.trainingUnits = trainingUnits;
	}
	
	private CompilationUnit[] trainingUnits;
	
	public TrainingData train() {
		TrainingVisitor visitor = new TrainingVisitor(); 
				
		for (CompilationUnit c : trainingUnits) {
			c.accept(visitor);
		}
		
		return visitor.data;
	}
}
