package codewalker.actions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Random;

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
import visit.Predictor;
import visit.Tracer;
import visit.VariableVisitor;
import dir.JavaFile;


public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	
	public SampleAction() {
		
	}
	
	
	

	public void run(IAction action)
	{
		RunTests r1 = new RunTests("ant");
		RunTests r2 = new RunTests("jfreechart");
		r1.start();
		r2.start();
		try {
			r2.join();
			r1.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}
