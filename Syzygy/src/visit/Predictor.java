package visit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import edu.cmu.cs.syzygy.test.JavaFile;

public class Predictor extends BaseVisitor {
	private LiteralVisitor lit;
	private VariableVisitor variable;
	private MethodVisitor methods;
	private double totalProb = 0;
	private double nonZeroProb = 0.0;
	private int numPreds = 0;
	private int numNonTotalZPreds = 0;
	private int numNonZeroPreds = 0;
	
	private File test_file = null;
	private BufferedWriter output_file_buffer = null;
	private BufferedWriter output_stat = null;
	
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
	
	private void dumpStats(String form , double prob, TypeContext t) throws IOException {
		output_stat.write(form + "\t" + prob + "\t" + t);
		output_stat.newLine();
		output_stat.flush();
	}
	
	private double predict(TypeContext t, Expression exp, boolean log, int n) throws IOException
	{
		int numLit = lit.getCount(t);
		int numVar = variable.getCount(t);
		int numMethods = methods.getCount(t);
		int total = numLit + numMethods + numVar;
		double thisProb = 0.0;
		
		if(total == 0) {
			
			numLit = lit.getCountCtx(t.contextType);
			numVar = variable.getCountCtx(t.contextType);
			numMethods = methods.getCountCtx(t.contextType);
			
			total = numLit + numVar + numMethods;
			
			if (total == 0) {
				numLit = lit.getCountGen();
				numVar = variable.getCountGen();
				numMethods = methods.getCountGen();
				
				total = numLit + numVar + numMethods;
				
				if (total == 0) {
					if (log) numPreds++;
					if (log) Tracer.numPredTotal++;
					if (log) Tracer.numPredZero++;
					return 0;
				}
			}
		} 
		
		
		if (isLiteral(exp)) {
			try {
				thisProb = ((double)numLit/(double)total) * lit.getProb(t, exp);
				//System.out.println(exp + " ====> " + numLit + " " + total + " " + lit.getProb(t,  exp) + " " + thisProb);
				if (log) numPreds++;
				if (log) numNonTotalZPreds++;
				if (log) dumpStats("lit", thisProb, t);
			} catch(java.lang.NumberFormatException e) {
				return -1;
			}
		} else if (isMethod(exp)) {
			
			MethodInvocation mi = (MethodInvocation)exp;
			double p = methods.getProb(t, mi);
			
			//System.out.println("Method " + exp + " p:" + p + " numMethods/Total:" + (double)numMethods/(double)total);
			
			if(p < 0) {
				if (log) Tracer.numMethodsMinus1++;
			    return -1;
		    }
			
			
			thisProb = ((double)numMethods/(double)total) * p;
			
			IMethodBinding ib = mi.resolveMethodBinding();
			
			if(ib == null) { 
			    if (log) Tracer.numMethodsMinus1++;
				return -1;
		    }
			
			ITypeBinding object_type = ib.getDeclaringClass();
			Expression object_expr = mi.getExpression();
			
			if(object_type != null && object_expr != null) {
				double result = predict(new TypeContext(object_type.getQualifiedName(), Context.ContextType.METHOD_ARGUMENT),
					object_expr, false, n + 1);
				if(result == -1) {
					if(log) Tracer.numMethodsMinus1++;
					return -1;
				}
				
				thisProb *= result;
			}
			
			ITypeBinding[] binds = ib.getParameterTypes();
			if(mi.arguments().size() <= binds.length) {
				int i = 0;
				for (Object _arg : mi.arguments()) {
					Expression arg = (Expression)_arg;
					ITypeBinding bind = binds[i];
				
					if(bind == null) {
						if (log) Tracer.numMethodsMinus1++;
						return -1;
					}
				
					double result = predict(new TypeContext(bind.getQualifiedName(), Context.ContextType.METHOD_ARGUMENT),
							arg, false, n + 1);
					if(result == -1) {
						if (log) Tracer.numMethodsMinus1++;
						return -1;
					}
					thisProb *= result;
					++i;
				}
			}
			
			if (log) numPreds++;
			if (log) numNonTotalZPreds++;
			if (log) Tracer.numMethodsPositive++;

			if (log) dumpStats("met", thisProb, t);
		} else if (isVariable(exp)) {
			//System.out.println("VAR " + exp + " numVar:" + numVar + " total:" + total + " predVar:" + predVar(exp.getStartPosition(), t.fullTypeName, exp));
			
			thisProb = ((double)numVar/(double)total) * predVar(exp.getStartPosition(), t.fullTypeName, exp);
			if (log) numPreds++;
			if (log) numNonTotalZPreds++;
			if (log) dumpStats("var", thisProb, t);
			
		} else {
			return -1;
		}
		
		
		if (log) {
		Tracer.numPredTotal++;
		if(thisProb == 0.0)
			Tracer.numPredZero++;
		if(thisProb > 0.0) {
			numNonZeroPreds++;
			nonZeroProb += thisProb;
		}
		totalProb += thisProb;
		assert (thisProb <= 1);
		assert (thisProb >= 0);
		assert(numNonTotalZPreds <= numPreds);
		
		dumpExpression1(exp);
		}
		
		return thisProb;
	}
	
	public void preVisit (ASTNode node) {
		if (node instanceof Expression) {
			Expression exp = (Expression)node;
			
			ITypeBinding typ = exp.resolveTypeBinding();
			
			if (typ != null) {
				try {
					predict(new TypeContext(typ.getQualifiedName(), Context.findContext(node)), exp, true, 1);
				} catch (IOException e) {
				}
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
	
	public Predictor(LiteralVisitor _lit, VariableVisitor _variable, MethodVisitor _methods, BufferedWriter buf, BufferedWriter stats_buf)
	{
		lit = _lit;
		variable = _variable;
		methods = _methods;
		output_file_buffer = buf;
		output_stat = stats_buf;
	}

	public double get_nonzero_test() {
		if(numNonZeroPreds == 0)
			return 0.0;
		
		return nonZeroProb / (double)numNonZeroPreds;
	}
	
	public double get_nonzerototal_test() {
		if(this.numNonTotalZPreds == 0)
			return 0.0;
		
		return totalProb / (double)numNonTotalZPreds;
	}
}
