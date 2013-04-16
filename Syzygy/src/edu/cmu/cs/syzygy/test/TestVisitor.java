package edu.cmu.cs.syzygy.test;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.syzygy.NotImplementedException;
import edu.cmu.cs.syzygy.Predictor;
import edu.cmu.cs.syzygy.TrainingData;
import edu.cmu.cs.syzygy.Util;

public class TestVisitor extends ASTVisitor {
	private ResultTable table = new ResultTable();
	private Predictor pred = null;
	
	public void preVisit(ASTNode node)
	{
		if(node instanceof Expression) {
			Expression expr = (Expression)node;
			ITypeBinding binding = expr.resolveTypeBinding();
			if(binding != null) {
				String type = binding.getQualifiedName();
				try {
					double prob = pred.prob(expr, Util.findContext(expr), type);
					table.addResult(expr, type, prob);
				} catch(NotImplementedException e) {
					
				}
			}
		}
	}
	
	public ResultTable getResults()
	{
		return table;
	}
	
	TestVisitor(TrainingData data)
	{
		pred = new Predictor(data);
	}
}
