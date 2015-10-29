package sq.pipe;

import sq.data.Instance;

public class Next2Unary extends Pipe {

	private static final long serialVersionUID = 1207414908628327804L;

	int step = 1;

	public Next2Unary(int n) {
		step = n;
	}

	public Instance proc(Instance inst) {
		int l = inst.length();
		for (int i = 0; i < l; i++) {
			for (int j = 1; j <= step; j++) {
				if (i + j < l) {
					String[] texts = inst.getText(i + j).split(" ");
					for (int k = 0; k < texts.length; k++) {
						inst.addUnary(i, texts[k] + "@N" + j);
					}
				} else {
					inst.addUnary(i, (j + i - l + 1) + "E@N" + j);
				}

			}
		}
		return inst;
	}
}