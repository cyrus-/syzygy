package codewalker.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import visit.LiteralVisitor;
import visit.MethodVisitor;
import visit.VariableVisitor;
import dir.Directory;
import dir.JavaFile;


public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private static LiteralVisitor lit = new LiteralVisitor();
	private static VariableVisitor var = new VariableVisitor();
	private static MethodVisitor methods = new MethodVisitor();

	public SampleAction() {
	}
	
	private static void traverseDir(File dir, IJavaProject prj)
	{	
		Directory currentDir = new Directory();
		File[] files = dir.listFiles();
		
		for (File child : files) {
			if (!child.isDirectory()) {
				continue;
			}
			
			traverseDir(child, prj);
		}
		
		// add child files
		for (File child : files) {
			if (child.isFile() && child.getName().toLowerCase().endsWith(".java")) {
				JavaFile file = new JavaFile(child, prj);
				file.accept(lit);
				file.accept(methods);
				file.accept(var);
			}
		}
	}
	
	public void serialize(String fileName)
	{
		try {
			FileOutputStream fout = new FileOutputStream(fileName);
			
			ObjectOutputStream out = new ObjectOutputStream(fout);
			
			out.writeObject(methods);
			out.writeObject(lit);
			out.writeObject(var);
			
			out.close();
			fout.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deserialize(String fileName)
	{
		try {
			FileInputStream fis = new FileInputStream(fileName);
			ObjectInputStream in = new ObjectInputStream(fis);
			
			methods = (MethodVisitor)in.readObject();
			lit = (LiteralVisitor)in.readObject();
			var = (VariableVisitor)in.readObject();
			
			in.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void run(IAction action)
	{
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject("antlr");
		System.out.println(proj.getLocation());
		IJavaProject jproj = JavaCore.create(proj);
				
		traverseDir(proj.getLocation().toFile(), jproj);

		var.print();
		lit.print();
		methods.print();
		MessageDialog.openInformation(window.getShell(), "CodeWalker",
				"Statistics were collected");
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}