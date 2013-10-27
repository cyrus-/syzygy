package edu.cmu.cs.syzygy;

import java.util.Hashtable;

public class FrequencyTable<T> {
	private Table<SyntacticContext, String> countTable = new Table<SyntacticContext, String>();
	
	private Hashtable<Pair<SyntacticContext, String>, Integer> numElems
	  = new Hashtable<Pair<SyntacticContext, String>, Integer>();
	
	private Hashtable<Triple<SyntacticContext, String, T>, Integer> frequencies
		= new Hashtable<Triple<SyntacticContext, String, T>, Integer>();
	
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		for (Entry<Triple<SyntacticContext, String, T>, Integer> e : frequencies.entrySet()) {
			sb.append(e.getKey().toString() + " = " + e.getValue().toString() + "\n");
		}
		return sb.toString();
	}
	
	public void add(SyntacticContext ctx, String typ, T data)
	{
		countTable.add(ctx,  typ);
		
		Triple<SyntacticContext, String, T> t = new Triple<SyntacticContext, String, T>(ctx, typ, data);
		
		if (!frequencies.containsKey(t)) {
			Util.htInc(numElems, new Pair<SyntacticContext, String>(ctx, typ));
		}	
		
		Util.htInc(frequencies, t);
	}
	
	public int getTotal()
	{
		return countTable.getTotal();
	}
	
	public int getFreq(SyntacticContext ctx)
	{
		return countTable.getCount1(ctx);
	}
	
	public int getFreq(String type) 
	{
		return countTable.getCount2(type);
	}
	
	public int getFreq(SyntacticContext ctx, String type)
	{
		return countTable.getCount(ctx,  type);
	}
	
	public int getFreq(String type, T data) {
		int total = 0;
		for (SyntacticContext ctx : SyntacticContext.values()) {
		  total += getFreq(ctx, type, data);
		}
		return total;
	}
	
	public int getFreq(SyntacticContext ctx, String type, T data) {
		return Util.htGetZero(frequencies, new Triple<SyntacticContext, String, T>(ctx, type, data));
	}
	
	public int getCount(SyntacticContext ctx, String type) {
		if (numElems == null) {
			System.out.println("How can this be null");
			System.exit(1);
		}
		if (ctx == null) {
			System.out.println("null ctx");
			System.exit(1);
		}
		if (type == null) {
			System.out.println("null type");
			System.exit(1);
		}
		return Util.htGetZero(numElems, new Pair<SyntacticContext, String>(ctx, type));
	}
}
