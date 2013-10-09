package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.IMethodBinding;

import edu.cmu.cs.syzygy.lm.NGram;

public class TrainingData {

	public NGram intData = new NGram(2);
	public NGram floatingData = new NGram(2);
	public NGram stringData = new NGram(2);
	public NGram charData = new NGram(2);

	public Table<SyntacticContext, String> literals = new Table<SyntacticContext, String>();
	public Table<SyntacticContext, String> variables = new Table<SyntacticContext, String>();
	public FrequencyTable<IMethodBinding> methods = new FrequencyTable<IMethodBinding>();
	public FrequencyTable<String> enumLiterals = new FrequencyTable<String>();
	// enumLiterals and literals must be kept in sync
	
	public TrainingData() {
	}
	
	public int getLiteralFreq(SyntacticContext ctx, String type) {
		return literals.getCount(ctx, type);
	}

	public int getVariableFreq(SyntacticContext ctx, String type) {
		return variables.getCount(ctx,  type);
	}
	
	public int getMethodFreq(SyntacticContext ctx, String type) {
		return methods.getFreq(ctx, type);
	}
	
	public int getLiteralFreq(SyntacticContext ctx) {
		return literals.getCount1(ctx);
	}

	public int getVariableFreq(SyntacticContext ctx) {
		return variables.getCount1(ctx);
	}
	
	public int getMethodFreq(SyntacticContext ctx) {
		return methods.getFreq(ctx);
	}

	public int getLiteralFreq() {
		return literals.getTotal();
	}

	public int getVariableFreq() {
		return variables.getTotal();
	}

	public int getMethodFreq() {
		return methods.getTotal();
	}
}
