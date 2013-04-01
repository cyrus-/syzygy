package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

import visit.Tracer;

public class Predictor {
	private TrainingData data;

	public Predictor(TrainingData data) {
		this.data = data;
	}
	
	private enum SyntacticForm {
		LIT, VAR, METHOD;
	}
	
	public double calculateFormProb(SyntacticForm form, SyntacticContext ctx, String type) {
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
			break;
		case METHOD:
			return numMethods / total;
			break;
		case VAR:
			return numVar / total;
			break;
		}
	}
	
	/* different predict methods for different subtypes of AST Node 
	 * public double predict();
	 */
	public double predict(NumberLiteral x, SyntacticContext ctx, String type) {
		double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
		
	}
	
	private String getExpectedType(NumberLiteral x) {
		// TODO Auto-generated method stub
		return null;
	}

	private SyntacticContext getContext(NumberLiteral x) {
		// TODO Auto-generated method stub
		return null;
	}

	public double predict(StringLiteral s) {
		
	}
}
