package sq.pipe;

import sq.data.Instance;

public class TextSplit2Unary extends Pipe {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5097605148483840636L;

	public Instance proc(Instance inst) {
		int n = inst.length();
		for (int i = 0; i < n; i++) {
			String value = inst.getText(i);
			String[] feats = value.split("[^a-zA-Z0-9']");
			for (int j = 0; j < feats.length; j++) {
				if (feats[j].length() > 0) {
					inst.addUnary(i, feats[j]);
				}
			}
		}
		return inst;
	}

}