/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.util.Collection;
import java.util.Iterator;

public class Instance {
	String name;
	TokenSequence ts;
	LabelSequence ls;
	int length;

	public Instance(TokenSequence tokenSequence) {
		this(null, tokenSequence, null);
	}

	public Instance(String key, TokenSequence tokenSequence) {
		this(key, tokenSequence, null);
	}

	public Instance(TokenSequence tokenSequence, LabelSequence labelSequence) {
		this(null, tokenSequence, labelSequence);
	}

	public Instance(String key, TokenSequence tokenSequence, LabelSequence labelSequence) {
		name = key;
		length = tokenSequence.length();
		ts = tokenSequence;
		ls = labelSequence;
	}

	public boolean setLabel(LabelSequence labelSequence) {
		if (ls == null) {
			ls = labelSequence;
			return true;
		}
		System.err.println("Labeling already exists!");
		return false;
	}

	public int[] getLabels() {
		return ls.getLabels();
	}

	public int getLabel(int i) {
		return ls.get(i);
	}

	public int[] getUnary(int i) {
		return ts.get(i).ufs.getFeatures();
	}

	public int[] getBinary(int i) {
		return ts.get(i).bfs.getFeatures();
	}

	Alphabet getLabelAlphabet() {
		return ls.getAlphabet();
	}

	public String getText(int i) {
		return ts.get(i).getText();
	}

	public String getName() {
		return name;
	}

	public boolean addUnary(int i, String feature) {
		return ts.get(i).addUnary(feature);
	}

	public boolean addBinary(int i, String feature) {
		return ts.get(i).addBinary(feature);
	}

	public int length() {
		return length;
	}

	public void extract(Alphabet ua, Alphabet ba) {
		for (int i = 0; i < length; i++) {
			Token token = ts.get(i);
			token.setUnaryFeature(extractFeats(ua, token.ud));
			token.setBinaryFeature(extractFeats(ba, token.bd));
		}
	}
	
	FeatureVector dumpFeats(Alphabet alphabet, Collection<String> data) {
		for (String sdata : data) {
			alphabet.add(sdata);
		}
		return new FeatureVector(alphabet, data);
	}

	FeatureVector extractFeats(Alphabet alphabet, Collection<String> data) {
		Iterator<String> iterator = data.iterator();
		while (iterator.hasNext()) {
			String value = iterator.next();
			if (!alphabet.has(value)) {
				iterator.remove();
			}
		}
		return new FeatureVector(alphabet, data);
	}
	
	void count(Counter uc) {
		for (int i = 0; i < length; i++) {
			Token token = ts.get(i);
			countFeats(uc, token.ud);
		}
	}

	void count(Counter uc, Counter bc) {
		for (int i = 0; i < length; i++) {
			Token token = ts.get(i);
			countFeats(uc, token.ud);
			countFeats(bc, token.bd);
		}
	}
	
	void countFeats(Counter c, Collection<String> data) {
		for (String sdata : data) {
			c.add(sdata);
		}
	}
}