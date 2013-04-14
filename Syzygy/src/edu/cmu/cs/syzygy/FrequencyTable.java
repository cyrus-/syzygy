package edu.cmu.cs.syzygy;

import java.util.Hashtable;


public class FrequencyTable<T> {
	private Hashtable<SyntacticContext, Integer> ctxCount;
	private Hashtable<Pair<SyntacticContext, String>, Integer> ctxtypeCount;
	
	private Hashtable<Triple<SyntacticContext, String, T>, Integer> frequencies;
	
	int total = 0;
	
	
	public void add(SyntacticContext ctx, String typ, T data)
	{
		Util.htInc(ctxCount, ctx);
		Util.htInc(ctxtypeCount, new Pair<SyntacticContext, String>(ctx, typ));
		Util.htInc(frequencies, new Triple<SyntacticContext, String, T>(ctx, typ, data));
		total++;
	}
	
	public int getTotal()
	{
		return total;
	}
	
	public int getCount(SyntacticContext ctx)
	{
		return Util.htGetZero(ctxCount, ctx);
	}
	
	public int getCount(SyntacticContext ctx, String typ)
	{
		return Util.htGetZero(ctxtypeCount, new Pair<SyntacticContext, String>(ctx, typ));
	}
	
	public int getCount(SyntacticContext ctx, String typ, T data) {
		return Util.htGetZero(frequencies, new Triple<SyntacticContext, String, T>(ctx, typ, data));
	}
}
