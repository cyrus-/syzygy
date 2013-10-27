package edu.cmu.cs.syzygy;

public class Triple<T1, T2, T3> {
	public T1 fst;
	public T2 snd;
	public T3 third;
	
	public Triple(T1 t1, T2 t2, T3 t3) {
		fst = t1;
		snd = t2;
		third = t3;
	}
	
	public int hashCode() {
		return (fst.toString() + snd.toString() + third.toString()).hashCode();
	}
	
	public boolean equals(Object obj)
	{
		if(obj instanceof Triple<?, ?, ?>) {
			Triple<?,?,?> other = (Triple<?, ?, ?>)obj;
			return fst.equals(other.fst) && snd.equals(other.snd) && third.equals(other.third);
		}
		return false;
	}
	
	public String toString() {
		return "(" + fst.toString() + ", " + snd.toString() + ", " + third.toString() + ")";
	}
}
