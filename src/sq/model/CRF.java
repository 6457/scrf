package sq.model;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sq.data.Instance;
import sq.data.InstanceList;
import sq.data.LabelSequence;

public class CRF extends Model {
	int s;
	int k;
	int r;
	int lp;

	public CRF(InstanceList trainList) {
		list = trainList;
		s = list.getStateNumber();
		k = list.getUnarySize();
		r = list.getBinarySize();
		lp = (s + 1) * r + k;
		pars = new double[s * lp + s];
		// s*(s+1)*r: binary; s*k: unary; s: end
		grads = new double[pars.length];
	}

	public CRF(File file) {
		read(file);
	}

	public class Encoder {
		Instance inst;
		double[] hlam;
		double[][][] lam;
		double[] tlam;
		int ct;

		public Encoder(Instance instance) {
			inst = instance;
			int l = inst.length();
			int uv = 0;
			int bv = 0;
			hlam = new double[s];
			lam = new double[l][s][s];
			tlam = new double[s];
			double uw;
			double bw;

			if (instance.length() == 0) {
				return;
			}

			int c = 0;
			ct = 0;
			int[] uf = instance.getUnary(0);
			int[] bf = instance.getBinary(0);
			uv = uf.length;
			bv = bf.length;
			for (int j = 0; j < s; j++) {
				c = j * lp + s * r;
				bw = 0;
				for (int m = 0; m < bv; m++) {
					bw += pars[c + bf[m]];
				}
				c = j * lp + (s + 1) * r;
				uw = 0;
				for (int m = 0; m < uv; m++) {
					uw += pars[c + uf[m]];
				}
				hlam[j] = uw + bw;
			}
			ct += s * (bv + uv);

			for (int i = 1; i < l; i++) {// position
				uf = instance.getUnary(i);
				bf = instance.getBinary(i);
				uv = uf.length;
				bv = bf.length;
				for (int j = 0; j < s; j++) {// current state
					c = j * lp + (s + 1) * r;// uniary feature
					uw = 0;
					for (int m = 0; m < uv; m++) {
						uw += pars[c + uf[m]];
					}
					for (int h = 0; h < s; h++) {// previous state
						bw = 0;
						for (int m = 0; m < bv; m++) {
							bw += pars[j * lp + h * r + bf[m]];
						}
						lam[i][j][h] = bw + uw;
					}
				}
				ct += s * s * bv + s * uv;
			}

			for (int j = 0; j < s; j++) {
				tlam[j] = pars[s * lp + j];
			}
			ct += s;
		}

		public Score getScore() {
			int l = inst.length();
			double[][] alpha = new double[l][s];
			double[][] beta = new double[l][s];
			double z;

			double[] tp = new double[s];

			// forward
			for (int j = 0; j < s; j++) {
				alpha[0][j] = hlam[j];
			}
			for (int i = 1; i < l; i++) {
				for (int j = 0; j < s; j++) {
					for (int h = 0; h < s; h++) {
						tp[h] = alpha[i - 1][h] + lam[i][j][h];
					}
					alpha[i][j] = logSum(tp);
				}
			}
			for (int h = 0; h < s; h++) {
				tp[h] = alpha[l - 1][h] + tlam[h];
			}
			z = logSum(tp);
			// System.out.println(z);
			// backward
			for (int j = 0; j < s; j++) {
				beta[l - 1][j] = tlam[j];
			}
			for (int i = l - 2; i >= 0; i--) {
				for (int j = 0; j < s; j++) {
					for (int h = 0; h < s; h++) {
						tp[h] = beta[i + 1][h] + lam[i + 1][h][j];
					}
					beta[i][j] = logSum(tp);
				}
			}
			// double zp = 0;
			// for (int j = 0; j < s; j++) {
			// tp[j] = beta[0][j] + hlam[j];
			// }
			// zp = logSum(tp);
			// System.err.println(z + "::" + zp);

			int[] gindex;
			double[] gvalue;
			double ll = 0;

			gindex = new int[ct];
			gvalue = new double[ct];
			// compute gradients
			int[] labels = inst.getLabels();
			int p = 0;
			int[] uf = inst.getUnary(0);
			int[] bf = inst.getBinary(0);
			int uv = uf.length;
			int bv = bf.length;
			int c = 0;
			for (int j = 0; j < s; j++) {
				double proby = Math.exp(alpha[0][j] + beta[0][j] - z);
				if (j == labels[0]) {
					c = j * lp + s * r;
					for (int m = 0; m < bv; m++, p++) {
						gindex[p] = c + bf[m];
						gvalue[p] = proby - 1;
						ll += pars[c + bf[m]];
					}
					c = j * lp + (s + 1) * r;
					for (int m = 0; m < uv; m++, p++) {
						gindex[p] = c + uf[m];
						gvalue[p] = proby - 1;
						ll += pars[c + uf[m]];
					}
				} else {
					c = j * lp + s * r;
					for (int m = 0; m < bv; m++, p++) {
						gindex[p] = c + bf[m];
						gvalue[p] = proby;
					}
					c = j * lp + s + 1;
					for (int m = 0; m < uv; m++, p++) {
						gindex[p] = c + uf[m];
						gvalue[p] = proby;
					}
				}

			}
			for (int i = 1; i < l; i++) {// position
				uf = inst.getUnary(i);
				bf = inst.getBinary(i);
				uv = uf.length;
				bv = bf.length;
				for (int j = 0; j < s; j++) {// current state
					for (int h = 0; h < s; h++) {// previous state
						c = j * lp + h * r;// binary feature
						double probyy = Math.exp(alpha[i - 1][h] + lam[i][j][h] + beta[i][j] - z);
						if (j == labels[i] && h == labels[i - 1]) {
							for (int m = 0; m < bv; m++, p++) {
								gindex[p] = c + bf[m];
								gvalue[p] = probyy - 1;
								ll += pars[c + bf[m]];
							}
						} else {
							for (int m = 0; m < bv; m++, p++) {
								gindex[p] = c + bf[m];
								gvalue[p] = probyy;
							}
						}
					}

					c = j * lp + (s + 1) * r;// uniary feature
					double proby = Math.exp(alpha[i][j] + beta[i][j] - z);
					if (j == labels[i]) {
						for (int m = 0; m < uv; m++, p++) {
							gindex[p] = c + uf[m];
							gvalue[p] = proby - 1;
							ll += pars[c + uf[m]];
						}
					} else {
						for (int m = 0; m < uv; m++, p++) {
							gindex[p] = c + uf[m];
							gvalue[p] = proby;
						}
					}
				}
			}
			for (int j = 0; j < s; j++, p++) {
				double proby = Math.exp(alpha[l - 1][j] + beta[l - 1][j] - z);
				c = s * lp + j;
				if (j == labels[l - 1]) {
					gindex[p] = c;
					gvalue[p] = proby - 1;
					ll += pars[c];
				} else {
					gindex[p] = c;
					gvalue[p] = proby;
				}
			}
			return new Score(z - ll, gindex, gvalue);
		}

