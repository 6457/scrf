import sq.data.Alphabet;
import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;
import sq.data.TokenSequence;
import sq.model.CRF;
import sq.pipe.Text2Unary;
import sq.trainer.SGDTrainer;
import sq.trainer.Trainer;

public class TestCRF {
	public static void main(String[] args) {
		TestCRF tCrf = new TestCRF();
		InstanceList list = tCrf.genTrain();
		CRF crf = new CRF(list);
		Trainer trainer = new SGDTrainer(crf);
		trainer.train();
		String[][] test = { { "Is", "today", "Wednesday" }, { "Today", "is", "Sunday" } };
		InstanceList tlist = new InstanceList(crf.getList());
		for (int i = 0; i < test.length; i++) {
			tlist.add(new Instance(new TokenSequence(test[i])));
		}
		tlist.extract();
		for (Instance inst : tlist) {
			LabelSequence ls = crf.new Encoder(inst).viterbi();
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
		list.dump(0, 3);
		return list;
	}
}