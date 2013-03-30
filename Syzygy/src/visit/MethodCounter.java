package visit;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class MethodCounter extends ASTVisitor {
	
	private int offset;
	private MethodVisitor mv;
	private String typ;
	private Set<Method> mSet = new HashSet<Method>();
	
	public MethodCounter(int o, String t, MethodVisitor m) {
		offset = o;
		typ = t;
		mv = m;
		//System.out.println("Finding methods of type " + t + " before offset " + o);
	}
	
	
	public void addMethodsGen (IType tp, boolean pubCheck) {
		ITypeHierarchy h;
		try {
			h = tp.newSupertypeHierarchy(null);
		} catch (JavaModelException e1) {
			return;
		}
		
		for (IType t : h.getAllTypes()) {
		try {
			IMethod[] meths = t.getMethods();
			
			for (IMethod m : meths) {
				int f = m.getFlags();
				boolean test = false;
			
				if (pubCheck) {
					test = Flags.isPublic(f);
				} else {
					test = ((Flags.isPublic(f)) || 
				     (Flags.isProtected(f)) || 
				     ((t.getFullyQualifiedName().equals(tp.getFullyQualifiedName())))); 
			    }
				
				Method mt = new Method(m);
				
				//System.out.println(mt.getReturnType() + " " + typ);
				if (test && (mt.getReturnType().equals(typ))) {
					//System.out.println("method : " + mt.getFullName());
					for (Context.ContextType c : Context.ContextType.values()) {
						mt.setContext(c);
						if (!(mv.hasMethod(mt.getReturnType(), c, mt))) {
							mSet.add(mt);
						}
					}
				}
			}
		} catch (JavaModelException e) {
			//System.out.println("couldn't get methods");
			continue;
		}
		}
	}
	
	public void addMethods (IJavaElement j) {
		if(j == null) {
			//System.out.println("NULL");
			return;
		} else {
			//System.out.println("NOT NULL");
		}
		
		if (j.getElementType() == IJavaElement.TYPE) {
			addMethodsGen(((IType)j), true);
		}
	}
	
	public boolean visit (SingleVariableDeclaration var) {
		if (var.getStartPosition() < offset) {
			ITypeBinding bind = var.getType().resolveBinding();
			
			try {
			if(bind != null)
				addMethods(bind.getJavaElement());
			} catch(NullPointerException e) {
				return false;
			}
		}
		return false;
	}
	
	public boolean visit (FieldDeclaration var) {
		try {
			if (var.getStartPosition() < offset) {
				ITypeBinding bind = var.getType().resolveBinding();
				if(bind != null)
					addMethods(bind.getJavaElement());
			}
			return false;
		} catch(NullPointerException e) {
			return false;
		}
	}
	
	public boolean visit (VariableDeclarationExpression var) {
		try {
			if (var.getStartPosition() < offset) {
				ITypeBinding bind = var.getType().resolveBinding();
				if(bind != null)
					addMethods(bind.getJavaElement());
			}
		} catch(NullPointerException e) {
		}
		return false;
	}
	
	public boolean visit (VariableDeclarationStatement var) {
		try {
			if (var.getStartPosition() < offset) {
				ITypeBinding bind = var.getType().resolveBinding();
				if(bind != null)
					addMethods(bind.getJavaElement());
			}
		} catch(NullPointerException e) {
		}
		return false;
	}
	
	
	public boolean visit (TypeDeclaration dec) {
		
		for (MethodDeclaration m : dec.getMethods()) {
			IMethodBinding mi = m.resolveBinding();
			
			if (mi == null) {
				//System.out.println("Could not get method binding");
				continue;
			}
			
			Method mt;
			try {
				mt = new Method(m, mi);
			} catch (Exception e) {
				continue;
			}
			
			if ((mt.getReturnType().equals(typ))) {
				for (Context.ContextType c : Context.ContextType.values()) {
					mt.setContext(c);
					if (mv.hasMethod(mt.getReturnType(), c, mt)) {
						mSet.add(mt);
					}
				}
			}
		}
		return true;
		/*
		IJavaElement j = dec.resolveBinding().getJavaElement();
		
		if(j == null) {
			System.out.println("can't find class");
			System.out.println("blah: " + dec.resolveBinding().toString());
			return true;
		} else {
			//System.out.println("NOT NULL");
		}
		
		if (j.getElementType() == IJavaElement.TYPE) {
			addMethodsGen((IType) j, false);
		} else if (j.getElementType() == IJavaElement.CLASS_FILE){
			addMethodsGen(((IClassFile) j).getType(), false);
		}
		return true;
		*/
	}

	public int getCount() {
		return mSet.size();
	}
}
