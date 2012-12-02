package visit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
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
	private LinkedList<Double> accurancies = new LinkedList<Double>();
	private File test_file = null;
	
	
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
			  return getVars (offset, typ, node.getRoot());
		  }
		}
		return 0;
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
	
<<<<<<< Updated upstream
	private boolean isLiteral(Expression exp) {
		if ((exp instanceof NumberLiteral) || (exp instanceof BooleanLiteral) || (exp instanceof CharacterLiteral) || (exp instanceof NullLiteral) || (exp instanceof StringLiteral)) {
			return true;
		} else if (exp instanceof Name) {
		  return LiteralVisitor.isEnumLiteral((Name)exp);
		} else {
			return false;
		}
	}
	
	private double predict (TypeContext t, Expression exp)
	{
		int numLit = lit.getCount(t);
		int numVar = variable.getCount(t);
		int numMethods = methods.getCount(t);
		int total = numLit + numMethods + numVar;
		
		String foo;
		
		if (isLiteral(exp)) {
			return (numLit/total) * lit.getProb(t, exp);
		} else if (isMethod(exp)) {
			return (numMethods/total) * methods.getProb(t, exp);
		} else {
			return (numVar/total) * predVar(exp.getStartPosition(), t.fullTypeName, exp);
      }
		return 0.0;
	}
	
	private String dumpExpression(ASTNode node)
	{
		int start = node.getStartPosition();
		String ret = node.toString();
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	private void dumpExpression1(ASTNode node)
	{
		String tokens = dumpExpression(node);
		System.out.println(tokens);	
	}
	
	private void predictInt(int val, NumberLiteral literal)
	{
		Context.ContextType typ = Context.findContext(literal);
		int allInts = lit.countAllInts(typ);
		int countThisInt = lit.countInt(val, typ);
		
		//System.out.println("Count for all " + allInts + " for " + val + " " + countThisInt);
		dumpExpression1(literal);
		
		if(allInts != 0) {
			/// XXX what if == 0?
			double acc = (double)countThisInt / (double)allInts;
			accurancies.add(acc);
		}
	}
	
	private void predictDouble(double val, NumberLiteral literal)
	{
		Context.ContextType typ = Context.findContext(literal);
		int allDoubles = lit.countAllDoubles(typ);
		int countThisDouble = lit.countDouble(val, typ);
		
		//System.out.println(val + " all " + allDoubles + " " + countThisDouble);
		
		if(allDoubles != 0) {
			double acc = (double)countThisDouble / (double)allDoubles;
			
			accurancies.add(acc);
		}
	}
	
	public boolean visit(NumberLiteral literal)
	{
		try {
			final int val = Integer.parseInt(literal.getToken());
			predictInt(val, literal);
		} catch(final Exception e1) {
			try {
				final double val = Double.parseDouble(literal.getToken());
				predictDouble(val, literal);
			} catch(final Exception e2) {
				// we do nothing
			}
		}
		
		return false;
	}
	
	public boolean visit(QualifiedName qn)
	{
		if(!qn.getQualifier().isQualifiedName())
			return false;
		
		ITypeBinding typ = qn.resolveTypeBinding();
		
		if(typ == null) {
			return false;
		}
		
		String option = qn.getName().toString();
		String typName = typ.getQualifiedName();
		Context.ContextType ctx = Context.findContext(qn);
		
		dumpExpression1(qn);
		predictEnum(typName, option, ctx);
		
		return false;
	}
	
	public boolean visit(StringLiteral litstr)
	{
		Context.ContextType ctx = Context.findContext(litstr);
		String val = litstr.getLiteralValue();
		int countAllStrings = lit.countAllStrings(ctx);
		int countThisString = lit.countString(val, ctx);
		
		//System.out.println(val + " all " + countAllStrings + " " + countThisString);
		
		if(countAllStrings != 0) {
			accurancies.add((double)countThisString / (double)countAllStrings);
		}
		
		return false;
	}
	
	public boolean visit(SimpleName name)
	{
		ITypeBinding typ = name.resolveTypeBinding();
		IBinding bind = name.resolveBinding();
		
		if(typ == null)
			return false;
		if(bind == null)
			return false;
		
		if(name.getParent() instanceof EnumDeclaration || name.getParent() instanceof EnumConstantDeclaration) {
			return false;
		}
		
		if(typ.isEnum()) {
			String typName = typ.getQualifiedName();
			String option = name.toString();

			boolean inThere = false;
			
			for(IVariableBinding b : typ.getDeclaredFields()) {
				if(b.getName().equals(option)) {
					inThere = true;
					break;
				}
			}
			
			if(inThere) {
				predictEnum(typName, option, Context.findContext(name));
			}
		}
		
		return false;
	}
	
	private void predictEnum(String typName, String option, ContextType ctx) {
		int allThisType = lit.countEnumsOfType(typName, ctx);
		int thisOption = lit.countEnumsOfTypeWithOption(typName, option, ctx);
	
		System.out.println(typName + " " + option + " " + thisOption + "/" + allThisType + " " + ctx);
		
		if(allThisType != 0) {
			accurancies.add((double)thisOption / (double)allThisType);
		}
	}

	public boolean visit(MethodInvocation mi)
	{
		dumpExpression1(mi);
		return false;
		
	}
	
	public double test(JavaFile file, File underlying_file)
	{
		test_file = underlying_file;
		
		file.accept(this);
		
		if(accurancies.size() == 0)
			return 1.0;
		else {
			Double sum = 0.0;
			for(Double one : accurancies) {
				sum = sum + one;
			}
			
			return sum / (double)accurancies.size();
		}
	}
	
	public Predictor(LiteralVisitor _lit, VariableVisitor _variable, MethodVisitor _methods)
	{
		lit = _lit;
		variable = _variable;
		methods = _methods;
	}
}
