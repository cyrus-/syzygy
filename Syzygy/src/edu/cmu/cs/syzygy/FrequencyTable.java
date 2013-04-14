package edu.cmu.cs.syzygy;

import java.util.Hashtable;

public class FrequencyTable<T> {
	private Table<SyntacticContext, String> countTable = new Table<SyntacticContext, String>();
	
	private Hashtable<Triple<SyntacticContext, String, T>, Integer> frequencies
		= new Hashtable<Triple<SyntacticContext, String, T>, Integer>();
	
	public void add(SyntacticContext ctx, String typ, T data)
	{
		countTable.add(ctx,  typ);
		Util.htInc(frequencies, new Triple<SyntacticContext, String, T>(ctx, typ, data));
	}
	
	public int getTotal()
	{
		return countTable.getTotal();
	}
	
	public int getCount(SyntacticContext ctx)
	{
		return countTable.getCount1(ctx);
	}
	
	public int getCount(SyntacticContext ctx, String typ)
	{
		return countTable.getCount(ctx,  typ);
	}
	
	public int getCount(SyntacticContext ctx, String typ, T data) {
		return Util.htGetZero(frequencies, new Triple<SyntacticContext, String, T>(ctx, typ, data));
	}
}
