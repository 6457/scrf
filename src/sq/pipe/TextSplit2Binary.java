package sq.pipe;

import sq.data.Instance;

public class TextSplit2Binary extends Pipe {

	private static final long serialVersionUID = -5156243253296746470L;

	public Instance proc(Instance inst) {
		int n = inst.length();
		for (int i = 0; i < n; i++) {
			String value = inst.getText(i);
			String[] feats = value.split("[^a-zA-Z0-9']");
			for (int j = 0; j < feats.length; j++) {
				inst.addBinary(i, feats[j]);
			}
		}
		return inst;
	}
	
}