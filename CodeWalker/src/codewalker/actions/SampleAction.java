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

	public void run(IAction action)
	{
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject("planet");
		System.out.println(proj.getLocation());
		IJavaProject jproj = JavaCore.create(proj);
				
		Directory dir = traverseDir(proj.getLocation().toFile(), jproj);
		
		LiteralVisitor lit = new LiteralVisitor();
		VariableVisitor var = new VariableVisitor();
		MethodVisitor methods = new MethodVisitor();
		
		dir.accept(lit);
		dir.accept(methods);
		dir.accept(var);
		
		// serialize everything into a file
		try {
			FileOutputStream fout = new FileOutputStream("collected.data");
			
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
		
		// deserialize objects from file
		 try {
			FileInputStream fis = new FileInputStream("collected.data");
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