package visit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.LinkedList;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.NullLiteral;

import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;


import org.eclipse.jdt.core.dom.VariableDeclaration;

import visit.Context.ContextType;

import dir.JavaFile;

public class Predictor extends BaseVisitor {
	private LiteralVisitor lit;
	private VariableVisitor variable;
	private MethodVisitor methods;
	private double totalProb = 0;
	private int numPreds = 0; 
	
	private File test_file = null;
	private BufferedWriter output_file_buffer = null;
	
	// Number of variables available in scope
	private int getVars (int offset, String typ, ASTNode node) {
		if (node instanceof MethodDeclaration) {
			VariableCounter vc = new VariableCounter(offset, typ);
			node.accept(vc);
			return vc.getCount();
		} else {
		  if (node.getRoot() == node) {
			  return 0;
		  } else {
			  return getVars (offset, typ, node.getParent());
		  }
		}
	}
	
	private double predVar(int offset, String t, ASTNode exp) {
		int vars = getVars(offset, t, exp);
		
		if (vars == 0) {
			// What now? shouldn't really happen
			return 1;
		} else {
			return (1/vars);
		}
	}
	
	private boolean isLiteral(Expression exp) {
		if ((exp instanceof NumberLiteral) || (exp instanceof StringLiteral)) {
			return true;
		} else if (exp instanceof SimpleName) {
		  return LiteralVisitor.isEnumLiteral((SimpleName)exp);
		} else if(exp instanceof QualifiedName) {
			return LiteralVisitor.qualifiedNameIsEnum((QualifiedName)exp);
		} else {
			return false;
		}
	}
	
	private boolean isMethod(Expression exp) {
		return exp instanceof MethodInvocation;
	}
	
	private boolean isVariable(Expression exp) {
		if (exp instanceof SimpleName) {
			return VariableVisitor.isVar((SimpleName)exp);
		} else return false;
	}
	
	private void predict(TypeContext t, Expression exp)
	{
		int numLit = lit.getCount(t);
		int numVar = variable.getCount(t);
		int numMethods = methods.getCount(t);
		int total = numLit + numMethods + numVar;
		
		
		String foo;
		
		if (isLiteral(exp)) {
			totalProb += (numLit/total) * lit.getProb(t, exp);
			numPreds++;
		} else if (isMethod(exp)) {
			totalProb += (numMethods/total) * methods.getProb(t, (MethodInvocation)exp);
			numPreds++;
		} else if (isVariable(exp)) {
			totalProb += (numVar/total) * predVar(exp.getStartPosition(), t.fullTypeName, exp);
			numPreds++;
		}
	}
	
	public void preVisit (ASTNode node) {
		if (node instanceof Expression) {
			Expression exp = (Expression)node;
			
			ITypeBinding typ = exp.resolveTypeBinding();
			
			if (typ != null) {
				predict(new TypeContext(typ.getQualifiedName(), Context.findContext(node)), exp);
				dumpExpression(node);
			}
		}
	}
	

	private String dumpExpression(ASTNode node)
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
	
	private void dumpExpression1(ASTNode node)
	{
		String tokens = dumpExpression(node);
		
		try {
			System.out.println("Wrote " + tokens);
			output_file_buffer.write(tokens);
			output_file_buffer.newLine();
			output_file_buffer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(tokens);	
	}
	
	
	public double test(JavaFile file, File underlying_file)
	{
		test_file = underlying_file;
		
		file.accept(this);
		
		if (numPreds == 0) {
			return 1;
		} else {
		  return totalProb / numPreds;
		}
	}
	
	public Predictor(LiteralVisitor _lit, VariableVisitor _variable, MethodVisitor _methods, BufferedWriter buf)
	{
		lit = _lit;
		variable = _variable;
		methods = _methods;
		output_file_buffer = buf;
	}
}
