package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import edu.cmu.cs.syzygy.lm.NGram;

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
		case METHOD:
			return numMethods / total;
		case VAR:
			return numVar / total;
		}
	}
	
	
	private boolean isInt(String type) {
		return (type.equals("int") || type.equals("short") || type.equals("long") 
				|| type.equals("byte"));
	}

	
	/* different predict methods for different subtypes of AST Node 
	 * public double predict();
	 */
	public double predict(NumberLiteral x, SyntacticContext ctx, String type) {
		double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
		
		if (isInt(type)) {
			return formProb + data.intData.lnProb(Util.normalizeNumberLiteral(x, type));
		} else {
			return formProb + data.floatingData.lnProb(Util.normalizeNumberLiteral(x, type));
		}
	}
	
	public double predict(StringLiteral s, SyntacticContext ctx, String type) {
		double formProb = calculateFormProb(SyntacticForm.LIT, ctx, type);
		
		return formProb + data.stringData.lnProb(s.getLiteralValue());
	}
	
	public double predict(BooleanLiteral b, SyntacticContext ctx, String type) {
		// TRUE = FALSE ???
		return (calculateFormProb(SyntacticForm.LIT, ctx, type) * 0.5);
	}

	/*
    public double predict(CharacterLiteral s, SyntacticContext ctx, String type) {
	}
	
public double predict(StringLiteral s, SyntacticContext ctx, String type) {
	
}
public double predict(StringLiteral s, SyntacticContext ctx, String type) {
	
}
public double predict(StringLiteral s, SyntacticContext ctx, String type) {
	
}
public double predict(StringLiteral s, SyntacticContext ctx, String type) {
	
}
*/

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
