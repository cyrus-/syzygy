package dir;

import java.io.File;
import java.io.FileReader;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class JavaFile extends BaseNode {

	private ASTNode compilationUnit;
	
	public JavaFile(File file, IJavaProject prj) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		
		try {
			FileReader reader = new FileReader(file);
			
			int length = (int)file.length();
			int offset = 0;
			int readByte = 0;
			
			char[] source = new char[length];
			while ((readByte = reader.read(source, offset, length)) > 0) {
				offset += readByte;
				length -= readByte;
			}
			
			parser.setUnitName("planet");
			parser.setProject(prj);
			parser.setSource(source);
			
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setResolveBindings(true);
			//parser.setBindingsRecovery(true);
			//parser.setStatementsRecovery(true);
			
			
			compilationUnit = parser.createAST(null);
			
			reader.close();
		} catch(Exception e) {
			
		}
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		compilationUnit.accept(visitor);
	}
}
