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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class MethodCounter extends ASTVisitor {
	
	private int offset;
	private String typ;
	private Set<String> names = new HashSet<String>();
	
	public MethodCounter(int o, String t) {
		offset = o;
		typ = t;
		System.out.println("Finding methods of type " + t + " before offset " + o);
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
				
				if (test && (m.getReturnType().equals(typ))) {
					StringBuilder fullname = new StringBuilder();
					fullname.append(m.getDeclaringType().getFullyQualifiedName());
					fullname.append(".");
					fullname.append(m.getElementName());
					names.add(fullname.toString());
				}
			}
		} catch (JavaModelException e) {
			System.out.println("couldn't get methods");
			continue;
		}
		}
	}
	
	public void addMethods (IJavaElement j) {
		if (j.getElementType() == IJavaElement.TYPE) {
			addMethodsGen(((IType)j), true);
		}
	}
	
	public boolean visit (SingleVariableDeclaration var) {
		if (var.getStartPosition() < offset) {
			addMethods(var.getType().resolveBinding().getJavaElement());
		}
		return false;
	}
	
	public boolean visit (FieldDeclaration var) {
		if (var.getStartPosition() < offset) {
			addMethods(var.getType().resolveBinding().getJavaElement());
		}
		return false;
	}
	
	public boolean visit (VariableDeclarationExpression var) {
		if (var.getStartPosition() < offset) {
			addMethods(var.getType().resolveBinding().getJavaElement());
		}
		return false;
	}
	
	public boolean visit (VariableDeclarationStatement var) {
		if (var.getStartPosition() < offset) {
			addMethods(var.getType().resolveBinding().getJavaElement());
		}
		return false;
	}
	
	
	public boolean visit (TypeDeclaration dec) {
		IJavaElement j = dec.resolveBinding().getJavaElement();
		
		if (j.getElementType() == IJavaElement.TYPE) {
			addMethodsGen((IType) j, false);
		} else if (j.getElementType() == IJavaElement.CLASS_FILE){
			addMethodsGen(((IClassFile) j).getType(), false);
		}
		return true;
	}
		
	public int getCount() {
		return names.size();
	}
}
