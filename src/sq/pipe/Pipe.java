package sq.pipe;

import java.io.Serializable;

import sq.data.Instance;

public abstract class Pipe implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4994603562797481894L;

	public abstract Instance proc(Instance inst);
}