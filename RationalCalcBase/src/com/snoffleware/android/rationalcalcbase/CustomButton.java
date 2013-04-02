package com.snoffleware.android.rationalcalcbase;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

class CustomButton extends Button implements OnClickListener {
	
	OnClickListener clickListener;
	
	public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		RationalCalc calc = (RationalCalc)context;
		clickListener = calc.listener;
		setOnClickListener(this);
	}
	
	public void onClick(View view){
		clickListener.onClick(this);
	}
}


