package sq.trainer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;

import sq.data.Instance;
import sq.data.InstanceList;
import sq.model.Model;
import sq.model.Model.Score;
import sq.utils.Timer;

public class SGDTrainer extends CRFTrainer {
	public SGDTrainer(Model model) {
		super(model);
	}

	public void train(Options opt, InstanceList vlist) {
		double eta = opt.eta;
		double tol = opt.tol;
		double lam = opt.lam;
		int maxiter = opt.max;

		double ov = -Double.MAX_VALUE;
		double wscale = 1;
		double et = eta;
		double t = eta * lam;
		double ll = 0;
		InstanceList list = model.getList();
		int n = list.size();
		NumberFormat format = new DecimalFormat("#0.00");
		Timer.on();
		for (int i = 0; i < maxiter; i++) {
			ll = 0;
			// System.err.println(model.test(list));
			Collections.shuffle(list);
			for (Instance inst : list) {
				Score score = model.getScore(inst);
				et = 1 / (lam * t);
				wscale = (1 - et * lam);
				rescale(model.pars, wscale);
				update(score, et);
				ll += score.f;
				++t;
			}
			double twonorm = twoNorm(model.pars);
			ll = ll / n + lam / 2 * twonorm;
			System.out.println("two norm: " + format.format(twonorm));
			System.out
					.println("epoch=" + i + ";\t obj=" + format.format(ll) + ";\t time=" + format.format(Timer.get()));
			System.out.println("error: " + model.test(list));
			
			if (Math.abs(ov - ll) < tol) {
				break;
			}
			ov = ll;
			if (vlist != null) {
				System.out.println("valid: " + model.test(vlist));
			}
		}
	}
}