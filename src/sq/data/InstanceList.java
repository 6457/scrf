/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import sq.pipe.Pipe;

public class InstanceList extends ArrayList<Instance> {
	private static final long serialVersionUID = 1L;
	private Alphabet labelAlpha;
	private Alphabet ufa;
	private Alphabet bfa;
	private Collection<Pipe> pipes;

	public InstanceList() {
		labelAlpha = new Alphabet();
		ufa = new Alphabet();
		bfa = new Alphabet();
		bfa.add("-B-");
		pipes = new LinkedList<>();
	}

	public InstanceList(Alphabet label, Alphabet unaryFeatures, Alphabet binaryFeatures, Collection<Pipe> collection) {
		labelAlpha = label;
		ufa = unaryFeatures;
		bfa = binaryFeatures;
		pipes = collection;
	}

	public InstanceList(Alphabet label) {
		labelAlpha = label;
		ufa = new Alphabet();
		bfa = new Alphabet();
		bfa.add("-B-");
		pipes = new LinkedList<>();
	}

	public InstanceList(Alphabet binaryFeatures, Collection<Pipe> pipeCollection) {
		labelAlpha = new Alphabet();
		ufa = new Alphabet();
		bfa = binaryFeatures;
		pipes = pipeCollection;
	}

	public InstanceList(InstanceList list) {
		labelAlpha = list.labelAlpha;
		ufa = list.ufa;
		bfa = list.bfa;
		pipes = list.getPipes();
	}

	public boolean addPipe(Pipe pipe) {
		if (size() != 0) {
			System.err.println("Cannot add pipe after creation!");
			return false;
		} else {
			return pipes.add(pipe);
		}
	}

	public boolean add(Instance inst) {
		if (pipes.size() == 0) {
			System.err.println("No pipe availble, thus no feature added!");
			return false;
		}
		for (Pipe pipe : pipes) {
			pipe.proc(inst);
		}
		return super.add(inst);
	}
	
	public void dump(int dumpsize, HashSet<String> dict) {
		dump(dumpsize, Integer.MAX_VALUE, dict);
	}

	public void dump(int ushrink, int bshrink, HashSet<String> dict) {
		Counter uc = new Counter();
		Counter bc = new Counter();
		for (Instance inst : this) {
			inst.count(uc, bc);
		}
		for (String item : uc.keySet()) {
			if (uc.get(item) >= ushrink || dict.contains(item)) {
				ufa.add(item);
			}
		}
		for (String item : bc.keySet()) {
			if (bc.get(item) >= bshrink || dict.contains(item)) {
				bfa.add(item);
			}
		}
		for (Instance inst : this) {
			inst.extract(ufa, bfa);
		}
	}

	public void dump(int ushrink, int bshrink) {
		Counter uc = new Counter();
		Counter bc = new Counter();
		for (Instance inst : this) {
			inst.count(uc, bc);
		}
		for (String item : uc.keySet()) {
			if (uc.get(item) >= ushrink) {
				ufa.add(item);
			}
		}
		for (String item : bc.keySet()) {
			if (bc.get(item) >= bshrink) {
				bfa.add(item);
			}
		}
		for (Instance inst : this) {
			inst.extract(ufa, bfa);
		}
	}

	public void extract() {
		for (Instance inst : this) {
			inst.extract(ufa, bfa);
		}
	}

	public void readCoNLL(String file, int dumpsize) {
		String[] inStrings = file.split("\n\n");
		for (int i = 0; i < inStrings.length; i++) {
			String[] lines = inStrings[i].split("\n");
			String[] tokens = new String[lines.length];
			String[] labs = new String[lines.length];
			for (int j = 0; j < lines.length; j++) {
				String[] values = lines[j].split(" ");
				if (values.length != 3) {
					System.err.println("Format Error!");
				}
				tokens[j] = values[0] + " " + values[1];
				labs[j] = values[2];
				labelAlpha.add(labs[j]);
			}
			add(new Instance(new TokenSequence(tokens), new LabelSequence(labelAlpha, labs)));
		}
		if (ufa.size() != 0 || dumpsize < 0) {
			extract();
		} else {
			dump(dumpsize, dumpsize);
		}
	}

	public Instance proc(Instance inst) {
		for (Pipe pipe : pipes) {
			pipe.proc(inst);
		}
		inst.extract(ufa, bfa);
		return inst;
	}

	public void printFeatures() {
		Alphabet ua = getUnaryAlphabet();
		Alphabet ba = getBinaryAlphabet();
		for (int i = 0; i < size(); i++) {
			Instance inst = get(i);
			for (int j = 0; j < inst.length(); j++) {
				System.out.println(inst.getText(j));
				int[] uf = inst.getUnary(j);
				for (int k = 0; k < uf.length; k++) {
					System.out.print("u:" + ua.get(uf[k]) + " ");
				}
				int[] bf = inst.getBinary(j);
				for (int k = 0; k < bf.length; k++) {
					System.out.println("b:" + ba.get(bf[k]) + " ");
				}
				System.out.println();
			}
		}
	}

	public Alphabet getLabelAlphabet() {
		return labelAlpha;
	}

	public Alphabet getUnaryAlphabet() {
		return ufa;
	}

	public Alphabet getBinaryAlphabet() {
		return bfa;
	}

	public int lookLabel(String labelString) {
		return labelAlpha.look(labelString);
	}

	public int getUnarySize() {
		return ufa.size();
	}

	public int getBinarySize() {
		return bfa.size();
	}

	public int getStateNumber() {
		return labelAlpha.size();
	}

	public Collection<Pipe> getPipes() {
		return pipes;
	}
}