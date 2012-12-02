package visit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class MethodVisitor extends BaseVisitor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Hashtable<TypeContext, Hashtable<Method, Integer>> frequencies = new Hashtable<TypeContext, Hashtable<Method, Integer> >();
	private Hashtable<String, ArrayList<Method>> methods = new Hashtable<String, ArrayList<Method>>();
	private Hashtable<String, ArrayList<Method>> declaredMethods = new Hashtable<String, ArrayList<Method>>();
	
	// temporary
	private List<IBinding> member_bindings = null;
	private String thisClassName = null;
	
	/*
	public double getProb (TypeContext t, IMethodBinding meth) {
		Method m = toMethod(meth);
		
		int total = 0;
		for (Entry<Method, Integer> e : frequencies.get(t).entrySet()) {
			total += e.getValue();
		}
		
		return (frequencies.get(t).get(m)/total); 
	}*/
	
	public double getProb(TypeContext t, MethodInvocation mi) {
		// total number (used + unused) methods available of given return type
		
		Method m = new Method(mi, mi.resolveMethodBinding());
		
		if(!frequencies.containsKey(t)) {
			return 0.0;
		}
		
		Hashtable<Method, Integer> table = frequencies.get(t);

		int total = 0;
		for (Entry<Method, Integer> e : table.entrySet()) {
			total += e.getValue();
		}
		int numseen = table.size();
		
		if (total == 0) return -1;
		
		if (table.containsKey(m)) {
			// seen method
			return (1 - (numseen/total)) * (table.get(m).intValue()/total);
		} else {
			// unseen method
			MethodCounter mctr = new MethodCounter(mi.getStartPosition(), t.fullTypeName);
			mi.getRoot().accept(mctr);
			
			int tmp = (mctr.getCount() - numseen);
			
			if (tmp == 0) {
				return -1;
			} else {
			  return (numseen/total) * (1/tmp);
			}
		}
	}
	
	
	public int getCount (TypeContext t) {
		int total = 0;
		if(frequencies.containsKey(t)) {
			Hashtable<Method, Integer> table = frequencies.get(t);
			for(Method m : table.keySet()) {
				total += table.get(m);
			}
		}
		return total;
	}
	
	private boolean hasUsedMethod(IMethodBinding meth)
	{
		ITypeBinding cl = meth.getDeclaringClass();
		
		if(cl == null)
			return false;
		
		String className = cl.getQualifiedName();
		
		if(methods.containsKey(className)) {
			ArrayList<Method> ls = methods.get(className);
			
			for(Method m : ls) {
				if(m.equal(meth)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
	
	private void removeFromTable(Hashtable<String, ArrayList<Method>> table, IMethodBinding meth)
	{
		ITypeBinding cl = meth.getDeclaringClass();
		
		if(cl == null)
			return;
		
		String className = cl.getQualifiedName();
		
		ArrayList<Method> ls = table.get(className);
		
		assert(ls != null);
		
		for(Method m : ls) {
			if(m.equal(meth)) {
				ls.remove(m);
				return;
			}
		}
		assert(false);
	}
	
	private void addToTable(Hashtable<String, ArrayList<Method>> table, IMethodBinding meth, ASTNode md)
	{
		ITypeBinding cl = meth.getDeclaringClass();
		
		if(cl == null)
			return;
		
		String className = cl.getQualifiedName();
		Method newm = new Method(md, meth);
		
		
		if(table.containsKey(className)) {
			ArrayList<Method> ls = table.get(className);
		
			ls.add(newm);
			
		} else {
			ArrayList<Method> ls = new ArrayList<Method>();
			ls.add(newm);
			table.put(className, ls);
		}
	}
	
	private boolean containsMethod(Hashtable<String, ArrayList<Method>> table, IMethodBinding meth)
	{
		ITypeBinding cl = meth.getDeclaringClass();
		
		if(cl == null)
			return false;
		
		String className = cl.getQualifiedName();
		
		if(table.containsKey(className)) {
			ArrayList<Method> ls = table.get(className);
			
			for(Method m : ls) {
				if(m.equal(meth)) {
					return true;
				}
			}
			return false;
		} else {
			return false;
		}
	}
	
	private Method findMethod(IMethodBinding meth, MethodInvocation mi)
	{
		ITypeBinding cl = meth.getDeclaringClass();
		
		if(cl == null)
			return null;
		
		String className = cl.getQualifiedName();
		
		if(methods.containsKey(className)) {
			ArrayList<Method> ls = methods.get(className);
			
			for(Method m : ls) {
				if(m.equal(meth)) {
					return m;
				}
			}
			
			Method newm = new Method(mi, meth);
			ls.add(newm);
			
			return newm;
		} else {
			Method newm = new Method(mi, meth);
			ArrayList<Method> ls = new ArrayList<Method>();
			
			ls.add(newm);
			
			methods.put(className, ls);
			
			return newm;
		}
	}
	
	public boolean visit(FieldDeclaration fd)
	{
		List frags = fd.fragments();
		
		for(Object frag : frags) {
			if(frag instanceof VariableDeclarationFragment) {
				if((fd.getModifiers() & Modifier.PUBLIC) != 0) {
					VariableDeclarationFragment fragment = (VariableDeclarationFragment)frag;
					member_bindings.add(fragment.getName().resolveBinding());
					
					TypeDeclaration tdecl = (TypeDeclaration)fd.getParent();
					ITypeBinding typ = tdecl.resolveBinding();
					if(typ != null && thisClassName == null) {
						thisClassName = typ.getQualifiedName();
					}
				}
			}
		}
		
		return true;
	}
	
	public boolean visit(CompilationUnit unit)
	{
		member_bindings = new LinkedList<IBinding>();
		thisClassName = null;
		return true;
	}
	
	private boolean isMemberBinding(IBinding b)
	{
		for(IBinding member : member_bindings) {
			if(b.equals(member)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isPublicField(QualifiedName qn)
	{
		IBinding bind = qn.resolveBinding();
		
		if(bind instanceof IVariableBinding) {
			IVariableBinding var = (IVariableBinding)bind;
			
			if(var.isField() && !var.isEnumConstant()) {
				return true;
			}
		}
		return false;
	}
	
	public static Method getPublicFieldAsMethod(QualifiedName qn)
	{
		ITypeBinding typ = qn.resolveTypeBinding();
		if(typ == null)
			return null;
		
		IBinding bind = qn.resolveBinding();
		if(bind == null)
			return null;
		IVariableBinding var = (IVariableBinding)bind;
		ITypeBinding cls = var.getDeclaringClass();
		
		if(cls == null)
			return null;
		
		String name = qn.getFullyQualifiedName();
		
		return new Method(qn, cls.getQualifiedName(), typ.getQualifiedName(), name);
	}

	public boolean visit(QualifiedName qn)
	{
		if(isPublicField(qn)) {
			Method method = getPublicFieldAsMethod(qn);
			
			if(method != null)
				addMethod(method);
		}
		return false;
	}
	
	public boolean isSelfPublicField(SimpleName name)
	{
		IBinding bind = name.resolveBinding();
		if(bind == null)
			return false;
		ITypeBinding typ = name.resolveTypeBinding();
		if(typ == null)
			return false;
		
		if(bind != null && !name.isDeclaration() && isMemberBinding(bind)) {
			return true;
		}
		return false;
	}
	
	public boolean visit(SimpleName name)
	{
		if(isSelfPublicField(name)) {
			ITypeBinding typ = name.resolveTypeBinding();
		
			Method method = new Method(name, thisClassName, typ.getQualifiedName(), name.getFullyQualifiedName());
			
			addMethod(method);
		}
		
		return false;
	}
	
	public boolean visit(MethodDeclaration md)
	{
		IMethodBinding mb = md.resolveBinding();
		
		if(mb == null)
			return true;
		
		addToTable(declaredMethods, mb, md);
		
		return true;
	}

	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		if(bind == null)
			return true;
		
		IMethodBinding meth = mi.resolveMethodBinding();
		
		if(meth == null)
			return true;
		
		Method method = findMethod(meth, mi);
		
		if(method == null)
			return true;
		
		addMethod(method);
		
		return true;
	}
	
	public void addMethod(Method method)
	{
		TypeContext tctx = method.getTypeContext();
		
		if(frequencies.containsKey(tctx)) {	
			Hashtable<Method, Integer> inner_table = frequencies.get(tctx);
			if(inner_table.containsKey(method)) {
				inner_table.put(method, inner_table.get(method) + 1);
			} else {
				inner_table.put(method, 1);
			}
		} else {
			Hashtable<Method, Integer> inner_table = new Hashtable<Method, Integer>();
			inner_table.put(method, 1);
			frequencies.put(tctx, inner_table);
		}
	}
	
	public void print()
	{
		System.out.println(">>> USED METHODS");
		
		for(TypeContext tctx : frequencies.keySet()) {
			System.out.println(tctx);
			Hashtable<Method, Integer> inner_table = frequencies.get(tctx);
			for(Method meth : inner_table.keySet()) {
				System.out.println("\t" + meth.toString() + " " + inner_table.get(meth));
			}
		}
		
		System.out.println(">>> DECLARED METHODS");
		
		for(String className : declaredMethods.keySet()) {
			ArrayList<Method> ls = declaredMethods.get(className);
			
			for(Method meth : ls) {
				System.out.println("\t" + meth.toString());
			}
		}
	}
}
