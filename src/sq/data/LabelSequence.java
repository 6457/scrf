/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

public class LabelSequence {
	
	protected Alphabet alpha;
	protected int size;
	protected int[] feats;
	
	public LabelSequence(Alphabet alphabet, int[] labinds) {
		alpha = alphabet;
		size = labinds.length;
		feats = labinds;
	}
	
	public LabelSequence(Alphabet alphabet, String[] labels) {
		alpha = alphabet;
		size = labels.length;
		feats = new int[size];
		for (int i = 0; i < size; i++) {
			feats[i] = alpha.look(labels[i]);
		}
	}
	
	public int[] getLabels() {
		return feats;
	}
	
	public Alphabet getAlphabet() {
		return alpha;
	}

	public int get(int pos) {
		return feats[pos];
	}
	
	public int length() {
		return size;
	}
}