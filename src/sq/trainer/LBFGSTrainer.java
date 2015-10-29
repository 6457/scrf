package sq.trainer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

import riso.numerical.LBFGS;
import riso.numerical.LBFGS.ExceptionWithIflag;
import sq.data.InstanceList;
import sq.model.Model;
import sq.utils.Timer;

public class LBFGSTrainer extends Trainer {

	public LBFGSTrainer(Model model) {
		super(model);
	}

	public void train() {


	}

	public double twoNorm(double[] w) {
		double sum = 0;
		for (int i = 0; i < w.length; i++) {
			sum += w[i] * w[i];
		}
		return sum;
	}

	public void train(Options opt, InstanceList vlist) {
		double tol = opt.tol;
		double eps = opt.eta;
		double sigma = opt.lam;
		int maxiter = opt.max;
		
		int m = 7;
		int[] iflag = { 0 };
		double[] diag = new double[model.pars.length];
		Arrays.fill(diag, sigma);
		double ov = -Double.MAX_VALUE;
		NumberFormat format = new DecimalFormat("#0.00");
		Timer.on();
		for (int i = 0; i < maxiter; i++) {
			try {
				model.gradAll();
				LBFGS.lbfgs(model.size(), m, model.pars, model.ll, model.grads, false, diag, new int[] { -1, 0 }, eps,
						Double.MIN_NORMAL, iflag);
				if (Math.abs(model.ll - ov) < tol) {
					break;
				}
				ov = model.ll;
				System.out.println("epoch=" + i + ";\t obj=" + format.format(ov) + ";\t time=" + format.format(Timer.get()));
				System.out.println("error: " + model.test(model.getList()));
				if (vlist != null) {
					System.out.println("valid: " + model.test(vlist));
				}
			} catch (ExceptionWithIflag e) {
				e.printStackTrace();
			}
		}
	}
}