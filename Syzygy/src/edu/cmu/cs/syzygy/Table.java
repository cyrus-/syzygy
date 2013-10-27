package edu.cmu.cs.syzygy;

import java.util.Hashtable;

/**
 * Table for counting pairs of items of different types.
 * Efficiently stores the counts for each element of the pair.
 * 
 * @author flavioc
 *
 */
public class Table<T1, T2> {
	private Hashtable<T1, Integer> t1data = new Hashtable<T1, Integer>();
	private Hashtable<T2, Integer> t2data = new Hashtable<T2, Integer>();
	
	private Hashtable<Pair<T1, T2>, Integer> all = new Hashtable<Pair<T1, T2>, Integer>();
	
	private int total = 0;
	
	public String toString()
	{
		return all.toString();
	}
	
	public void add(T1 t1, T2 t2)
	{
		// for t1
		Util.htInc(t1data, t1);
		// for t2
		Util.htInc(t2data, t2);
		// for both
		Util.htInc(all, new Pair<T1, T2>(t1, t2));
		
		// increment total
		total++;
	}
	
	public int getTotal()
	{
		return total;
	}
	
	public int getCount1(T1 el)
	{
		return Util.htGetZero(t1data, el);
	}
	
	public int getCount2(T2 el)
	{
		return Util.htGetZero(t2data, el);
	}
	
	public int getCount(T1 t1, T2 t2)
	{
		Pair<T1, T2> p = new Pair<T1, T2>(t1, t2);
		
		return Util.htGetZero(all, p);
		
	}
	
	public Table()
	{
	}
}
