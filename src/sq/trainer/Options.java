package sq.trainer;

public class Options {
	double eta = 0.1;
	double tol = 0.001;
	double lam = 0.0001;
	int max = 100;
	int trainer = 2;
	int dump = 3;
	String help = "Linear chain CRF package.\nUsage:\n" 
			+ "  -n training method: 0, SGD; 1, LBFGS; 2, LBFGS parallel\n"
			+ "  -r SGD learning rate\n" 
			+ "  -d dump size\n" 
			+ "  -t likelihood tolerance\n"
			+ "  -l regularization parameter\n" 
			+ "  -m number of maximum iterations\n"
			+ "Copyright at Liu Suqi (suqi@cs.ucsd.edu)\n";

	public double setLearnRate(double rate) {
		return eta = rate;
	}

	public double setTol(double tolerance) {
		return tol = tolerance;
	}

	public double setReg(double regularization) {
		return lam = regularization;
	}

	public int setMaxIter(int numiter) {
		return max = numiter;
	}

	public int setTrainer(int method) {
		return trainer = method;
	}

	public int setDump(int size) {
		return dump = size;
	}

	public Options parseCommand(String[] args) {
		int i = 0;
		while (i < args.length) {
			switch (args[i]) {
			case "-n":
				trainer = Integer.parseInt(args[++i]);
				break;
			case "-r":
				eta = Double.parseDouble(args[++i]);
				break;
			case "-d":
				dump = Integer.parseInt(args[++i]);
				break;
			case "-t":
				tol = Double.parseDouble(args[++i]);
				break;
			case "-l":
				lam = Double.parseDouble(args[++i]);
				break;
			case "-m":
				max = Integer.parseInt(args[++i]);
				break;
			case "-h":
				System.out.println(help);
				return null;
			default:
				System.err.println("\nError: Unrecognized option.\n");
				System.err.println(help);
				return null;
			}
			++i;
		}
		return this;
	}

	public int getTrainer() {
		return trainer;
	}

	public int getDump() {
		return dump;
	}

	public String help() {
		return help;
	}

	public String toString() {
		return "eta = " + eta + "\ntol = " + tol + "\nlam = " + lam;
	}
}