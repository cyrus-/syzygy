package visit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
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
	
	// temporary
	private List<IBinding> member_bindings = null;
	private String thisClassName = null;
	
	/*
	public double getProb (TypeContext t, Expression exp) {
		Method m = toMethod(exp);
		
		int total = 0;
		for (Entry<Method, Integer> e : frequencies.get(t).entrySet()) {
			total += e.getValue();
		}
		
		return (frequencies.get(t).get(m)/total); 
	}
	*/
	
	private Method findMethod(IMethodBinding meth)
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
			
			Method newm = new Method(meth);
			ls.add(newm);
			
			return newm;
		} else {
			Method newm = new Method(meth);
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

	public boolean visit(QualifiedName qn)
	{
		IBinding bind = qn.resolveBinding();
		
		//System.out.println("qualified " + qn);
		if(bind != null && isMemberBinding(bind)) {
			//System.out.println("Member " + qn);
		} else {
			ITypeBinding typ = qn.resolveTypeBinding();
			if(typ == null) {
				return false;
			}
			ITypeBinding cls = typ.getDeclaringClass();
			
			if(cls != null && cls.isClass()) {
				//System.out.println(qn.getName() + " It is a class " + cls.getName());
			} else {
				//System.out.println("cls " + cls.getName());
			}
		}
		
		return false;
	}
	
	public boolean visit(SimpleName name)
	{
		IBinding bind = name.resolveBinding();
		ITypeBinding typ = name.resolveTypeBinding();
		
		if(bind != null && !name.isDeclaration() && isMemberBinding(bind)) {
			TypeContext tctx = new TypeContext(typ.getQualifiedName(), name);
			assert(thisClassName != null);
			Method method = new Method(thisClassName, typ.getQualifiedName(), name.getFullyQualifiedName());
			
			addMethod(tctx, method);
		}
		
		return false;
	}
	
	public boolean visit(MethodInvocation mi)
	{
		ITypeBinding bind = mi.resolveTypeBinding();
		if(bind == null)
			return true;
		
		IMethodBinding meth = mi.resolveMethodBinding();
		
		if(meth == null)
			return true;
		
		TypeContext tctx = new TypeContext(bind.getQualifiedName(), mi);
		Method method = findMethod(meth);
		
		if(method == null)
			return true;
		
		addMethod(tctx, method);
		
		return true;
	}
	
	public void addMethod(TypeContext tctx, Method method)
	{
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
		for(TypeContext tctx : frequencies.keySet()) {
			System.out.println(tctx);
			Hashtable<Method, Integer> inner_table = frequencies.get(tctx);
			for(Method meth : inner_table.keySet()) {
				System.out.println("\t" + meth.toString() + " " + inner_table.get(meth));
			}
		}
	}
}
