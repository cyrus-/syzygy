package edu.cmu.cs.syzygy;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

public class TrainingVisitor extends ASTVisitor {
	public TrainingData data;
	
	public boolean visit(NumberLiteral lit)
	{
		System.out.println(lit + " " + lit.resolveTypeBinding());
		
		if(Util.isInt(lit)) {
			data.intData.increment(lit.getToken());
		} else if(Util.isFloat(lit) || Util.isDouble(lit)) {
			data.floatingData.increment(lit.getToken());
		}
		
		String type = lit.resolveTypeBinding().getQualifiedName();
		
		data.literals.add(Util.findContext(lit), type);
		
		return false;
	}
	
	public boolean visit(StringLiteral str)
	{
		data.stringData.increment(str.getLiteralValue());
		data.literals.add(Util.findContext(str), str.resolveTypeBinding().getQualifiedName());
		return false;
	}
	
	public boolean visit(CharacterLiteral chr)
	{
		data.charData.increment(Character.toString(chr.charValue()));
		data.literals.add(Util.findContext(chr), chr.resolveTypeBinding().getQualifiedName());
		return false;
	}
	
	public TrainingVisitor() {
		data = new TrainingData();
	}
}