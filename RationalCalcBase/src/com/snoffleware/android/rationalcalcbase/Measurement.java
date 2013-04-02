package com.snoffleware.android.rationalcalcbase;

import org.apache.commons.math.fraction.BigFraction;

public class Measurement {
	private BigFraction fraction;
	public BigFraction getFraction() {
		return fraction;
	}
	public void setFraction(BigFraction fraction) {
		this.fraction = fraction;
	}
	
	private UnitType unitType;
	public UnitType getUnitType() {
		return unitType;
	}
	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}
	
	public Measurement() { }
	
	public Measurement(BigFraction fraction, UnitType unitType) {
		this.fraction = fraction;
		this.unitType = unitType;
	}
	
	@Override
	public String toString(){
		if(unitType == null) {
			return fraction.toString();
		} else {
			return fraction.toString() + " " + unitType.getAbbreviation();	
		}
		 
	}
}
