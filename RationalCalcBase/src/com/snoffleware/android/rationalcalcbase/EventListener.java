package com.snoffleware.android.rationalcalcbase;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

public class EventListener implements OnKeyListener, OnClickListener,
		OnLongClickListener {
	
	Brain brain;
	
	void setHandler(Brain brain) {
		this.brain = brain;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		/*case R.id.equal :
			//equals
			break;
		case R.id.backspace :
			//logic delete character
			break;
		case R.id.on :
			//clear all
			break;
		case R.id.conv :
			//bring up convert to menu
			break;*/
		default :
			//insert character
			if(v instanceof Button) {
				String text = ((Button)v).getText().toString();
				brain.insert(text);	
			}			
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if(id == R.id.backspace){
			//clear all
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onKey(View v, int code, KeyEvent key) {
		//keyboard events
		int action = key.getAction();
		
		if(action == KeyEvent.ACTION_UP){
			switch(code){
			case KeyEvent.KEYCODE_ENTER :
			case KeyEvent.KEYCODE_DPAD_CENTER :
				//equals
				break;
			}
		}		
		return true;
	}
}
