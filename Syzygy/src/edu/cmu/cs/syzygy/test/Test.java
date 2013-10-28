package edu.cmu.cs.syzygy.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.CompilationUnit;

import edu.cmu.cs.syzygy.Debug;
import edu.cmu.cs.syzygy.ResolveBindingException;
import edu.cmu.cs.syzygy.Trainer;
import edu.cmu.cs.syzygy.TrainingData;
import edu.cmu.cs.syzygy.TrainingVisitor;

public class Test implements Runnable {
	private Random generator = new Random();
	private String project = null;
	
	public Test (String project_name) {
		project = project_name;
	}

	private static void getAllFiles(File dir, LinkedList<File> ls)
	{
		File[] files = dir.listFiles();
		
		for (File child : files) {
			if (!child.isDirectory()) {
				continue;
			}
			
			getAllFiles(child, ls);
		}
		
		// add child files
		for (File child : files) {
			if (child.isFile() && child.getName().toLowerCase().endsWith(".java")) {
				ls.add(child);
			}
		}
	}
	
	private TrainingData trainWithList(IJavaProject prj, LinkedList<File> ls)
	{
		TrainingVisitor visitor = new TrainingVisitor();
		
		for(File file : ls) {
			JavaFile jfile = new JavaFile(file, prj);
			try {
			  jfile.accept(visitor);
			} catch (ResolveBindingException e) {
				System.out.println("Could not resolve binding while training: " + e.toString());
			}
		}
		
		return visitor.data;
	}
	
	private LinkedList<File> pickFiles(LinkedList<File> ls, int howmany)
	{
		LinkedList<File> ret = new LinkedList<File>();
		
		for(int i = 0; i < howmany; ++i) {
			int size = ls.size();
			int index = generator.nextInt(size);
			ret.add(ls.get(index));
			ls.remove(index);
		}
		
		return ret;
	}
	
	private ResultTable trainLeaveFractionOut(IJavaProject prj, LinkedList<File> ls, double frac, final int numIterations)
	{
		final int size = ls.size();
		final int numTestFiles = max(1, (int)(size * frac));
		ResultTable results = new ResultTable();
		
		System.out.println("Total files " + ls.size());
		
		for(int i = 0; i < numIterations; ++i) {
			LinkedList<File> outls = pickFiles(ls, numTestFiles);
			System.out.println("=========== ITERATION " + project + " " + i + " ===========");
			
			TrainingData data = trainWithList(prj, ls);
			
			Debug.print(Debug.Mode.ENUMLITERALS, data.enumLiterals.toString());
			
			Debug.print(Debug.Mode.INFO, data.toString());
			
			BufferedWriter dumpfile = null;
			try {
			   dumpfile = new BufferedWriter(new FileWriter(project + ".tokens" + Integer.toString(i)));
			   dumpfile.write("" + numTestFiles + "\n");
				for(File test : outls) {
					dumpfile.write(test.getAbsolutePath().substring(ResourcesPlugin.getWorkspace().getRoot().getProject(project).getLocation().toFile().getAbsolutePath().length()).substring(1));
					dumpfile.write("\n");
				}
			} catch (IOException e) {
			  System.out.println("Unable to write to dump file");
			  continue;
			}

			
			TestVisitor visitor = new TestVisitor(data);
			
			for(File test : outls) {
				//System.out.println("Testing on " + test);
				JavaFile testFile = new JavaFile(test, prj);
				
				
			    visitor.setFile(test, dumpfile);
				
				visitor.setUnit(testFile.getUnit());
				
				testFile.accept(visitor);
			}
			
			// put files back
			ls.addAll(outls);
			results.merge(visitor.getResults());
		}
		
		return results;
	}
	
	private static int max(int i, int j) {
		if(i > j)
			return i;
		else
			return j;
	}
	
	public void run()
	{
		final long start = System.currentTimeMillis();
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		
		IJavaProject jproj = JavaCore.create(proj);
		LinkedList<File> allFiles = new LinkedList<File>();
		File dir = proj.getLocation().toFile();
		
		getAllFiles(dir, allFiles);
		trainWithList(jproj, allFiles);
		ResultTable results = trainLeaveFractionOut(jproj, allFiles, 0.1, 10);
		
		System.out.println("Accuracy rate (" + project + "): " + results.getAverage() * 100);
		
		int[] h = results.getHistogram();
		
		for (int i = 0; i <= 10 ; i++) {
			System.out.println("Upto: " + i + " = " + h[i]);
		}
		
		final long end = System.currentTimeMillis();
		final long totalTime = end - start;
		System.out.println("=========== EXECUTION TIME: " + totalTime + " milliseconds =============");
	}
}
