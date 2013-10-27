package edu.cmu.cs.syzygy.test;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;

import edu.cmu.cs.syzygy.SyntacticContext;
import edu.cmu.cs.syzygy.Util;

public class ResultTable {
	private LinkedList<Result> probabilities = new LinkedList<Result>();
	private int total = 0;
	private double total_prob = 0.0;
	
	public void addResult(Expression node, String type, final double prob)
	{
		if(Double.isNaN(prob)) {
			throw new ArithmeticException("probability is NaN");
		}
		
		if(Double.isInfinite(prob)) {
			throw new ArithmeticException("probability is infinite");
		}
		
		if(prob < 0.0 || prob > 1.0) {
			throw new ArithmeticException("probability must be between 0.0 and 1.0");
		}
		
		assert(prob >= 0.0 && prob <= 1.0);
		
		System.out.println(node + " type " + prob);
		Result result = new Result(node, type, Util.findForm(node), Util.findContext(node), prob);
		probabilities.add(result);
		total++;
		total_prob += Math.exp(prob);
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
		assert(total > 0);
		System.out.println("total " + total_prob + " " + total);
		return total_prob / (double)total;
	}
	
	public int[] getHistogram()
	{
		int[] h = new int[11];
		
		for (Result r : probabilities) {
			double p = Math.exp(r.prob);
			if (p == 0) {
				h[0]++;
			} else {
			for (int i = 1; i <= 10; i++) {
				double lower = ((double)(i-1))/10;
				double upper = ((double)i)/10;
				
				if ((lower < p) && (p <= upper)) {
					h[i]++;
				}
			}
			}
		}
		return h;
	}
	
}
