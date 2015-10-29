package sq.trainer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import riso.numerical.LBFGS;
import riso.numerical.LBFGS.ExceptionWithIflag;
import sq.data.InstanceList;
import sq.model.Model;
import sq.utils.Timer;

public class LBFGSPTrainer extends CRFTrainer {

	public LBFGSPTrainer(Model model) {
		super(model);
	}

	public void train(Options opt, InstanceList vlist) {
		double tol = opt.tol;
		double sigma = opt.lam;
		int maxiter = opt.max;
		
		int m = 7;
		int[] iflag = { 0 };
		int ct = 0;
		double[] diag = new double[model.pars.length];
		Arrays.fill(diag, sigma);
		double ov = -Double.MAX_VALUE;
		NumberFormat timefmt = new DecimalFormat("#0.00");
		NumberFormat objfmt = new DecimalFormat("#0.000000");
		Timer.on();
		for (int i = 0; i < maxiter; i++) {
			try {
				model.getScoreParallel();
				LBFGS.lbfgs(model.size(), m, model.pars, model.ll, model.grads, false, diag, new int[] { -1, 0 }, 0,
						Double.MIN_NORMAL, iflag);
				System.out.println("status: " + iflag[0]);
				System.out.println("two norm: " + timefmt.format(twoNorm(model.pars)));
				if (Math.abs(model.ll - ov) < tol) {
					++ct;
				} else {
					ct = 0;
				}
				ov = model.ll;
				System.out.println("epoch=" + i + ";\t obj=" + objfmt.format(ov) + ";\t time=" + timefmt.format(Timer.get()));
				System.out.println("error: " + model.test(model.getList()));
				if (vlist != null) {
					System.out.println("valid: " + model.test(vlist));
				}
				if (ct > 2) {
					break;
				}
			} catch (ExceptionWithIflag e) {
				e.printStackTrace();
			}
		}
	}
}