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

import edu.cmu.cs.syzygy.Trainer;
import edu.cmu.cs.syzygy.TrainingData;

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
		CompilationUnit[] units = new CompilationUnit[ls.size()];
		int i = 0;
		
		for(File file : ls) {
			JavaFile jfile = new JavaFile(file, prj);
			units[i] = jfile.getUnit();
			++i;
		}
		
		return new Trainer(units).train();
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
			TestVisitor visitor = new TestVisitor(data);
			
			for(File test : outls) {
				System.out.println("Testing on " + test);
				JavaFile testFile = new JavaFile(test, prj);
				
				try {
					visitor.setFile(test, new BufferedWriter(new FileWriter(test.getName() + Integer.toString(i))));
				} catch (IOException e) {
					System.out.println("Unable to write to dump file");
				}
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
		
		System.out.println("Accuracy rate: " + results.getAverage());
		final long end = System.currentTimeMillis();
		final long totalTime = end - start;
		System.out.println("=========== EXECUTION TIME: " + totalTime + " milliseconds =============");
	}
}
