package sq.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;

public class HMM extends Model {
	int s;
	int v;
	int lp;

	public HMM(InstanceList trainList) {
		list = trainList;
		s = list.getStateNumber();
		v = list.getUnarySize();
		lp = s + 1 + v;

		double[] weis = new double[s + s * lp];
		for (Instance inst : trainList) {
			int[] labels = inst.getLabels();
			int l = labels.length;
			++weis[labels[0]];
			for (int i = 0; i < l - 1; i++) {
				++weis[s + labels[i] * lp + labels[i + 1]];
			}
			++weis[s + labels[l - 1] * lp + s];
			for (int i = 0; i < l; i++) {
				int[] f = inst.getUnary(i);
				for (int j = 0; j < f.length; j++) {
					++weis[s + labels[i] * lp + s + 1 + f[j]];
				}
			}
		}
		pars = new double[s + s * lp];
		int z = 0;
		for (int i = 0; i < s; i++) {
			z += weis[i];
		}
		for (int i = 0; i < s; i++) {
			pars[i] = Math.log(weis[i] + 1) - Math.log(z + s);
		}
		for (int i = 0; i < s; i++) {
			z = 0;
			for (int j = 0; j < s + 1; j++) {
				z += weis[s + i * lp + j];
			}
			for (int j = 0; j < s + 1; j++) {
				pars[s + i * lp + j] = Math.log(weis[s + i * lp + j] + 1) - Math.log(z + s + 1);
			}
			z = 0;
			for (int k = 0; k < v; k++) {
				z += weis[s + i * lp + s + 1 + k];
			}
			for (int k = 0; k < v; k++) {
				pars[s + i * lp + s + 1 + k] = Math.log(weis[s + i * lp + s + 1 + k] + 1) - Math.log(z + v);
			}
		}
	}

	public class Encoder {
		Instance inst;

		public Encoder(Instance instance) {
			inst = instance;
		}

		public LabelSequence viterbi() {
			int l = inst.length();

			double[][] gamma = new double[l][s];
			int[][] viters = new int[l - 1][s];

			int[] f = inst.getUnary(0);
			for (int j = 0; j < s; j++) {
				double uwei = 0;
				for (int m = 0; m < f.length; m++) {
					uwei += pars[s + j * lp + s + 1 + f[m]];
				}
				gamma[0][j] = pars[j] + uwei;
			}

			for (int i = 1; i < l; i++) {
				f = inst.getUnary(i);
				for (int j = 0; j < s; j++) {
					double best = -Double.MAX_VALUE;
					int bindex = -1;
					for (int h = 0; h < s; h++) {
						double value = gamma[i - 1][h] + pars[s + j * lp + h];
						if (value > best) {
							best = value;
							bindex = h;
						}
					}
					double uwei = 0;
					for (int m = 0; m < f.length; m++) {
						uwei += pars[s + j * lp + s + 1 + f[m]];
					}
					gamma[i][j] = best + uwei;
					viters[i - 1][j] = bindex;
				}
			}
			double best = -Double.MAX_VALUE;
			int bindex = -1;
			for (int j = 0; j < s; j++) {
				double value = gamma[l - 1][j] + pars[s + j * lp + s];
				if (value > best) {
					best = value;
					bindex = j;
				}
			}
			int[] labels = new int[l];
			labels[l - 1] = bindex;
			for (int i = l - 2; i >= 0; i--) {
				labels[i] = viters[i][labels[i + 1]];
			}
			return new LabelSequence(list.getLabelAlphabet(), labels);
		}

		public Score getScore() {
			return null;
		}

	}

	public Score getScore(Instance instance) {
		return new Encoder(instance).getScore();
	}

	public LabelSequence getLabel(Instance instance) {
		return new Encoder(instance).viterbi();
	}

	public void writePars(ObjectOutputStream oos) {
		try {
			oos.writeInt(s);
			oos.writeInt(v);
			oos.writeInt(lp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readPars(ObjectInputStream ois) {
		try {
			s = ois.readInt();
			v = ois.readInt();
			lp = ois.readInt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}