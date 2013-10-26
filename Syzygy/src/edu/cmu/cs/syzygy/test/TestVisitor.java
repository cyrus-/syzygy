package edu.cmu.cs.syzygy.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
				}
			}
		}
	}

	
	private String dumpExpression_aux(ASTNode node)
	{
		int start = node.getStartPosition();
		String ret = node.toString().replace('\n', ' ').replace('\t', ' ');
		
		try {
			final int LENGTH = 100;
			int new_start, real_length;
			FileInputStream fin = new FileInputStream(test_file);
			long SIZE_FILE = test_file.length();
			
			if(start - LENGTH < 0) {
				new_start = 0;
				real_length = start;
			} else {
				new_start = start - LENGTH;
				real_length = LENGTH;
			}
			if(new_start + real_length > SIZE_FILE) {
				real_length = (int)SIZE_FILE - new_start;
			}
			
			byte []data = new byte[real_length];
			fin.skip(new_start);
			
			assert(new_start < SIZE_FILE);
			assert(new_start >= 0);
			assert(new_start + real_length < SIZE_FILE);
			int total_read = fin.read(data, 0, real_length);

			assert(total_read == real_length);
			
			boolean inside_whitespace = true;
			int current_pos = real_length - 1;
			String token1 = null;
			String token2 = null;
			int start_token = 0;
			
			while (current_pos >= 0) {
				if(data[current_pos] == ' ' || data[current_pos] == '\t' || data[current_pos] == '\n') {
					
					if(!inside_whitespace) {
						if(token1 == null) {
							token1 = new String(data, current_pos + 1, start_token - current_pos);
						} else {
							token2 = new String(data, current_pos + 1, start_token - current_pos);
							return token2 + " " + token1 + " " + ret;
						}
						inside_whitespace = true;
					}
					current_pos--;
				} else {
					if(inside_whitespace) {
						inside_whitespace = false;
						start_token = current_pos;
					}
					current_pos--;
				}
			}
			
			if(token1 != null) {
				return token1 + " " + ret;
			} else {
				return ret;
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private void dumpExpression(ASTNode node)
	{
		String tokens = dumpExpression_aux(node);
		
		try {
			output_file_buffer.write(tokens);
			output_file_buffer.newLine();
			output_file_buffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(tokens);	
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
