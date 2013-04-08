package edu.cmu.cs.syzygy;

public class TrainingData {
	
	public NGram intData;
	public NGram floatingData;

	public TrainingData() {
		intData = new NGram();
		floatingData = new NGram();
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
}
