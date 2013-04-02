package com.snoffleware.android.rationalcalcbase;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Brain {
	private Context context;
	private SharedPreferences preferences;
	private ExpressionParser parser;
	private Display display;
	private String expression = "";

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	private int oldUnitType;
	private int oldPrecision;
	private Boolean oldRounding;
	private String oldDisplayOptions;
	private EntryType lastEntryType = null;
	private Boolean selfTestFailed;

	Brain(Context context, ExpressionParser parser, Display display) {
		this.context = context;
		this.parser = parser;
		this.display = display;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	private void saveCurrentPreferences() {
		//oldUnitType = preferences.getInt("displayUnit", UnitType.FOOT.getId());
		oldUnitType = preferences.getInt("displayUnit", UnitType.FOOTINCH.getId());
		oldPrecision = preferences.getInt("precision", 16);
		oldRounding = preferences.getBoolean("roundUp", true);
		oldDisplayOptions = preferences.getString("displayOptions", context.getString(R.string.displayAutomatic));
	}

	private void restoreCurrentPreferences() {
		SharedPreferences.Editor editor = preferences.edit();
		editor = preferences.edit();
		editor.putInt("displayUnit", oldUnitType);
		editor.putInt("precision", oldPrecision);
		editor.putBoolean("roundUp", oldRounding);
		editor.putString("displayOptions", oldDisplayOptions);
		editor.commit();
	}

	private void setTestPreferences(UnitType unitType, int precision,
			Boolean rounding, String displayOptions, StringBuilder sb) {
		// temporarily set unit to specified unit
		SharedPreferences.Editor editor = preferences.edit();
		editor = preferences.edit();
		editor.putInt("displayUnit", unitType.getId());
		editor.putInt("precision", precision);
		editor.putBoolean("roundUp", rounding);
		editor.putString("displayOptions", displayOptions);
		editor.commit();

		// show test setting
		sb.append("-- " + context.getString(R.string.selfTestSetPreferences) + " --\n\n");
		sb.append("Unit: "
				+ UnitType.getUnitTypeFromId(
						preferences								
						.getInt("displayUnit", UnitType.FOOTINCH.getId())) //.getInt("displayUnit", UnitType.FOOT.getId()))
						.getAbbreviation());
		sb.append("\n");
		sb.append(context.getString(R.string.selfTestSetPrecision) + ": "
				+ "1/" + String.valueOf(preferences.getInt("precision", 16)));
		sb.append("\n");
		String r = "";
		if (preferences.getBoolean("roundUp", true)) {
			r = context.getString(R.string.up);
		} else {
			r = context.getString(R.string.down);
		}
		sb.append(context.getString(R.string.selfTestSetRounding) + ": " + r);
		sb.append("\n");
		sb.append(context.getString(R.string.selfTestSetDisplayOptions) + ": " + 
				preferences.getString("displayOptions", context.getString(R.string.displayAutomatic)));
		sb.append("\n\n");
	}

	public String testSanity() {
		StringBuilder sb = new StringBuilder();
		
		try {
			selfTestFailed = false;
			saveCurrentPreferences();
			runExpressionTests(sb);

			if (selfTestFailed) {
				sb.insert(0, context.getString(R.string.selfTestOverallFailure)
						+ "\n\n");
			} else {
				sb.insert(0, context.getString(R.string.selfTestOverallSuccess)
						+ "\n\n");
			}
		} catch (Exception ex) {
			sb.append(context.getString(R.string.selfTestOverallFailure)
					+ "\n\n" + ex.toString());
		} finally {
			restoreCurrentPreferences();
		}

		return sb.toString();
	}

	private void runExpressionTests(StringBuilder sb) {
		sb.append("-- feet inch --\n\n");
		
		setTestPreferences(UnitType.FOOT, 16, true, context.getString(R.string.displayAutomatic), sb);
		
		sb.append(checkSolveResult("1 + .25 =", "1 4/16"));
		sb.append(checkSolveResult("1 + 0.25 =", "1 4/16"));
		sb.append(checkSolveResult(".25 + 2 =", "2 4/16"));
		sb.append(checkSolveResult("0.25 + 2 =", "2 4/16"));
		sb.append(checkSolveResult("1 ft + .25 in =", "1 ft"));
		sb.append(checkSolveResult("1 ft + 0.25 in =", "1 ft"));
		sb.append(checkSolveResult(".25 ft + 2 in =", "7/16 ft"));
		sb.append(checkSolveResult("0.25 ft + 2 in =", "7/16 ft"));
		
		setTestPreferences(UnitType.FOOTINCH, 16, true, context.getString(R.string.displayAutomatic), sb);
		
		sb.append(checkSolveResult("1 + .5 =", "1 ft 6 in"));
		sb.append(checkSolveResult("1 + 0.5 =", "1 ft 6 in"));
		sb.append(checkSolveResult(".25 + 2 =", "2 ft 3 in"));
		sb.append(checkSolveResult("0.25 + 2 =", "2 ft 3 in"));
		sb.append(checkSolveResult("1 ft + .25 in =", "1 ft 4/16 in"));
		sb.append(checkSolveResult("1 ft + 0.25 in =", "1 ft 4/16 in"));
		sb.append(checkSolveResult(".25 ft + 2 in =", "0 ft 5 in"));
		sb.append(checkSolveResult("0.25 ft + 2 in =", "0 ft 5 in"));
		
		setTestPreferences(UnitType.FOOTINCH, 16, true, context.getString(R.string.displayAutomatic), sb);
		
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm - 1 km 1 m 1 cm 1 mm =", "0 ft 0 in"));
		sb.append(checkSolveResult("1 ft =", "1 ft 0 in"));
		
		setTestPreferences(UnitType.INCH, 12, true,context.getString(R.string.displayAutomatic), sb);
				
		sb.append(checkSolveResult("53 in × 31.2 =", "1653 7/12 in"));
		sb.append(checkSolveResult("1.2 in + 1.3 in + 1.4 in + 39/41 in =", "4 10/12 in"));
		
		setTestPreferences(UnitType.FOOTINCH, 16, true, context.getString(R.string.displayAutomatic), sb);

		sb.append(checkSolveResult("10 ft 10 ft + 1 ft 1 ft - 1 ft 1 ft =", "20 ft 0 in"));
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm + 1 km 1 m 1 cm 1 mm =", "6568 ft 3 12/16 in"));
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm × 3 =", "9852 ft 5 10/16 in"));
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm ÷ 3 =", "1094 ft 8 10/16 in"));
		
		sb.append(checkSolveResult("10 =", "10 ft 0 in"));
		sb.append(checkSolveResult("10 ft =", "10 ft 0 in"));
		sb.append(checkSolveResult("10 ft 1 in =", "10 ft 1 in"));
		sb.append(checkSolveResult("10 ft 1 1/2 in =", "10 ft 1 8/16 in"));
		
		sb.append(checkSolveResult("1.25 ft + 2.75 ft =", "4 ft 0 in"));
		sb.append(checkSolveResult("2 1/2 in =", "0 ft 2 8/16 in"));
		sb.append(checkSolveResult("1 mi 1 yd 1 ft 1 in + 1 mi 1 yd 1 ft 1 in =", "10568 ft 2 in"));
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm =", "3284 ft 1 14/16 in"));
		sb.append(checkSolveResult("1 km 1 m 1 cm 1 mm + 1 km 1 m 1 cm 1 mm =", "6568 ft 3 12/16 in"));
		sb.append(checkSolveResult("48 in + 12 in =", "5 ft 0 in"));
		sb.append(checkSolveResult("1.25 m 10 1/2 in + 1.25 yd 10.5 cm =", "9 ft 14/16 in"));
		
		setTestPreferences(UnitType.FOOTINCH, 16, true, context.getString(R.string.displayDecimal), sb);
		
		sb.append(checkSolveResult("6.26 ft 9 1/2 mm 16 5/16 in 1.1 mi + 10 ft =", "5825 ft 7.8 in"));
		
		setTestPreferences(UnitType.FOOT, 16, true, context.getString(R.string.displayAutomatic), sb);

		sb.append("-- unit and unitless --\n\n");

		sb.append(checkSolveResult("2 =", "2"));
		sb.append(checkSolveResult("10 ft =", "10 ft"));
		sb.append(checkSolveResult("2 + 10 ft =", "12 ft"));
		sb.append(checkSolveResult("2 ft + 10 =", "12 ft"));
		sb.append(checkSolveResult("10 - 2 ft =", "8 ft"));
		sb.append(checkSolveResult("10 ft - 2 =", "8 ft"));
		sb.append(checkSolveResult("2 + 10 =", "12"));
		sb.append(checkSolveResult("10 - 2 =", "8"));
		sb.append(checkSolveResult("10 - 2 ft =", "8 ft"));
		sb.append(checkSolveResult("2 × 10 =", "20"));
		sb.append(checkSolveResult("2 × 10 ft =", "20 ft"));
		sb.append(checkSolveResult("10 ÷ 2 =", "5"));
		sb.append(checkSolveResult("10 ÷ 2 ft =", context
				.getString(R.string.error)));
		sb.append(checkSolveResult("10 ft ÷ 2 =", "5 ft"));

		sb.append("-- addition --\n\n");
		sb.append(checkSolveResult("4 ft + 4 in =", "4 5/16 ft"));
		sb.append(checkSolveResult("4 4/3 in + 8 9/16 in =", "1 3/16 ft"));

		sb.append("-- subtraction --\n\n");
		sb.append(checkSolveResult("4 ft - 12 in =", "3 ft"));
		sb.append(checkSolveResult("30 in - 1 ft =", "1 8/16 ft"));

		sb.append("-- decimal --\n\n");
		sb.append(checkSolveResult("4.25 ft + 12.5 in =", "5 5/16 ft"));

		sb.append("-- fraction --\n\n");
		sb.append(checkSolveResult("1 1/2 + 2 8/16 =", "4"));
		sb.append(checkSolveResult("1 1/2 - 1/2 =", "1"));
		sb.append(checkSolveResult("1 1/2 + 1/2 ft =", "2 ft"));
		sb.append(checkSolveResult("1 1/2 ft - 1/2 =", "1 ft"));
		
		sb.append("-- division --\n\n");
		sb.append(checkSolveResult("33333323323333 ÷ 0.5 ft =", context.getString(R.string.error)));
		sb.append(checkSolveResult("3332333 ft ÷ 0.5 =", "6664666 ft"));
		sb.append(checkSolveResult("5 ft ÷ 2.5 =", "2 ft"));
		sb.append(checkSolveResult("5 ft ÷ 2.51 =", "2 ft"));
		
		sb.append("-- multiplication --\n\n");
		sb.append(checkSolveResult("10.25 yd × 3 =", "92 4/16 ft"));
		sb.append(checkSolveResult("3 × 10 1/4 in =", "2 9/16 ft"));
		sb.append(checkSolveResult("2 in × 1/2 ft =", context.getString(R.string.error)));
		sb.append(checkSolveResult("2 1/2 in × 1.2 m =", context.getString(R.string.error)));


		sb.append("-- multiple measurements --\n\n");
		sb.append(checkSolveResult("2 mm + 3 mm - 4 mm + 10 mm =", "1/16 ft"));
		sb.append(checkSolveResult("20 mm × 100 ÷ 10 =", "10/16 ft"));
		sb.append(checkSolveResult("12.3 mm - 24 1/3 in + 15 39/41 ft =",
				"13 15/16 ft"));
		sb.append(checkSolveResult("12.3 mm - 24 1/3 in + 8.2394837325 m =",
				"25 1/16 ft"));

		sb.append("-- convert --\n\n");
		sb.append(checkConvertResult("23 ft =", UnitType.YARD, "7 11/16 yd"));
		sb.append(checkConvertResult("1 1/2 yd =", UnitType.FOOT, "4 8/16 ft"));
		sb.append(checkConvertResult("11.533 mm =", UnitType.CENTIMETRE,
				"1.2 cm"));
		sb.append(checkConvertResult("405 15/16 km =", UnitType.MILE,
				"252 4/16 mi"));
		sb.append(checkConvertResult("20004593 mm =", UnitType.FOOT,
				"65631 14/16 ft"));
		sb.append(checkConvertResult("234.539 ft =", UnitType.KILOMETRE,
				"0.1 km"));
		
		setTestPreferences(UnitType.CENTIMETRE, 1, true,context.getString(R.string.displayAutomatic), sb);
		sb.append(checkConvertResult("5 in =", UnitType.CENTIMETRE, "13 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 10, true,context.getString(R.string.displayAutomatic), sb);		
		sb.append(checkConvertResult("5 in =", UnitType.CENTIMETRE, "12.7 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 100, true,context.getString(R.string.displayAutomatic), sb);		
		sb.append(checkConvertResult("5.214 in =", UnitType.CENTIMETRE, "13.24 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 1000, true,context.getString(R.string.displayAutomatic), sb);		
		sb.append(checkConvertResult("6.68 in =", UnitType.CENTIMETRE, "16.967 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 10000, true, context.getString(R.string.displayAutomatic),sb);		
		sb.append(checkConvertResult("24.38 in =", UnitType.CENTIMETRE, "61.9252 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 100000, true,context.getString(R.string.displayAutomatic), sb);		
		sb.append(checkConvertResult("24.393 in =", UnitType.CENTIMETRE, "61.95822 cm"));
		
		setTestPreferences(UnitType.CENTIMETRE, 1000000, true,context.getString(R.string.displayAutomatic), sb);		
		sb.append(checkConvertResult("24.39366 in =", UnitType.CENTIMETRE, "61.959896 cm"));
	}

	private void logAndAppend(String logTag, String stringToOutput,
			StringBuilder sb, int numberOfLineBreaks) {
		sb.append(stringToOutput);
		for (int i = 0; i < numberOfLineBreaks; i++) {
			sb.append("\n");
		}
		Log.i(logTag, stringToOutput);
	}

	private String checkSolveResult(String expression, String expected) {
		StringBuilder sb = new StringBuilder();
		String tag = context.getString(R.string.selfTestCheckSolve);

		try {
			logAndAppend(tag, expression, sb, 1);

			Measurement result;
			try {
				result = parser.solve(expression);
			} catch (ExpressionParserException epe) {
				logAndAppend(tag, epe.getMessage(), sb, 1);
				result = null;
			}
			// Measurement result = parser.solve(expression);
			String printable = parser.getPrintableResult(result);
			logAndAppend(tag, context.getString(R.string.selfTestExpected)
					+ ": " + expected, sb, 1);
			logAndAppend(tag, context.getString(R.string.selfTestActual) + ": "
					+ printable, sb, 1);

			if (expected.equals(printable)) {
				logAndAppend(tag, context.getString(R.string.selfTestSuccess),
						sb, 2);
			} else {
				logAndAppend(tag, context.getString(R.string.selfTestFailure),
						sb, 2);
				selfTestFailed = true;
			}
		} catch (Exception ex) {
			logAndAppend(tag, context.getString(R.string.error) + ": "
					+ ex.toString(), sb, 1);
			selfTestFailed = true;
		}
		return sb.toString();
	}

	private String checkConvertResult(String unitTestExpression, UnitType unitType,
			String expected) {
		StringBuilder sb = new StringBuilder();
		String tag = context.getString(R.string.selfTestCheckConvert);

		try {
			logAndAppend(tag, unitTestExpression, sb, 1);

			Measurement result;
			try {
				//result = parser.convert(expression, unitType);
				//expression here is the expression in the unit test
				result = parser.convert(parser.solve(unitTestExpression), unitType);
			} catch (ExpressionParserException epe) {
				logAndAppend(tag, epe.getMessage(), sb, 1);
				result = null;
			}
			// Measurement result = parser.convert(expression, unitType);
			String printable = parser.getPrintableResult(result);
			logAndAppend(tag, context.getString(R.string.selfTestExpected)
					+ ": " + expected, sb, 1);
			logAndAppend(tag, context.getString(R.string.selfTestActual) + ": "
					+ printable, sb, 1);

			if (expected.equals(printable)) {
				logAndAppend(tag, context.getString(R.string.selfTestSuccess),
						sb, 2);
			} else {
				logAndAppend(tag, context.getString(R.string.selfTestFailure),
						sb, 2);
				selfTestFailed = true;
			}
		} catch (Exception ex) {
			logAndAppend(tag, context.getString(R.string.error) + ": "
					+ ex.toString(), sb, 1);
			selfTestFailed = true;
		}
		return sb.toString();
	}

	private void showErrorAndResetDisplay(String message, Boolean clearState) {
		Toast.makeText(context, message, 3000).show();

		if (clearState) {
			display.clear();
			display.add(context.getString(R.string.error));
			expression = "";
		}
	}

	public void insert(String text) {
		EntryType et = getEntryItemType(text);

		Log.i("display", "insert (" + text + ") of type " + et.toString());

		if (et == EntryType.OPERATOR) {
			if (text.equals(context.getString(R.string.clear))) {
				display.clear();
				expression = "";
				// TODO: clear full expression if long press on the on button
			} else if (parser.isOperator(text)) {
				// put this number on the stack and clear the display or display
				// the current result
				if (text.equals(context.getString(R.string.equal))) {
					expression = expression + display.get()
							+ context.getString(R.string.equal);
					display.clear();
					try {
						Measurement result = parser.solve(expression);
						if (result == null) {
							display.add(context.getString(R.string.error));
							Log.e("display", "result null for: " + expression);
						} else {
							String printable = parser
									.getPrintableResult(result);
							display.add(printable);
							Log.i("display", "printable result: " + printable);
						}
					} catch (ExpressionParserException epe) {
						showErrorAndResetDisplay(epe.getMessage(), true);
					} catch (Exception ex) {
						display.add(context.getString(R.string.error));
						Log.e("display", "expression: " + expression
								+ ex.toString());
					}
					expression = "";
				} else if (text.equals(context.getString(R.string.plus))
						|| text.equals(context.getString(R.string.minus))
						|| text.equals(context.getString(R.string.multiply))
						|| text.equals(context.getString(R.string.divide))) {
					try {
						String intermediateExpression = expression
								+ display.get() + " "
								+ context.getString(R.string.equal);
						Measurement result = parser
								.solve(intermediateExpression);

						expression = expression + display.get() + " " + text
								+ " ";

						display.clear();

						if (result == null) {
							display.add(context.getString(R.string.error));
							Log.e("display", "result null for: " + expression);
						} else {
							String printable = parser
									.getPrintableResult(result);
							display.add(printable);
							Log.i("display", "printable result: " + printable);
						}
					} catch (ExpressionParserException epe) {
						showErrorAndResetDisplay(epe.getMessage(), true);
					} catch (Exception ex) {
						display.add(context.getString(R.string.error));
						Log.e("display", "expression: " + expression
								+ ex.toString());
					}
				}
			}
			lastEntryType = EntryType.OPERATOR;
		} else if (et == EntryType.DIGIT) {
			if (lastEntryType == EntryType.OPERATOR) {
				display.clear();
			}
			
			//adding compound measurements
			if(lastEntryType == EntryType.UNIT){
				display.addSpace();
			}

			// add to the number to the display
			if (text.equals(context.getString(R.string.space))) {
				display.addSpace();
			} else if (text.equals(context.getString(R.string.backspace))) {
				display.backspace();
			} else {
				display.add(text);
			}
			lastEntryType = EntryType.DIGIT;
		} else if (et == EntryType.UNIT) {
			if (text.equals(context.getString(R.string.unit))) {
				setupAddUnitDialog();
			} else {
				display.addUnit(text);
			}
			lastEntryType = EntryType.UNIT;
		} else if (et == EntryType.CONVERT) {
			setupConvertDialog();
			lastEntryType = EntryType.CONVERT;
		}
	}

	public EntryType getEntryItemType(String text) {
		String t = text.trim();
		if (t.equals(context.getString(R.string.numberZero))
				|| t.equals(context.getString(R.string.numberOne))
				|| t.equals(context.getString(R.string.numberTwo))
				|| t.equals(context.getString(R.string.numberThree))
				|| t.equals(context.getString(R.string.numberFour))
				|| t.equals(context.getString(R.string.numberFive))
				|| t.equals(context.getString(R.string.numberSix))
				|| t.equals(context.getString(R.string.numberSeven))
				|| t.equals(context.getString(R.string.numberEight))
				|| t.equals(context.getString(R.string.numberNine))
				|| t.equals(context.getString(R.string.point))
				|| t.equals(context.getString(R.string.space))
				|| t.equals(context.getString(R.string.backspace))
				|| t.equals(context.getString(R.string.fraction))) {
			return EntryType.DIGIT;
		} else if (t.equals(context.getString(R.string.plus))
				|| t.equals(context.getString(R.string.minus))
				|| t.equals(context.getString(R.string.multiply))
				|| t.equals(context.getString(R.string.divide))
				|| t.equals(context.getString(R.string.clear))
				|| t.equals(context.getString(R.string.equal))) {
			return EntryType.OPERATOR;
		} else if (t.equals(context.getString(R.string.foot))
				|| t.equals(context.getString(R.string.inch))
				|| t.equals(context.getString(R.string.unit))
				|| UnitType.isUnitType(text)) {
			return EntryType.UNIT;
		} else if (t.equals(context.getString(R.string.conv))) {
			return EntryType.CONVERT;
		} else {
			// return HistoryItemType.NONE;
			return null;
		}
	}

	private void setupConvertDialog() {
//		final int displayUnit = preferences.getInt("displayUnit", UnitType.FOOT
//				.getId());
		final int displayUnit = preferences.getInt("displayUnit", UnitType.FOOTINCH
				.getId());
		final CharSequence[] unitCharSequence = UnitType
				.getCharSequenceItemsForDisplay();
		AlertDialog.Builder unitDialog = new AlertDialog.Builder(context);
		unitDialog.setTitle(context.getString(R.string.menuDisplayConvertToUnit) + "...");
		unitDialog.setItems(unitCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = (String) unitCharSequence[item];
						UnitType ut = UnitType.getUnitTypeFromDisplayFormat(s);

						String abbreviation = ut.getAbbreviation();
						Log.i("convert", "convert to unit: " + abbreviation);
						// convert to specified format
						
						//expression in this case should be the entire formula in memory
						//String expression = display.get()
						//		+ context.getString(R.string.equal);
						
						//String expressionToConvert = expression + context.getString(R.string.equal);
						
						expression = "";						
						String expressionToConvert = display.get() + context.getString(R.string.equal);
						display.clear();
						
						try {
							//Measurement result = parser.convert(expression, ut);
							Measurement result = parser.convert(parser.solve(expressionToConvert), ut);
							if (result == null) {
								display.add(context.getString(R.string.error));
								Log.e("convert", "result null for: "
										+ expressionToConvert);
							} else {
								
								//HACK -- set display unit to correct unit
								saveCurrentPreferences();								
								SharedPreferences.Editor editor = preferences.edit();
								editor = preferences.edit();
								editor.putInt("displayUnit", ut.getId());
								editor.commit();
								//HACK -- set display unit to correct unit
																
								String printable = parser
										.getPrintableResult(result);
								display.add(printable);
								Log.i("convert", "printable result: "
										+ printable);
								
								//HACK -- set display unit to correct unit
								restoreCurrentPreferences();
								//HACK -- set display unit to correct unit
							}
						} catch (ExpressionParserException epe) {
							showErrorAndResetDisplay(epe.getMessage(), false);
						} catch (Exception ex) {
							display.add(context.getString(R.string.error));
							Log.e("convert", "expression: " + expressionToConvert
									+ ex.toString());
						}
					}
				});
		AlertDialog alert = unitDialog.create();
		alert.show();
	}

	private void setupAddUnitDialog() {
		final CharSequence[] unitCharSequence = UnitType
				.getCharSequenceItemsForUnitAddDisplay();
		AlertDialog.Builder unitDialog = new AlertDialog.Builder(context);
		unitDialog.setTitle(context.getString(R.string.menuDisplayAddUnit) + "...");
		unitDialog.setItems(unitCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = (String) unitCharSequence[item];
						UnitType ut = UnitType.getUnitTypeFromDisplayFormat(s);

						String abbreviation = ut.getAbbreviation();
						insert(abbreviation);
					}
				});
		AlertDialog alert = unitDialog.create();
		alert.show();
	}
}
