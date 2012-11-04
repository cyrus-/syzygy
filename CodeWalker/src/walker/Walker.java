package walker;
import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import visit.LiteralVisitor;

import dir.Directory;
import dir.JavaFile;


public class Walker {
	
	private static IJavaProject project = null;
	
	private static Directory traverseDir(File dir)
	{
		Directory currentDir = new Directory();
		File[] files = dir.listFiles();
		
		for (File child : files) {
			if (!child.isDirectory()) {
				continue;
			}
			
			currentDir.addChild(traverseDir(child));
		}
		
		// add child files
		for (File child : files) {
			if (child.isFile() && child.getName().toLowerCase().endsWith(".java")) {
				currentDir.addChild(new JavaFile(child, null));
			}
		}
		
		return currentDir;
	}

	public static void main(String[] args)
	{
		String projectName = args[0];
		
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		System.out.println(proj.getLocation());
		JavaCore.create(proj);
		
		//Directory dir = traverseDir(new File(projectName));
		
		//LiteralVisitor lit = new LiteralVisitor();
		
		//dir.accept(lit);
		
		//lit.print();
	}
}
