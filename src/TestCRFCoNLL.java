import java.io.File;
import java.io.IOException;

import sq.data.InstanceList;
import sq.model.CRF;
import sq.pipe.TextSplit2Unary;
import sq.trainer.LBFGSPTrainer;
import sq.trainer.LBFGSTrainer;
import sq.trainer.Options;
import sq.trainer.SGDTrainer;
import sq.trainer.Trainer;
import sq.utils.Timer;
import sq.utils.Tools;

public class TestCRFCoNLL {
	public static void main(String[] args) throws IOException {
		Options opt = new Options();
		if (opt.parseCommand(args) != null) {
			System.out.println(opt);
		} else {
			return;
		}
		InstanceList list;

		// list.addPipe(new TextSplit2Binary());
		// list.addPipe(new Prev2Unary(1));
		// list.addPipe(new Next2Unary(1));
		
		boolean trainmod = true;
		CRF crf;
		if (trainmod) {
			list = new InstanceList();
			list.addPipe(new TextSplit2Unary());
			list.readCoNLL(Tools.readFile("dat/test.txt"), opt.getDump());
			InstanceList tList = new InstanceList(list);
			tList.readCoNLL(Tools.readFile("dat/test.txt"), -1);
			System.out.println("Number of instances: \t" + list.size());
			System.out.println("Number of states: \t" + list.getStateNumber());
			System.out.println("Number of unary features: \t" + list.getUnarySize());
			System.out.println("Number of binary features: \t" + list.getBinarySize());
			crf = new CRF(list);
			System.out.println("Number of parameters: \t" + crf.size());
			Trainer trainer;
			switch (opt.getTrainer()) {
			case 0:
				trainer = new SGDTrainer(crf);
				break;
			case 1:
				trainer = new LBFGSTrainer(crf);
				break;
			case 2:
				trainer = new LBFGSPTrainer(crf);
				break;
			default:
				System.err.println(opt.help());
				return;
			}
			Timer.on();
			trainer.train(opt);
			Timer.show();
			crf.write(new File("mod/conll"));
			
			crf = new CRF(new File("mod/conll"));
			System.err.println("Reread: " + crf.test(list));
			
		} else {
			crf = new CRF(new File("mod/conll"));
			list = new InstanceList(crf.getList());
			list.readCoNLL(Tools.readFile("dat/train.txt"), -1);
			System.out.println("Number of states: \t" + list.getStateNumber());
			System.out.println("Number of unary features: \t" + list.getUnarySize());
			System.out.println("Number of binary features: \t" + list.getBinarySize());
			System.out.println("Number of parameters: \t" + crf.size());
		}
		//list.printFeatures();
		System.err.println("Train error::" + crf.test(list));
		// crf.test2File(list, new File("res/train"));
		//System.err.println("Test error::" + crf.test(tList));
		// crf.test2File(tList, new File("res/test"));
	}
}