/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public class Token {
	String text;
	FeatureVector ufs;
	FeatureVector bfs;
	Collection<String> ud;
	Collection<String> bd;

	public Token(String value) {
		this(value, true);
	}

	public Token(String value, boolean count) {
		text = value;
		if (count) {
			ud = new LinkedList<>();
			bd = new LinkedList<>();
			bd.add("-B-");
		} else {
			ud = new HashSet<>();
			bd = new HashSet<>();
			bd.add("-B-");
		}
	}

	public String getText() {
		return text;
	}

	public boolean addUnary(String feature) {
		return ud.add(feature);
	}

	public boolean addBinary(String feature) {
		return bd.add(feature);
	}

	FeatureVector setUnaryFeature(FeatureVector fv) {
		return ufs = fv;
	}

	FeatureVector setBinaryFeature(FeatureVector fv) {
		return bfs = fv;
	}
}