package visit;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class VariableCounter extends ASTVisitor {
	
	private int offset;
	private int count = 0;
	private String typ;
	private Set<SimpleName> names = new HashSet<SimpleName>();
	
	public VariableCounter(int o, String t) {
		offset = o;
		typ = t;
		System.out.println("Finding variables of type " + t + " before offset " + o);
	}
	
	private boolean correctType(ASTNode p) {
	  if (p instanceof FieldDeclaration) {
		  return false;
      } else {
		  if (p instanceof VariableDeclarationExpression) {
			  String t = ((VariableDeclarationExpression)p).getType().resolveBinding().getQualifiedName();
			  return (t.equals(typ));
		  } else if (p instanceof VariableDeclarationStatement) {
			  String t = ((VariableDeclarationStatement)p).getType().resolveBinding().getQualifiedName();
			  return (t.equals(typ));
		  } else {
			  System.out.println("shit some other type of variable declaration");
			  return false;
		  }
      }
	}
	
	// This stuff is so that shadowed variables don't get counted twice
	private void addName (SimpleName name) {
		ITypeBinding typ = name.resolveTypeBinding();
		
		if (! names.contains(name)) {
			count++;
			System.out.println("Found variable " + name);
			names.add(name);
	      }		
	}
	
	public boolean visit (SingleVariableDeclaration var) {
		if (var.getStartPosition() < offset && var.getType().equals(typ)) {
			addName(var.getName());
		}
		return false;
	}

	public boolean visit (VariableDeclarationFragment var) {
		if ((var.getStartPosition() < offset) && (correctType (var.getParent()))) {
			addName (var.getName());
		}
		return true;
	}
	
	public int getCount() {
		return count;
	}

}
