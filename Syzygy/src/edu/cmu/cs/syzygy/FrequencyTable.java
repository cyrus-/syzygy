package edu.cmu.cs.syzygy;

import java.util.Hashtable;

public class FrequencyTable<T> {
	private Table<SyntacticContext, String> countTable = new Table<SyntacticContext, String>();
	
	private Hashtable<Pair<SyntacticContext, String>, Integer> numElems
	  = new Hashtable<Pair<SyntacticContext, String>, Integer>();
	
	private Hashtable<Triple<SyntacticContext, String, T>, Integer> frequencies
		= new Hashtable<Triple<SyntacticContext, String, T>, Integer>();
	
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
	
	public int getFreq(SyntacticContext ctx, String type)
	{
		return countTable.getCount(ctx,  type);
	}
	
	public int getFreq(SyntacticContext ctx, String type, T data) {
		return Util.htGetZero(frequencies, new Triple<SyntacticContext, String, T>(ctx, type, data));
	}
	
	public int getCount(SyntacticContext ctx, String type) {
		return numElems.get(new Pair<SyntacticContext, String>(ctx, type));
	}
}
