package com.snoffleware.android.rationalcalcbase;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class Display extends EditText {

	public Display(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public String get() {
		return getText().toString();
	}

	public void add(String text) {
		setText(getText() + text);
		setSelection(length());
	}

	public void addSpace() {
		setText(getText() + " ");
		setSelection(length());
	}

	public void addUnit(String text) {
		// setText(getText() + " " + text + " ");
		setText(getText() + " " + text);
		setSelection(length());
	}

	public void backspace() {
		if (length() > 0) {
			// todo: delete unit if there is one -- otherwise remove digit
			// if(DeleteUnit)
			// String d = calculatorDisplay.getText().subSequence(0,
			// calculatorDisplay.length() - 1);

			// auto eat units
			int i = length() - 1;
			char c = getText().charAt(i);

			if (Character.isLetter(c)) {
				while (Character.isLetter(c) && length() > 0) {
					setText(getText().subSequence(0, length() - 1));
					i = length() - 1;
					c = getText().charAt(i);
				}
				//pull additional space
				if(length() > 0 && Character.isWhitespace(c)) {					
					setText(getText().subSequence(0, length() - 1));
				}
			} else {
				setText(getText().subSequence(0, length() - 1));
			}

			// if(Character.isLetter(c)) {
			// keep going till it isn't a letter anymore plus take a space?

			// setText(getText().subSequence(0, length() - 1));

			// }
			// setText(getText().subSequence(0, length() - 1));
		}
		setSelection(length());
	}

	public void clear() {
		setText("");
	}
}
