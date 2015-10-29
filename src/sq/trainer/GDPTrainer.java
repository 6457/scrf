package sq.trainer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sq.data.Instance;
import sq.data.InstanceList;
import sq.model.Model;
import sq.model.Model.Score;
import sq.utils.Timer;

public class GDPTrainer extends CRFTrainer {

	public GDPTrainer(Model model) {
		super(model);
	}

	public void train(Options opt, InstanceList vlist) {
		double eta = opt.eta;
		double tol = opt.tol;
		double lam = opt.lam;
		int maxiter = opt.max;

		double ov = -Double.MAX_VALUE;
		double ll = 0;
		InstanceList list = model.getList();
		int n = list.size();
		int p = model.size();
		NumberFormat format = new DecimalFormat("#0.00");
		Timer.on();
		for (int i = 0; i < maxiter; i++) {
			ll = 0;
			try {
				List<Score> scores = processInputs(list);
				Arrays.fill(model.grads, 0);
				for (Score score : scores) {
					gradAdd(score);
					ll += score.f;
				}
				double et = eta / n;
				double el = eta * lam;
				for (int j = 0; j < p; j++) {
					model.pars[j] *= (1 - el);
					model.pars[j] -= model.grads[j] * et;
				}
				ll = ll / n + lam / 2 * twoNorm(model.pars);
				if (Math.abs(ov - ll) < tol) {
					break;
				}
				ov = ll;
				System.out.println("epoch=" + i + ";\t obj=" + format.format(ll) + ";\t time="
						+ format.format(Timer.get()));
				System.out.println("error: " + model.test(list));
				if (vlist != null) {
					System.out.println("valid: " + model.test(vlist));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Score> processInputs(List<Instance> inputs) throws InterruptedException, ExecutionException {

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);

		List<Future<Score>> futures = new ArrayList<Future<Score>>();
		for (final Instance inst : inputs) {
			Callable<Score> callable = new Callable<Score>() {
				public Score call() throws Exception {
					Score output = model.getScore(inst);
					;
					// process your input here and compute the output
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

}