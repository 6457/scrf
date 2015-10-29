/*
 * Suqi Liu, 01-06-2015
 * 
 */

package sq.data;

import java.util.Collection;

public class TokenSequence {
	private Token[] tokens;
	private int length;

	public TokenSequence(String[] values) {
		length = values.length;
		tokens = new Token[length];
		for (int i = 0; i < length; i++) {
			tokens[i] = new Token(values[i]);
		}
	}
	
	public TokenSequence(Collection<String> values) {
		length = values.size();
		tokens = new Token[length];
		int i = 0;
		for (String value : values) {
			tokens[i] = new Token(value);
			++i;
		}
	}

	public Token get(int i) {
		return tokens[i];
	}

	public int length() {
		return length;
	}
}