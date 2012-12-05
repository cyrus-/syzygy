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
import org.eclipse.jface.dialogs.MessageDialog;

import dir.JavaFile;

import visit.LiteralVisitor;
import visit.MethodVisitor;
import visit.Predictor;
import visit.Tracer;
import visit.VariableVisitor;

public class RunTests extends Thread {
	private LiteralVisitor lit = null;
	private VariableVisitor var = null;
	private MethodVisitor methods = null;
	private Random generator = new Random();
	private int RATIO = 10;
	private int ITERATIONS = 10;
	private boolean SHOW_TRAINING = false;
	private File PROJECT_DIR = null;
	
	
	private String project = "";
	
	private static final String PROJECT2 = "antlr";
	
	public RunTests (String p) {
		project = p;
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
	
	private void initData()
	{
		lit = new LiteralVisitor();
		var = new VariableVisitor();
		methods = new MethodVisitor();
	}
	
	private void trainWithList(IJavaProject prj, LinkedList<File> ls)
	{
		initData();
		for(File file : ls) {
			if(SHOW_TRAINING)
				System.out.println("Training on " + file.getName());
			JavaFile jfile = new JavaFile(file, prj);
			jfile.accept(lit);
			jfile.accept(methods);
			jfile.accept(var);
		}
	}
	
	private LinkedList<File> pick10percFiles(LinkedList<File> ls, int howmany)
	{
		LinkedList<File> ret = new LinkedList<File>();
		
		/*
		for(int i = 0; i < howmany; ++i) {
			int index = 0;
			ret.add(ls.get(index));
			ls.remove(index);
		}*/
		
		for(int i = 0; i < howmany; ++i) {
			int size = ls.size();
			int index = generator.nextInt(size);
			ret.add(ls.get(index));
			ls.remove(index);
		}
		
		return ret;
	}
	
	private double trainLeave10percOut(IJavaProject prj, LinkedList<File> ls) throws IOException
	{
		final int size = ls.size();
		double total = 0.0;
		double nonzerototal = 0.0;
		double cheat = 0;
		int tenperc = max(1, size / RATIO);
		
		for(int i = 0; i < ITERATIONS; ++i) {
			LinkedList<File> outls = pick10percFiles(ls, tenperc);
			System.out.println("=========== ITERATION " + project + i + " ===========");
			
			trainWithList(prj, ls);
			
			//print();
			
			FileWriter output_file = null;
			BufferedWriter output_file_buffer = null;
			
			//output_file = new FileWriter("data.tokens" + i);
			output_file = new FileWriter("/dev/null");
			output_file_buffer = new BufferedWriter(output_file);
			
			FileWriter output_file2 = null;
			BufferedWriter output_file_buffer2 = null;
			
			//output_file2 = new FileWriter("data.stats" + i);
			output_file2 = new FileWriter("/dev/null");
			output_file_buffer2 = new BufferedWriter(output_file2);
			
			output_file_buffer.write("" + tenperc);
			output_file_buffer.newLine();
			for(File test : outls) {
				output_file_buffer.write(test.getAbsolutePath().substring(PROJECT_DIR.getAbsolutePath().length()).substring(1) );
				output_file_buffer.newLine();
			}
			output_file_buffer.flush();
			
			Predictor pred = new Predictor(lit, var, methods, output_file_buffer, output_file_buffer2);
			
			double thistotal = 0.0;
			double thisnonzerototal = 0.0;
			double thischeat = 0;
			
			System.out.println("======> Need to test with " + outls.size() + " files");
			int file_cur = 0;
			for(File test : outls) {
				file_cur++;
				
				double thisfile = pred.test(new JavaFile(test, prj), test);
				thistotal += thisfile;
				thisnonzerototal += pred.get_nonzero_test();
				thischeat += pred.get_nonzerototal_test();

				System.out.println("==> " + file_cur + "/" + outls.size() + " " + test.getName() + " got " + thisfile);
			}
			
			output_file_buffer.close();
			
			total += thistotal / (double)outls.size();
			nonzerototal += thisnonzerototal / (double)outls.size();
			cheat += thischeat/ (double)outls.size();
			
			ls.addAll(outls);
			
			assert(ls.size() == size);
		}
		
		System.out.println("====> Non Zero Success Rate: " + nonzerototal / (double)ITERATIONS);
		System.out.println("====> Cheat Success Rate: " + cheat / (double)ITERATIONS);
		
		return total / (double)ITERATIONS;
	}
	
	private static int max(int i, int j) {
		if(i > j)
			return i;
		else
			return j;
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
	
	
	public void print()
	{
		var.print();
		lit.print();
		methods.print();
	}
	
	
	
	
	public double crossProject(IJavaProject prj, LinkedList<File> ls) throws IOException {
		trainWithList(prj, ls);
		
		FileWriter output_file = null;
		BufferedWriter output_file_buffer = null;
		
		 
		output_file = new FileWriter("/dev/null");
		output_file_buffer = new BufferedWriter(output_file);
		
		FileWriter output_file2 = null;
		BufferedWriter output_file_buffer2 = null;
		
		
		output_file2 = new FileWriter("/dev/null");
		output_file_buffer2 = new BufferedWriter(output_file2);
		
		
		Predictor pred = new Predictor(lit, var, methods, output_file_buffer, output_file_buffer2);
		
        IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT2);
		
		IJavaProject jproj = JavaCore.create(proj);
		LinkedList<File> allFiles = new LinkedList<File>();
		PROJECT_DIR = proj.getLocation().toFile();
		
		getAllFiles(PROJECT_DIR, allFiles);
		
		
		double thistotal = 0.0;
		double thisnonzerototal = 0.0;
		double thischeat = 0;
		
		System.out.println("======> Need to test with " + ls.size() + " files");
		int file_cur = 0;
		for(File test : allFiles) {
			file_cur++;
			
			double thisfile = pred.test(new JavaFile(test, jproj), test);
			thistotal += thisfile;
			thisnonzerototal += pred.get_nonzero_test();
			thischeat += pred.get_nonzerototal_test();

			System.out.println("==> " + file_cur + "/" + allFiles.size() + " " + test.getName() + " got " + thisfile);
		}
		
		thistotal = thistotal / (double)allFiles.size();
		thisnonzerototal = thisnonzerototal / (double)allFiles.size();
		thischeat = thischeat / (double)allFiles.size();
		
		System.out.println("====> Non Zero Success Rate: " + thisnonzerototal);
		System.out.println("====> Cheat Success Rate: " + thischeat);
		
		return thistotal;
	}
	
	public void run(){
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		
		IJavaProject jproj = JavaCore.create(proj);
		LinkedList<File> allFiles = new LinkedList<File>();
		PROJECT_DIR = proj.getLocation().toFile();
		
		getAllFiles(PROJECT_DIR, allFiles);
		
		
		double acc;
		try {
			acc = trainLeave10percOut(jproj, allFiles);
			//acc = crossProject(jproj, allFiles);
			
			System.out.println("====> Success rate " + acc);
			System.out.println("Total Predictions: " + Tracer.numPredTotal);
			System.out.println("0.0 Predictions: " + Tracer.numPredZero);
			System.out.println("Positive Predictions in Methods: " + Tracer.numMethodsPositive);
			System.out.println("Negative Predictions in Methods: " + Tracer.numMethodsMinus1);
		} catch (IOException e) {
			System.err.println("Failed to collect statistics: " + e.getMessage());
		}
	}
}
