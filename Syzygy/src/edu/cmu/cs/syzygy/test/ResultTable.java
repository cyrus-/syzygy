package edu.cmu.cs.syzygy.test;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.syzygy.SyntacticContext;
import edu.cmu.cs.syzygy.Util;

public class ResultTable {
	private LinkedList<Result> probabilities = new LinkedList<Result>();
	private int total = 0;
	private double total_prob = 0.0;
	
	public void addResult(ASTNode node, String type, final double prob)
	{
		Result result = new Result(node, type, Util.findForm(node), Util.findContext(node), prob);
		probabilities.add(result);
		total++;
		total_prob += prob;
	}
	
	public double averageForType(String type)
	{
		int total = 0;
		double sum = 0.0;
		
		for(Result result : probabilities) {
			if(result.type.equals(type)) {
				total++;
				sum += result.prob;
			}
		}
		
		return sum / (double)total;
	}
	
	public double averageForNodeType(String nodeType)
	{
		int total = 0;
		double sum = 0.0;
		
		for(Result result : probabilities) {
			if(result.nodeType.equals(nodeType)) {
				total++;
				sum += result.prob;
			}
		}
		
		return sum / (double)total;
	}
	
	public double averageForContext(SyntacticContext ctx)
	{
		int total = 0;
		double sum = 0.0;
		
		for(Result result : probabilities) {
			if(result.ctx == ctx) {
				total++;
				sum += result.prob;
			}
		}
		
		return sum / (double)total;
	}
	
	public void merge(ResultTable t2)
	{
		probabilities.addAll(t2.probabilities);
		total += t2.total;
		total_prob += t2.total_prob;
	}
	
	public double getAverage()
	{
		return total_prob / (double)total;
	}
}
