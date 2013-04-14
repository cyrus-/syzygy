package edu.cmu.cs.syzygy.lm;

import com.aliasi.lm.LanguageModel;
import com.aliasi.lm.NGramBoundaryLM;

/**
 * Estimates character language models dynamically. Call <code>increment</code>
 * to update the model using the observed <code>String</code>. The class will 
 * automatically break the string up into ngrams of size <code>order</code>.
 * 
 * At any time, you can call <code>lnProb</code> to get the log (base e)
 * probability of the <code>String</code>.
 * 
 * @author pschulam
 *
 */
public class NGram {
    
    private int order;
    private LanguageModel.Dynamic lm = null;
    
    public NGram(int order) {
    	this.order = order;
		this.lm = new NGramBoundaryLM(order);
    }
    
    public int order() {
    	return this.order;
    }
    
    public void increment(String s) {
    	lm.train(s);
    }
    
    public double lnProb(String s) {
    	double log2Prob = lm.log2Estimate(s);
    	return log2Prob / Math.log(2.0);
    }
}
