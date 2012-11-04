package codewalker.actions;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.jface.dialogs.MessageDialog;

import visit.LiteralVisitor;
import visit.MethodVisitor;
import visit.VariableVisitor;

import dir.Directory;
import dir.JavaFile;


public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	/**
	 * The constructor.
	 */
	public SampleAction() {
	}
	
	private static Directory traverseDir(File dir, IJavaProject prj)
	{
		Directory currentDir = new Directory();
		File[] files = dir.listFiles();
		
		for (File child : files) {
			if (!child.isDirectory()) {
				continue;
			}
			
			currentDir.addChild(traverseDir(child, prj));
		}
		
		// add child files
		for (File child : files) {
			if (child.isFile() && child.getName().toLowerCase().endsWith(".java")) {
				currentDir.addChild(new JavaFile(child, prj));
			}
		}
		
		return currentDir;
	}

	public void run(IAction action) {
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject("planet");
		System.out.println(proj.getLocation());
		IJavaProject jproj = JavaCore.create(proj);
				
		Directory dir = traverseDir(proj.getLocation().toFile(), jproj);
		
		LiteralVisitor lit = new LiteralVisitor();
		
		dir.accept(lit);
		
		lit.print();
		
		VariableVisitor var = new VariableVisitor();
		dir.accept(var);
		var.print();
		
		MethodVisitor methods = new MethodVisitor();
		dir.accept(methods);
		methods.print();
		
		MessageDialog.openInformation(
			window.getShell(),
			"CodeWalker",
			"Hello, Eclipse world " + jproj.toString());
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}