		public LabelSequence viterbi() {
			int l = inst.length();
			if (l == 0) {
				return new LabelSequence(list.getLabelAlphabet(), new int[0]);
			}
			double[][] gamma = new double[l][s];
			int[][] viters = new int[l - 1][s];

			// viterbi
			for (int j = 0; j < s; j++) {
				gamma[0][j] = hlam[j];
			}
			for (int i = 1; i < l; i++) {
				for (int j = 0; j < s; j++) {
					double best = -Double.MAX_VALUE;
					int bindex = -1;
					for (int h = 0; h < s; h++) {
						double value = gamma[i - 1][h] + lam[i][j][h];
						if (value > best) {
							best = value;
							bindex = h;
						}
					}
					gamma[i][j] = best;
					viters[i - 1][j] = bindex;
				}
			}
			double best = -Double.MAX_VALUE;
			int bindex = -1;
			for (int h = 0; h < s; h++) {
				double value = gamma[l - 1][h] + tlam[h];
				if (value > best) {
					best = value;
					bindex = h;
				}
			}
			int[] labels = new int[l];
			labels[l - 1] = bindex;
			for (int i = l - 2; i >= 0; i--) {
				labels[i] = viters[i][labels[i + 1]];
			}
			return new LabelSequence(list.getLabelAlphabet(), labels);
		}

		public double getLike(LabelSequence ls) {
			int l = inst.length();
			double[][] alpha = new double[l][s];
			double z;

			double[] tp = new double[s];

			// forward
			for (int j = 0; j < s; j++) {
				alpha[0][j] = hlam[j];
			}
			for (int i = 1; i < l; i++) {
				for (int j = 0; j < s; j++) {
					for (int h = 0; h < s; h++) {
						tp[h] = alpha[i - 1][h] + lam[i][j][h];
					}
					alpha[i][j] = logSum(tp);
				}
			}
			for (int h = 0; h < s; h++) {
				tp[h] = alpha[l - 1][h] + tlam[h];
			}

			z = logSum(tp);

			double ll = 0;

			int[] labels = ls.getLabels();
			int[] uf = inst.getUnary(0);
			int[] bf = inst.getBinary(0);
			int uv = uf.length;
			int bv = bf.length;
			int c = 0;
			int j = labels[0];
			c = j * lp + s * r;
			for (int m = 0; m < bv; m++) {
				ll += pars[c + bf[m]];
			}
			c = j * lp + (s + 1) * r;
			for (int m = 0; m < uv; m++) {
				ll += pars[c + uf[m]];
			}
			for (int i = 1; i < l; i++) {
				uf = inst.getUnary(i);
				bf = inst.getBinary(i);
				uv = uf.length;
				bv = bf.length;
				j = labels[i];
				int h = labels[i - 1];
				c = j * lp + h * r;
				for (int m = 0; m < bv; m++) {
					ll += pars[c + bf[m]];
				}
				c = j * lp + (s + 1) * r;
				for (int m = 0; m < uv; m++) {
					ll += pars[c + uf[m]];
				}

			}
			j = labels[l - 1];
			c = s * lp + j;
			ll += pars[c];
			return Math.exp(ll - z);
		}

		double logSum(double[] v) {
			double m = v[0];
			int l = v.length;
			for (int i = 1; i < l; i++) {
				m = Math.max(m, v[i]);
			}
			double s = 0;
			for (int i = 0; i < l; i++) {
				s += Math.exp(v[i] - m);
			}
			return m + Math.log(s);
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
			oos.writeInt(r);
			oos.writeInt(k);
			oos.writeInt(lp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readPars(ObjectInputStream ois) {
		try {
			s = ois.readInt();
			r = ois.readInt();
			k = ois.readInt();
			lp = ois.readInt();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}