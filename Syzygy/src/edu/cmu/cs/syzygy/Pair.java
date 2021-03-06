package edu.cmu.cs.syzygy;

public class Pair<T1, T2> {
	public T1 fst;
	public T2 snd;
	
	public Pair(T1 t1, T2 t2) {
		fst = t1;
		snd = t2;
	}
	
	public boolean equals(Object obj)
	{
		if(obj == null)
			return false;
		
		if(obj instanceof Pair<?, ?>) {
			Pair<?,?> other = (Pair<?, ?>)obj;
			return fst.equals(other.fst) && snd.equals(other.snd);
		}
		return false;
	}
	

	public int hashCode() {
		return (fst.toString() + snd.toString()).hashCode();
	}
	
	public String toString() {
		return "(" + fst.toString() + " , " + snd.toString() + ")";
	}
}
