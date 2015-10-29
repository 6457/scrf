import sq.data.Alphabet;
import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;
import sq.data.TokenSequence;
import sq.model.HMM;
import sq.pipe.Text2Unary;

public class TestHMM {
	public static void main(String[] args) {
		TestHMM tCrf = new TestHMM();
		InstanceList list = tCrf.genTrain();
		HMM hmm = new HMM(list);
		for (Instance inst : list) {
			LabelSequence ls = hmm.getLabel(inst);
			for (int i = 0; i < ls.length(); i++) {
				System.out.println(inst.getText(i) + "::" + inst.getLabel(i) + "::" + ls.get(i));
			}
		}
		String[][] test = { { "Is", "today", "Wednesday" }, { "Today", "is", "Sunday" } };
		InstanceList tlist = new InstanceList(list);
		for (int i = 0; i < test.length; i++) {
			tlist.add(new Instance(new TokenSequence(test[i])));
		}
		for (Instance inst : tlist) {
			LabelSequence ls = hmm.getLabel(inst);
			for (int i = 0; i < ls.length(); i++) {
				System.out.println(inst.getText(i) + "::" + ls.get(i));
			}
		}
	}

	InstanceList genTrain() {
		String[][] train = { { "Today", "is", "Wednesday" }, { "Tomorrow", "is", "Sunday" },
				{ "Is", "today", "Sunday" } };
		String[][] label = { { "n", "v", "n" }, { "n", "v", "n" }, { "v", "n", "n" } };
		InstanceList list = new InstanceList();
		list.addPipe(new Text2Unary());
		Alphabet labelAlphabet = list.getLabelAlphabet();
		labelAlphabet.add("n");
		labelAlphabet.add("v");
		for (int i = 0; i < train.length; i++) {
			list.add(new Instance(new TokenSequence(train[i]), new LabelSequence(labelAlphabet, label[i])));
		}
		return list;
	}
}