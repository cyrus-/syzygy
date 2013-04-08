package edu.cmu.cs.syzygy;

import edu.cmu.cs.syzygy.lm.NGram;

public class TrainingData {

	public NGram intData = new NGram(2);
	public NGram floatingData = new NGram(2);
	public NGram stringData = new NGram(2);
	public NGram charData = new NGram(2);

	public TrainingData() {
	}
	
	public int getLiteralCount(SyntacticContext ctx, String type) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getVariableCount(SyntacticContext ctx, String type) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getMethodCount(SyntacticContext ctx, String type) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getLiteralCount(SyntacticContext ctx) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getVariableCount(SyntacticContext ctx) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public int getMethodCount(SyntacticContext ctx) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getLiteralCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getVariableCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMethodCount() {
		// TODO Auto-generated method stub
		return 0;
	}
}
