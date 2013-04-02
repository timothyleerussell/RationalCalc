package com.snoffleware.android.rationalcalcbase;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class Tokenizer {	
	private Context context;
	
	public Tokenizer(Context context) {
		this.context = context;
	}
	
	public String logTokens(String expression) {
		String tokenReconstitution = "";
		List<String> tokens = tokenize(expression);

		for (int i = 0; i <= tokens.size() - 1; i++) {
			Log.i("Solve", tokens.get(i));
			tokenReconstitution += tokens.get(i) + " ";
		}
		return tokenReconstitution;
	}

	public List<String> tokenize(String expression) {
		char c;
		List<String> tokens = new ArrayList<String>();
		String previousToken = null;
		int i = 0;
		while (i < expression.length()) {
			c = expression.charAt(i);
			StringBuilder currentToken = new StringBuilder();

			if (c == ' ' || c == '\t') // whitespace -- skip
			{
				i++;
			} else if (c == '-'
					&& (previousToken == null || isOperator(previousToken))
					&& ((i + 1) < expression.length() && Character
							.isDigit(expression.charAt((i + 1))))) // negative
			// number -- add to tokens
			{
				currentToken.append(expression.charAt(i));
				i++;
				while (i < expression.length()
						&& Character.isDigit(expression.charAt(i))) {
					currentToken.append(expression.charAt(i));
					i++;
				}
			} else if (Character.isDigit(c)) // number -- add to tokens
			{
				while (i < expression.length()
						&& (Character.isDigit(expression.charAt(i))
								|| String.valueOf(expression.charAt(i)).equals(
										context.getString(R.string.fraction)) || String
								.valueOf(expression.charAt(i)).equals(
										context.getString(R.string.point)))) {
					currentToken.append(expression.charAt(i));
					i++;
				}
			} else if (Character.isLetter(c)) // unit of measure -- add to
			// tokens
			{
				while (i < expression.length()
						&& (Character.isLetter(expression.charAt(i)))) {
					currentToken.append(expression.charAt(i));
					i++;
				}
			} else if (isOperator(String.valueOf(c))) // operator -- add to
			// tokens
			{
				currentToken.append(c);
				i++;
			} else // no match -- toss the token
			{
				i++;
			}

			if (currentToken.length() > 0) {
				tokens.add(currentToken.toString());
				previousToken = currentToken.toString();
			}
		}
		return tokens;
	}
	
	public boolean isOperator(String c) {
		if (c.equals(context.getString(R.string.divide))
				|| c.equals(context.getString(R.string.plus))
				|| c.equals(context.getString(R.string.minus))
				|| c.equals(context.getString(R.string.multiply))
				|| c.equals(context.getString(R.string.equal))) {
			return true;
		} else {
			return false;
		}
	}
}
