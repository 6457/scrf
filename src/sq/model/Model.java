package sq.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sq.data.Alphabet;
import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;
import sq.pipe.Pipe;
import sq.utils.Tools;

public abstract class Model {
	public double[] pars;
	public double ll;
	public double[] grads;
	InstanceList list;

	public int size() {
		return pars.length;
	}

	public abstract class Encoder {
		Instance inst;

		public Encoder(Instance instance) {
			inst = instance;
		}

		public abstract Score getScore();
	}

	public class Score {
		public double f;
		public int[] gindex;
		public double[] gvalue;

		public Score(double funcvalue, int[] gradindex, double[] gradvalue) {
			f = funcvalue;
			gindex = gradindex;
			gvalue = gradvalue;
		}
	}

	public abstract Score getScore(Instance instance);

	public abstract LabelSequence getLabel(Instance instance);

	public void getScore(InstanceList list) {
		ll = 0;
		Arrays.fill(grads, 0);
		for (Instance inst : list) {
			Score score = getScore(inst);
			ll += score.f;
			int[] index = score.gindex;
			double[] value = score.gvalue;
			int p = index.length;
			for (int i = 0; i < p; i++) {
				grads[index[i]] += value[i];
			}
		}
	}

	public void getScoreParallel() {
		ll = 0;
		Arrays.fill(grads, 0);
		try {
			List<Score> scores = processInputs(list);
			for (Score score : scores) {
				ll += score.f;
				int[] index = score.gindex;
				double[] value = score.gvalue;
				int p = index.length;
				for (int i = 0; i < p; i++) {
					grads[index[i]] += value[i];
				}
			}
			int n = list.size();
			for (int i = 0; i < grads.length; i++) {
				grads[i] /= n;
			}
			ll /= n;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Score> processInputs(List<Instance> inputs) throws InterruptedException, ExecutionException {

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);

		List<Future<Score>> futures = new ArrayList<Future<Score>>();
		for (final Instance inst : inputs) {
			Callable<Score> callable = new Callable<Score>() {
				public Score call() throws Exception {
					Score output = getScore(inst);
					return output;
				}
			};
			futures.add(service.submit(callable));
		}

		service.shutdown();

		List<Score> outputs = new ArrayList<Score>();
		for (Future<Score> future : futures) {
			outputs.add(future.get());
		}
		return outputs;
	}

	public void gradAll() {
		getScore(list);
	}

	public InstanceList getList() {
		return list;
	}

	public void write(File file) {
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
			oos.writeObject(list.getLabelAlphabet());
			oos.writeObject(list.getUnaryAlphabet());
			oos.writeObject(list.getBinaryAlphabet());
			oos.writeObject(list.getPipes());
			int l = pars.length;
			oos.writeInt(l);
			for (int i = 0; i < l; i++) {
				oos.writeDouble(pars[i]);
			}
			writePars(oos);
			oos.close();
		} catch (IOException e) {
			System.err.println("Exception writing file " + file + ": " + e);
		}
	}

	@SuppressWarnings("unchecked")
	public void read(File file) {
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
			list = new InstanceList((Alphabet) ois.readObject(), (Alphabet) ois.readObject(),
					(Alphabet) ois.readObject(), (Collection<Pipe>) ois.readObject());
			int l = ois.readInt();
			pars = new double[l];
			for (int i = 0; i < l; i++) {
				pars[i] = ois.readDouble();
			}
			readPars(ois);
			ois.close();
		} catch (IOException e) {
			System.err.println("Exception writing file " + file + ": " + e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public double[] testList(InstanceList list) {
		int wct = 0;
		int wer = 0;
		int dct = 0;
		int der = 0;

		for (Instance inst : list) {
			int[] ls = inst.getLabels();
			int[] pred = getLabel(inst).getLabels();
			boolean fd = false;
			for (int i = 0; i < inst.length(); i++) {
				if (pred[i] != ls[i]) {
					fd = true;
					++wer;
				}
				++wct;
			}
			if (fd) {
				++der;
			}
			++dct;
		}
		return new double[] { (double) wer / wct, (double) der / dct };
	}

	public double test(InstanceList list) {
		int ct = 0;
		int er = 0;
		for (Instance inst : list) {
			int[] ls = inst.getLabels();
			int l = ls.length;
			ct += l;
			int[] pred = getLabel(inst).getLabels();
			for (int i = 0; i < l; i++) {
				if (pred[i] != ls[i]) {
					++er;
				}
			}
		}
		return ((double) er) / ct;
	}

	public double testDocum(InstanceList list) {
		int ct = 0;
		int er = 0;
		StringBuilder sb = new StringBuilder();
		for (Instance inst : list) {
			int[] ls = inst.getLabels();
			int[] pred = getLabel(inst).getLabels();
			boolean fd = false;
			for (int i = 0; i < inst.length(); i++) {
				if (pred[i] != ls[i]) {
					fd = true;
				}
			}
			if (fd) {
				sb.append(inst.getName() + "\n");
				++er;
			}
			++ct;
		}
		try {
			Tools.writeFile("err", sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ((double) er) / ct;
	}

	public double test2Print(InstanceList list) {
		int er = 0;
		for (Instance inst : list) {
			int[] ls = inst.getLabels();
			int l = ls.length;
			int[] pred = getLabel(inst).getLabels();
			StringBuilder sb = new StringBuilder();
			Alphabet ua = list.getUnaryAlphabet();
			Alphabet ba = list.getBinaryAlphabet();
			for (int i = 0; i < l; i++) {
				if (pred[i] != ls[i]) {
					sb.append(inst.getText(i) + "::" + ls[i] + "::" + pred[i] + "\n");
					sb.append("-->");
					int[] uf = inst.getUnary(i);
					for (int k = 0; k < uf.length; k++) {
						sb.append("u:" + ua.get(uf[k]) + " ");
					}
					int[] bf = inst.getBinary(i);
					for (int k = 0; k < bf.length; k++) {
						sb.append("b:" + ba.get(bf[k]) + " ");
					}
					sb.append("\n");
				}
			}
			if (sb.length() > 0) {
				++er;
				System.out.println(inst.getName());
				System.out.println(sb);
			}
		}
		return ((double) er) / list.size();
	}

	public void test2File(InstanceList list, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		Alphabet labelAlpha = list.getLabelAlphabet();
		for (Instance inst : list) {
			int[] ls = inst.getLabels();
			int l = ls.length;
			int[] pred = getLabel(inst).getLabels();
			for (int i = 0; i < l; i++) {
				bw.append(labelAlpha.get(ls[i]) + " " + labelAlpha.get(pred[i]) + "\n");
			}
		}
		bw.close();
	}

	public abstract void writePars(ObjectOutputStream oos);

	public abstract void readPars(ObjectInputStream ois);

}