package edu.cmu.cs.syzygy;

import edu.cmu.cs.syzygy.lm.NGram;

public class TrainingData {

	public NGram intData = new NGram(2);
	public NGram floatingData = new NGram(2);
	public NGram stringData = new NGram(2);
	public NGram charData = new NGram(2);

	public Table<SyntacticContext, String> literals = new Table<SyntacticContext, String>(); 
	public Table<SyntacticContext, String> variables = new Table<SyntacticContext, String>();
	public Table<SyntacticContext, String> methods = new Table<SyntacticContext, String>();
	
	public TrainingData() {
	}
	
	public int getLiteralCount(SyntacticContext ctx, String type) {
		return literals.getCount(ctx, type);
	}

	public int getVariableCount(SyntacticContext ctx, String type) {
		return variables.getCount(ctx,  type);
	}
	
	public int getMethodCount(SyntacticContext ctx, String type) {
		return methods.getCount(ctx, type);
	}
	
	public int getLiteralCount(SyntacticContext ctx) {
		return literals.getCount1(ctx);
	}

	public int getVariableCount(SyntacticContext ctx) {
		return variables.getCount1(ctx);
	}
	
	public int getMethodCount(SyntacticContext ctx) {
		return methods.getCount1(ctx);
	}

	public int getLiteralCount() {
		return literals.getTotal();
	}

	public int getVariableCount() {
		return variables.getTotal();
	}

	public int getMethodCount() {
		return methods.getTotal();
	}
}
