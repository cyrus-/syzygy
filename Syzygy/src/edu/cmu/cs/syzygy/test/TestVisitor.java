package edu.cmu.cs.syzygy.test;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.cmu.cs.syzygy.NotImplementedException;
import edu.cmu.cs.syzygy.Predictor;
import edu.cmu.cs.syzygy.ResolveBindingException;
import edu.cmu.cs.syzygy.TrainingData;
import edu.cmu.cs.syzygy.Util;

public class TestVisitor extends ASTVisitor {
	private ResultTable table = new ResultTable();
	private Predictor pred = null;
	private File test_file = null;
	private BufferedWriter output_file_buffer = null;
	
	public void preVisit(ASTNode node)
	{
		if(node instanceof Expression) {
			Expression expr = (Expression)node;
			dumpExpression(expr);
			
			ITypeBinding binding = expr.resolveTypeBinding();
			if(binding != null) {
				String type = binding.getQualifiedName();
				try {
					double prob = pred.prob(expr, Util.findContext(expr), type);
					table.addResult(expr, type, prob);
				} catch(NotImplementedException e) {
					System.out.println("Not implemented: " + e.getMessage());
				} catch (ResolveBindingException e) {
					System.out.println("Could not resolve binding for: " + e.getMessage());
				}
			}
		}
	}
	
	private String[] tokenize(String s) {
		return s.replaceAll("(\\W)", " $1 ").split("\\s+");
	}

	
	private String dumpExpression_aux(ASTNode node) throws IOException
	{
		int start = node.getStartPosition();
		// Assumption : 100 characters before the start position will be enough to find the two preceding tokens.
		final int padded_start = start - Math.min(start, 100);
		
		StringBuilder b = new StringBuilder();

		
			byte[] buffer = new byte[start - padded_start];
			FileInputStream file = new FileInputStream(test_file);
			file.skip(padded_start);
			
			file.read(buffer);
			
			file.close();
			
			String[] pretokens = tokenize(new String(buffer));
			
			if (pretokens.length < 2) {
				for (int i = 0; i < pretokens.length; i++) {
					b.append(pretokens[i]);
					b.append(" ");
			    }
				b.reverse();
			} else {
				b.append(pretokens[pretokens.length - 2]);
				b.append(" ");
				b.append(pretokens[pretokens.length - 1]);
				b.append(" ");
			}
			
			for (String t : tokenize(node.toString())) {
				b.append(t);
				b.append(" ");
			}
			
			return b.toString().trim();
		
	}

	
	private void dumpExpression(ASTNode node)
	{
		try {

			String tokens = dumpExpression_aux(node);
			
			output_file_buffer.write(tokens);
			output_file_buffer.newLine();
			output_file_buffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public ResultTable getResults()
	{
		return table;
	}
	
	public void setFile(File f, BufferedWriter buf) {
		test_file = f;
		output_file_buffer = buf;
	}
	
	TestVisitor(TrainingData data)
	{
		pred = new Predictor(data);
	}
}
