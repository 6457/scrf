package sq.trainer;

import sq.model.Model;
import sq.model.Model.Score;

public abstract class CRFTrainer extends Trainer {

	public CRFTrainer(Model model) {
		super(model);
	}

	public void gradAdd(Score score) {
		model.ll += score.f;
		int[] index = score.gindex;
		double[] value = score.gvalue;
		int p = index.length;
		for (int i = 0; i < p; i++) {
			model.grads[index[i]] += value[i];
		}
	}

	public void update(Score score, double eta) {
		int[] index = score.gindex;
		double[] value = score.gvalue;
		int p = index.length;
		for (int i = 0; i < p; i++) {
			model.pars[index[i]] -= eta * value[i];
		}
	}

	public double twoNorm(double[] w) {
		double sum = 0;
		for (int i = 0; i < w.length; i++) {
			sum += w[i] * w[i];
		}
		return sum;
	}

	public void rescale(double[] w, double scale) {
		for (int i = 0; i < w.length; i++) {
			w[i] *= scale;
		}
	}

	public void add(double[] x, double[] y, double scale) {
		for (int i = 0; i < x.length; i++) {
			x[i] += y[i] * scale;
		}
	}

	public void normalize(double[] w) {
		double scale = Math.sqrt(twoNorm(w));
		rescale(w, 1 / scale);
	}
}