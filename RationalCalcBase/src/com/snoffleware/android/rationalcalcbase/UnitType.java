package com.snoffleware.android.rationalcalcbase;

import java.util.ArrayList;

public enum UnitType {
	MILE (0, "mi", "miles", 1609.344, "fraction"),
	YARD (1, "yd", "yards", 0.9144, "fraction"),
	FOOT (2, "ft", "feet", 0.3048, "fraction"),
	INCH (3, "in", "inches", 0.0254, "fraction"),
	KILOMETRE (4, "km", "kilometers", 1000.0, "decimal"),
	METRE (5, "m", "meters", 1.0, "decimal"),
	CENTIMETRE (6, "cm", "centimeters", 0.01, "decimal"),
	MILLIMETRE (7, "mm", "millimeters", 0.001, "decimal"),
	FOOTINCH (8, "ft/in", "foot/inch", 0.3048, "fraction");
	
	private int id;	
	public int getId() {
		return id;
	}
	private String abbreviation;
	public String getAbbreviation() {
		return abbreviation;
	}
	private String plural;
	public String getPlural() {
		return plural;
	}
	private double meters;
	public double getMeters() {
		return meters;
	}
	private String fractionOrDecimal;
	public String getFractionalOrDecimal() {
		return fractionOrDecimal;
	}
	
	UnitType(int id, String abbreviation, String plural, double meters, String fractionalOrDecimal) {
		this.id = id;
		this.abbreviation = abbreviation;
		this.plural = plural;
		this.meters = meters;
		this.fractionOrDecimal = fractionalOrDecimal;
	}
	
	public static CharSequence[] getCharSequenceItemsForDisplay() {
		ArrayList<String> units = new ArrayList<String>();
		for(UnitType ut : UnitType.values()) {
			String u = ut.getPlural() + " (" + ut.getAbbreviation() + ")";
			units.add(u);
		}
		int size = units.size();
		return units.toArray(new CharSequence[size]);
	}
	
	public static CharSequence[] getCharSequenceItemsForUnitAddDisplay() {
		ArrayList<String> units = new ArrayList<String>();
		for(UnitType ut : UnitType.values()) {
			if(ut != UnitType.FOOTINCH) {
				String u = ut.getPlural() + " (" + ut.getAbbreviation() + ")";
				units.add(u);
			}
		}
		int size = units.size();
		return units.toArray(new CharSequence[size]);
	}
	
	public static UnitType getUnitTypeFromDisplayFormat(String unitInDisplayFormat) {
		for(UnitType ut : UnitType.values()) {
			String u = ut.getPlural() + " (" + ut.getAbbreviation() + ")";
			if(u.equals(unitInDisplayFormat)) {
				return ut;
			}
		}
		return null;
	}
		
	public static UnitType getUnitTypeFromId(int id){
		for(UnitType ut : UnitType.values()){
			if(id == ut.getId()){
				return ut;
			}
		}
		return null;
	}
	
	public static UnitType getUnitTypeFromPlural(String plural) {
		for(UnitType ut : UnitType.values()) {
			if(plural.equals(ut.getPlural())) {
				return ut;
			}
		}
		return null;
	}
	
	public static UnitType getUnitTypeFromAbbreviation(String abbreviation){
		for(UnitType ut : UnitType.values()) {
			if(abbreviation.equals(ut.getAbbreviation())){
				return ut;
			}
		}
		return null;
	}
	
	public static Boolean isUnitType(String abbreviation) {
		for(UnitType ut : UnitType.values()){
			if(abbreviation.equals(ut.getAbbreviation())) {
				return true;
			}
		}
		return false;
	}
}
