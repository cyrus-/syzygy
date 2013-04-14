package edu.cmu.cs.syzygy.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class EclipseAction implements IWorkbenchWindowActionDelegate {
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
		
		ExecutorService es = Executors.newFixedThreadPool(4);
		
		for (int i = 0; i < projects.length; i++) {
			es.submit(new Test(projects[i], rest(i)));
		}
		
		es.shutdown();
	}

	public void selectionChanged(IAction action, ISelection selection)
	{
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
	}
}
