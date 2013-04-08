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
	private Hashtable<T1, Integer> t1data;
	private Hashtable<T2, Integer> t2data;
	
	private class pair {
		public T1 t1 = null;
		public T2 t2 = null;
		
		public boolean equals(Object obj)
		{
			if(obj instanceof Table.pair) {
				@SuppressWarnings("unchecked")
				pair other = (pair)obj;
				return t1.equals(other.t1) && t2.equals(other.t2);
			}
			return false;
		}
		
		public int hashCode()
		{
			return t1.hashCode() + t2.hashCode();
		}
		
		pair(T1 _t1, T2 _t2) {
			t1 = _t1;
			t2 = _t2;
		}
	};
	
	private Hashtable<pair, Integer> all;
	
	private int total;
	
	public void add(T1 t1, T2 t2)
	{
		// for t1
		Integer current = t1data.get(t1);
		if(current == null)
			current = 1;
		else
			current++;
		t1data.put(t1,  current);
		
		// for t2
		current = t2data.get(t2);
		if(current == null)
			current = 1;
		else
			current++;
		t2data.put(t2, current);
		
		// for both
		pair p = new pair(t1, t2);
		
		current = all.get(p);
		if(current == null)
			current = 1;
		else
			current++;
		all.put(p, current);
		
		// increment total
		total++;
	}
	
	public int getTotal()
	{
		return total;
	}
	
	public int getCount1(T1 el)
	{
		Integer current = t1data.get(el);
		
		if(current == null)
			return 0;
		else
			return current;
	}
	
	public int getCount2(T2 el)
	{
		Integer current = t2data.get(el);
		
		if(current == null)
			return 0;
		else
			return current;
	}
	
	public int getCount(T1 t1, T2 t2)
	{
		pair p = new pair(t1, t2);
		
		Integer current = all.get(p);
		
		if(current == null)
			return 0;
		else
			return current;
	}
	
	public Table()
	{
		total = 0;
	}
}
