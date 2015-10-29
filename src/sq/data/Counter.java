/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.io.Serializable;
import java.util.HashMap;

public class Counter extends HashMap<String, Integer> implements Serializable {
	private static final long serialVersionUID = -1028187120716970359L;

	public Integer add(String item) {
		Integer count = get(item);
		return count == null ? put(item, 1) : put(item, ++count);
	}
}