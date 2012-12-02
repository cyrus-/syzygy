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
import visit.VariableVisitor;
import dir.Directory;
import dir.JavaFile;


public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
	private static LiteralVisitor lit = null;
	private static VariableVisitor var = null;
	private static MethodVisitor methods = null;
	private static Random generator = new Random();
	
	private static final String PROJECT = "planet";

	public SampleAction() {
		
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
	
	private static void initData()
	{
		lit = new LiteralVisitor();
		var = new VariableVisitor();
		methods = new MethodVisitor();
	}
	
	private static void trainWithList(IJavaProject prj, LinkedList<File> ls)
	{
		initData();
		for(File file : ls) {
			JavaFile jfile = new JavaFile(file, prj);
			jfile.accept(lit);
			jfile.accept(methods);
			jfile.accept(var);
		}
	}
	
	private static LinkedList<File> pick10percFiles(LinkedList<File> ls, int howmany)
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
	
	private static double trainLeave10percOut(IJavaProject prj, LinkedList<File> ls) throws IOException
	{
		final int size = ls.size();
		double total = 0.0;
		int tenperc = max(1, size / 10);
		
		for(int i = 0; i < 10; ++i) {
			LinkedList<File> outls = pick10percFiles(ls, tenperc);
			
			trainWithList(prj, ls);
			
			//print();
			
			FileWriter output_file = null;
			BufferedWriter output_file_buffer = null;
			
			output_file = new FileWriter("data.tokens" + i);
			output_file_buffer = new BufferedWriter(output_file);
			
			output_file_buffer.write("" + tenperc);
			output_file_buffer.newLine();
			for(File test : outls) {
				output_file_buffer.write(test.toString());
				output_file_buffer.newLine();
			}
			output_file_buffer.flush();
			
			Predictor pred = new Predictor(lit, var, methods, output_file_buffer);
			
			double thistotal = 0.0;
			
			for(File test : outls) {
				double thisfile = pred.test(new JavaFile(test, prj), test);
				thistotal += thisfile;
				
				System.out.println(test.getName() + " got " + thisfile);
			}
			
			output_file_buffer.close();
			
			total += thistotal / (double)outls.size();
			
			ls.addAll(outls);
			
			assert(ls.size() == size);
		}
		
		return total / (double)10;
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
	
	public static void print()
	{
		var.print();
		lit.print();
		methods.print();
	}

	public void run(IAction action)
	{
		IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT);
		
		IJavaProject jproj = JavaCore.create(proj);
		LinkedList<File> allFiles = new LinkedList<File>();
		File projectDir = proj.getLocation().toFile();
		
		getAllFiles(projectDir, allFiles);
		
		//trainWithList(jproj, allFiles);
		
		double acc;
		try {
			acc = trainLeave10percOut(jproj, allFiles);
			System.out.println("====> Success rate " + acc);

			MessageDialog.openInformation(window.getShell(), "CodeWalker",
					"Statistics were collected");
		} catch (IOException e) {
			MessageDialog.openInformation(window.getShell(), "CodeWalker", "Failed to collect statistics");
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}


	public void dispose() {
	}


	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}