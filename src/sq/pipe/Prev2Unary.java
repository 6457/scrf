package sq.pipe;

import sq.data.Instance;

public class Prev2Unary extends Pipe {

	private static final long serialVersionUID = 1207414908628327804L;

	int step = 1;

	public Prev2Unary(int n) {
		step = n;
	}

	public Instance proc(Instance inst) {
		int l = inst.length();
		for (int i = 0; i < l; i++) {
			for (int j = 1; j <= step; j++) {
				if (j <= i) {
					String[] texts = inst.getText(i - j).split(" ");
					for (int k = 0; k < texts.length; k++) {
						inst.addUnary(i, texts[k] + "@P" + j);
					}
				} else {
					inst.addUnary(i, (j - i) + "B@P" + j);
				}
			}
		}
		return inst;
	}
}