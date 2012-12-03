package visit;

import java.io.Serializable;
import java.util.Hashtable;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import visit.Context.ContextType;

public class LiteralVisitor extends BaseVisitor implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Hashtable<Integer, Hashtable<Context.ContextType, Integer> > intFrequencies = new Hashtable<Integer, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<Double, Hashtable<Context.ContextType, Integer> > doubleFrequencies = new Hashtable<Double, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<Float, Hashtable<Context.ContextType, Integer> > floatFrequencies = new Hashtable<Float, Hashtable<Context.ContextType, Integer> >();
	private Hashtable<String, Hashtable<String, Hashtable<Context.ContextType, Integer> > > enumFrequencies = new Hashtable<String, Hashtable<String, Hashtable<Context.ContextType, Integer> > >();
	private Hashtable<String, Hashtable<Context.ContextType, Integer> > stringFrequencies = new Hashtable<String, Hashtable<Context.ContextType, Integer> >();
	
	
	
	public boolean visit(NumberLiteral literal)
	{
		ITypeBinding typ = literal.resolveTypeBinding();
		
		if(typ == null) {
			try {
				final int val = Integer.parseInt(literal.getToken());
				addInteger(val, literal);
			
			} catch(final Exception e1) {
				try {
					final double val = Double.parseDouble(literal.getToken());
					addDouble(val, literal);
				} catch(final Exception e2) {
				}
			}
		} else if(typ.getName().equals("int")) {
			try {
				int num = Integer.parseInt(literal.getToken());
				addInteger(num, literal);
			} catch(final Exception e1) {
				
			}
		} else if(typ.getName().equals("double")) {
			double num = Double.parseDouble(literal.getToken());
			addDouble(num, literal);
		} else if(typ.getName().equals("float")) {
			float num = Float.parseFloat(literal.getToken());
			addFloat(num, literal);
		}
		
		return false;
	}
	
	public static boolean qualifiedNameIsEnum(QualifiedName qn)
	{
		if(!qn.getQualifier().isQualifiedName()) {
			return false;
		}
		
		ITypeBinding typ = qn.resolveTypeBinding();
		
		if(typ == null)
			return false;
		if(typ.isEnum())
			return true;
		return false;
	}
	
	public boolean visit(QualifiedName qn)
	{
		if(!qualifiedNameIsEnum(qn))
			return false;
		
		String option = qn.getName().toString();
		String typName = qn.getQualifier().getFullyQualifiedName().toString();
		Context.ContextType ctx = Context.findContext(qn);
		
		ITypeBinding typ = qn.resolveTypeBinding();
		
		typName = typ.getQualifiedName();
		
		addEnum(typName, option, ctx);
		return false;
	}
	
	public boolean visit(StringLiteral lit)
	{
		String val = lit.getLiteralValue();
		
		addString(val, lit);
		
		return false;
	}
	
	public static boolean isEnumLiteral (Name name) {
		ITypeBinding typ = name.resolveTypeBinding();
		
		if(name instanceof SimpleName) {
			SimpleName s = (SimpleName)name;
			if(s.isDeclaration())
				return false;
		}
		
		if (typ == null) return false;
		
		if(name.getParent() instanceof EnumDeclaration || name.getParent() instanceof EnumConstantDeclaration) {
			return false;
		}
		
		if(typ.isEnum()) {
			String option = name.toString();
			
			for(IVariableBinding b : typ.getDeclaredFields()) {
				if(b.getName().equals(option)) return true;
			}
		}
		return false;
	}
	
	public boolean visit(SimpleName name)
	{
		if (LiteralVisitor.isEnumLiteral(name)) {
			addEnum(name.resolveTypeBinding().getQualifiedName(), name.toString(), Context.findContext(name));
		}
		
		return false;
	}
	
	private void addEnum(String typName, String option, Context.ContextType ctx) {
		if(enumFrequencies.containsKey(typName)) {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = enumFrequencies.get(typName);
			if(innerTable.containsKey(option)) {
				Hashtable<Context.ContextType, Integer> table = innerTable.get(option);
				if(table.containsKey(ctx))
					table.put(ctx, table.get(ctx) + 1);
				else
					table.put(ctx, 1);
			} else {
				Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
				table.put(ctx, 1);
				innerTable.put(option, table);
			}
		} else {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = new Hashtable<String, Hashtable<Context.ContextType, Integer> >();
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			innerTable.put(option, table);
			enumFrequencies.put(typName, innerTable);
		}
	}

	private void addDouble(double num, NumberLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		
		if(doubleFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			doubleFrequencies.put(num, table);
		}
	}
	
	private void addInteger(int num, NumberLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		if(intFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			intFrequencies.put(num, table);
		}
	}
	
	private void addFloat(float num, NumberLiteral lit)
	{
		Context.ContextType ctx = Context.findContext(lit);
		if(floatFrequencies.containsKey(num)) {
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(num);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			floatFrequencies.put(num, table);
		}
	}
	
	private void addString(String val, StringLiteral lit) {
		Context.ContextType ctx = Context.findContext(lit);
		if(stringFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(val);
			if(table.containsKey(ctx)) {
				table.put(ctx, table.get(ctx) + 1);
			} else {
				table.put(ctx,  1);
			}
		} else {
			Hashtable<Context.ContextType, Integer> table = new Hashtable<Context.ContextType, Integer>();
			table.put(ctx, 1);
			stringFrequencies.put(val, table);
		}
	}

	public void print()
	{
		System.out.println("========================");
		
		System.out.println("Int Frequencies");
		for(Integer num : intFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Double Frequencies");
		for(Double num : doubleFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Float Frequencies");
		for(Float num : floatFrequencies.keySet()) {
			System.out.println("\t" + num.toString());
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(num);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
		
		System.out.println("Enum Frequencies");
		for(String typ : enumFrequencies.keySet()){
			System.out.println(typ);
			Hashtable<String, Hashtable<Context.ContextType, Integer> > innerTable = enumFrequencies.get(typ);
			for(String option : innerTable.keySet()) {
				System.out.println("\t" + option);
				Hashtable<Context.ContextType, Integer> table = innerTable.get(option);
				for(Context.ContextType ctx : table.keySet()) {
					System.out.println("\t\t" + ctx.toString() + " " + table.get(ctx));
				}
			}
		}
		
		
		System.out.println("String Frequencies");
		for(String val : stringFrequencies.keySet()) {
			System.out.println("\t" + val.toString());
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(val);
			for(Context.ContextType ctx : table.keySet()) {
				System.out.println("\t\t" + ctx + " " + table.get(ctx));
			}
		}
	}

	public int countAllInts(ContextType typ) {
		int total = 0;
		
		for(Integer num : intFrequencies.keySet()) {
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(num);
			
			if(table.containsKey(typ)) {
				total += table.get(typ);
			}
		}
		
		return total;
	}

	public int countInt(int val, ContextType typ) {
		if(intFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = intFrequencies.get(val);
			
			if(table.containsKey(typ)) {
				return table.get(typ);
			}
		}
		
		return 0;
	}
	
	public int countAllFloats(ContextType typ) {
		int total = 0;
		
		for(Float num : floatFrequencies.keySet()) {
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(num);
			
			if(table.containsKey(typ)) {
				total += table.get(typ);
			}
		}
		
		return total;
	}
	
	public int countFloat(float val, ContextType typ) {
		if(floatFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = floatFrequencies.get(val);
			
			if(table.containsKey(typ)) {
				return table.get(typ);
			}
		}
		
		return 0;
	}

	public int countAllDoubles(ContextType typ) {
		int total = 0;
		
		for(Double num : doubleFrequencies.keySet()) {
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(num);
			
			if(table.containsKey(typ)) {
				total += table.get(typ);
			}
		}
		
		return total;
	}

	public int countDouble(double val, ContextType typ) {
		if(doubleFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = doubleFrequencies.get(val);
			
			if(table.containsKey(typ)) {
				return table.get(typ);
			}
		}
		
		return 0;
	}

	public int countAllStrings(ContextType ctx) {
		int total = 0;
		
		for(String str : stringFrequencies.keySet()) {
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(str);
			
			if(table.containsKey(ctx)) {
				total += table.get(ctx);
			}
		}
		
		return total;
	}

	public int countString(String val, ContextType ctx) {
		if(stringFrequencies.containsKey(val)) {
			Hashtable<Context.ContextType, Integer> table = stringFrequencies.get(val);
			
			if(table.containsKey(ctx)) {
				return table.get(ctx);
			}
		}
		
		return 0;
	}

	public int countEnumsOfType(String typName, ContextType ctx) {
		int total = 0;
		
		assert(typName != null);
		assert(enumFrequencies != null);
		
		if(enumFrequencies.containsKey(typName)) {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > table = enumFrequencies.get(typName);
			
			for(String option : table.keySet()) {
				Hashtable<Context.ContextType, Integer> inner = table.get(option);
				
				if(inner.containsKey(ctx)) {
					total += inner.get(ctx);
				}
			}
		}
		
		return total;
	}

	public int countEnumsOfTypeWithOption(String typName, String option,
			ContextType ctx) {
		if(enumFrequencies.containsKey(typName)) {
			Hashtable<String, Hashtable<Context.ContextType, Integer> > table = enumFrequencies.get(typName);
			
			if(table.containsKey(option)) {
				Hashtable<Context.ContextType, Integer> inner = table.get(option);
				
				if(inner.containsKey(ctx)) {
					return inner.get(ctx);
				}
			}
		}
		return 0;
	}
	
	public int getCount(TypeContext tctx)
	{
		String typName = tctx.fullTypeName;
		Context.ContextType ctx = tctx.contextType;
		if(typName.equals("int")) {
			return countAllInts(ctx);
		} else if(typName.equals("float")) {
			return countAllFloats(ctx);
		} else if(typName.equals("double")) {
			return countAllDoubles(ctx);
		} else if(typName.equals("java.lang.String")) {
			return countAllStrings(ctx);
		} else {
			if(enumFrequencies.containsKey(typName)) {
				return countEnumsOfType(typName, ctx);
			}
		}
		return 0;
		
	}

	public double getProb(TypeContext t, Expression exp) {
		if(t.fullTypeName.equals("int")) {
			NumberLiteral lit = (NumberLiteral)exp;
			
			int val = Integer.parseInt(lit.getToken());
			int total = countAllInts(t.contextType);
			
			if(total == 0)
				return 0.0;
			
			return (double)countInt(val, t.contextType) / (double)total;
		} else if(t.fullTypeName.equals("float")) {
			NumberLiteral lit = (NumberLiteral)exp;
			float val = Float.parseFloat(lit.getToken());
			int total = countAllFloats(t.contextType);
			
			if(total == 0)
				return 0.0;
			
			return (double)countFloat(val, t.contextType) / (double)total;
		} else if(t.fullTypeName.equals("double")) {
			NumberLiteral lit = (NumberLiteral)exp;
			double val = Double.parseDouble(lit.getToken());
			int total = countAllDoubles(t.contextType);
			
			if(total == 0)
				return 0.0;
			
			return (double)countDouble(val, t.contextType) / (double)total;
		} else if(t.fullTypeName.equals("java.lang.String")) {
			StringLiteral lit = (StringLiteral)exp;
			String val = lit.getLiteralValue();
			int total = countAllStrings(t.contextType);
			
			if(total == 0)
				return 0.0;
			
			return (double)countString(val, t.contextType) / (double)total;
		} else {
			String typName = null;
			String option = null;
			
			if(exp instanceof SimpleName) {
				SimpleName name = (SimpleName)exp;
				
				ITypeBinding typ = name.resolveTypeBinding();
				assert(typ != null);
				
				typName = typ.getQualifiedName();
				option = name.toString();
				
			} else if(exp instanceof QualifiedName) {
				QualifiedName qn = (QualifiedName)exp;
				option = qn.getName().toString();
				typName = qn.getQualifier().getFullyQualifiedName().toString();
			} else {
				assert(false);
			}
			
			if(typName == null)
				return 0.0;
			
			int total = countEnumsOfType(typName, t.contextType);
			
			if(total == 0)
				return 0.0;
			
			return (double)countEnumsOfTypeWithOption(typName, option, t.contextType) / (double)total;
		}
	}
}
