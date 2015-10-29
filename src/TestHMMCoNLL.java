import java.io.IOException;

import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;
import sq.model.HMM;
import sq.pipe.TextSplit2Unary;
import sq.utils.Tools;

public class TestHMMCoNLL {
	public static void main(String[] args) throws IOException {
		InstanceList list = new InstanceList();
		list.addPipe(new TextSplit2Unary());
		list.readCoNLL(Tools.readFile("dat/test.txt"), 0);
		System.out.println(list.size());
		HMM hmm = new HMM(list);
		for (Instance inst : list) {
			LabelSequence ls = hmm.getLabel(inst);
			for (int i = 0; i < ls.length(); i++) {
				System.out.println(ls.get(i) + "::" + inst.getLabel(i));
			}
		}
	}
}