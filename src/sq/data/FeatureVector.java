/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.util.Collection;
import java.util.Iterator;

public class FeatureVector {
	protected Alphabet alpha;
	protected int size;
	protected int[] feats;
	
	public FeatureVector(Alphabet alphabet, int[] features) {
		alpha = alphabet;
		size = features.length;
		feats = features;
	}
	
	public FeatureVector(Alphabet alphabet, Collection<String> featlist) {
		alpha = alphabet;
		size = featlist.size();
		feats = new int[size];
		Iterator<String> iter = featlist.iterator();
		for (int i = 0; i < size; i++) {
			feats[i] = alpha.look(iter.next());
		}
	}
	
	public int[] getFeatures() {
		return feats;
	}
	
	public Alphabet getAlphabet() {
		return alpha;
	}
}