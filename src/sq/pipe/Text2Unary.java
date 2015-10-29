package sq.pipe;

import sq.data.Instance;

public class Text2Unary extends Pipe {

	private static final long serialVersionUID = 1207414908628327804L;

	public Instance proc(Instance inst) {
		for (int i = 0; i < inst.length(); i++) {
			inst.addUnary(i, inst.getText(i).toLowerCase());
		}
		return inst;
	}

}