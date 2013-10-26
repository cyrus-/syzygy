package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.IMethodBinding;

import edu.cmu.cs.syzygy.lm.NGram;
import edu.cmu.cs.syzygy.methods.IMethod;

public class TrainingData {

	public NGram intData = new NGram(2);
	public NGram floatingData = new NGram(2);
	public NGram stringData = new NGram(2);
	public NGram charData = new NGram(2);

	public Table<SyntacticContext, String> total = new Table<SyntacticContext, String>();
	public Table<SyntacticContext, String> literals = new Table<SyntacticContext, String>();
	public Table<SyntacticContext, String> variables = new Table<SyntacticContext, String>();
	public FrequencyTable<IMethod> methods = new FrequencyTable<IMethod>();
	public FrequencyTable<String> enumLiterals = new FrequencyTable<String>();
	
	public Pair<Integer, Integer> booleanLiteral = new Pair<Integer, Integer>(0,0);
	// enumLiterals and literals must be kept in sync
	
	public TrainingData() {
	}
	
	public void addBoolean(boolean b) {
		booleanLiteral.fst = booleanLiteral.fst + 1;
		
		if (b) { 
		  booleanLiteral.snd = booleanLiteral.snd + 1;
		}
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
	
	public int getTotalFreq(SyntacticContext ctx, String type) {
		return total.getCount(ctx,type);
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
	
	public int getTotalFreq(SyntacticContext ctx) {
		return total.getCount1(ctx);
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
	
	public int getTotalFreq() {
		return total.getTotal();
	}
	
	public void incrementTotal(SyntacticContext ctx, String type) {
		total.add(ctx, type);
	}
}
