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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	
	public SampleAction() {
		
	}
	
	private static final String[] projects = {"planet"};
	
	
	private String[] rest (int i) {
		String[] result = new String[projects.length - 1];
		for (int j = 0; j < projects.length; j++) {
			if (i == j) continue;
			result[i] = projects[i];
		}
		return result;
	}
	

	public void run(IAction action)
	{
		new RunTests(projects[0], rest(0)).run();
		
		/*
		ExecutorService es = Executors.newFixedThreadPool(4);
		
		for (int i = 0; i < projects.length; i++) {
			es.submit(new RunTests(projects[i], rest(i)));
		}
		
		es.shutdown();*/
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
