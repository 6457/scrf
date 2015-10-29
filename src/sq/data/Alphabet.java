/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Alphabet implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5665733122416895846L;
	
	private HashMap<String, Integer> map;
	private ArrayList<String> pam;
	private int size;
	public Alphabet() {
		size = 0;
		map = new HashMap<>();
		pam = new ArrayList<>();
	}
	public int add(String item) {
		if (map.containsKey(item)) {
			return map.get(item);
		} else {
			pam.add(item);
			map.put(item, size);
			return size++;
		}
	}
	public boolean has(String item) {
		return map.containsKey(item);
	}
	public int look(String item) {
		return map.get(item);
	}
	public String get(int index) {
		if (index < size) {
			return pam.get(index);
		}
		return null;
	}
	public int size() {
		return size;
	}
}