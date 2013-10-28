package edu.cmu.cs.syzygy.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class EclipseAction implements IWorkbenchWindowActionDelegate {
	private static final String[] projects = {"jfreechart", "ant", "antlr", "findbugs", "batik", "axion", "AoIsrc292"};
	
	public void run(IAction action)
	{
		runSequential();
	}
	
	private void runSequential()
	{
		for(String project : projects) {
			(new Test(project)).run();
		}	
	}
	
	private void runParallel()
	{
		ExecutorService es = Executors.newFixedThreadPool(4);
		
		for(String project : projects) {
			es.submit(new Test(project));
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
