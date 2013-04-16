package edu.cmu.cs.syzygy.test;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.cmu.cs.syzygy.SyntacticContext;
import edu.cmu.cs.syzygy.SyntacticForm;

public class Result {
	public String str = null; // token of the node
	public String nodeType = null; // ASTNode type name
	public String type = null; // type of node
	public SyntacticForm form = SyntacticForm.LIT; // form
	public SyntacticContext ctx = SyntacticContext.OTHER; // context
	public double prob = 0.0; // result
	
	Result(ASTNode _node, String _type, SyntacticForm _form, SyntacticContext _ctx, double _prob)
	{
		str = _node.toString();
		nodeType = _node.getClass().getSimpleName();
		type = _type;
		form = _form;
		ctx = _ctx;
		prob = _prob;
	}
}
