package sq.trainer;

import sq.data.InstanceList;
import sq.model.Model;

public abstract class Trainer {
	Model model;

	public Trainer(Model model) {
		this.model = model;
	}

	public abstract void train(Options opt, InstanceList vlist);

	public void train(Options options) {
		train(options, null);
	}

	public void train() {
		train(new Options());
	}
	
	public String toString() {
		return getClass().getName();
	}
}