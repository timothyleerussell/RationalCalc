package com.snoffleware.android.rationalcalcbase;

import org.apache.commons.math.fraction.BigFraction;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class ExpressionParser {
	private Context context;
	private SharedPreferences preferences;
	private UnitType displayUnit;
	private Integer precisionPreference;
	private boolean roundUpPreference;
	private String displayOptionsPreference;
	// private Measurement subTotal;
	Boolean unitlessCalculation = true;

	public ExpressionParser(Context context) {
		this.context = context;
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Measurement solve(String expression)
			throws ExpressionParserException {

//		displayUnit = UnitType.getUnitTypeFromId(preferences.getInt(
//				"displayUnit", UnitType.FOOT.getId()));
		displayUnit = UnitType.getUnitTypeFromId(preferences.getInt(
				"displayUnit", UnitType.FOOTINCH.getId()));

		List<String> segment = new ArrayList<String>();
		List<String> tokens = tokenize(expression);
		Queue<Measurement> measurements = new LinkedList<Measurement>();
		Queue<String> operators = new LinkedList<String>();

		unitlessCalculation = true;

		for (int counter = 0; counter < tokens.size(); counter++) {
			String currentToken = tokens.get(counter).trim();
			String previousToken, nextToken;

			// Log.i("solve", "counter: " + String.valueOf(counter) + " size: "
			// + String.valueOf(tokens.size()) + " token: " + currentToken);
			try {
				previousToken = tokens.get(counter - 1).trim();
			} catch (Exception e) {
				previousToken = null;
			}

			try {
				// Log.i("solve", "currentToken + 1 = " +
				// tokens.get(counter+1).trim());
				nextToken = tokens.get(counter + 1).trim();
			} catch (Exception e) {
				// Log.e("solve", "currentToken + 1 is null" + e.toString());
				nextToken = null;
			}

			// Log.i("solve", "currentToken: " + currentToken);
			// Log.i("solve", "nextToken: " + nextToken);
			if (!isOperator(currentToken)) {
				segment.add(currentToken);
				// Log.i("solve segment add", currentToken);

				// if(UnitType.isUnitType(currentToken) && nextToken != null &&
				// !isEqualOperator(nextToken)) {
				if (UnitType.isUnitType(currentToken) && nextToken != null
						&& !isOperator(nextToken)) {
					// operators.add(context.getString(R.string.plus));
					operators.add(context.getString(R.string.compoundplus));
					// Log.i("solve operator add", "auto +");
					measurements.add(segmentToMeasurement(segment));
					// Log.i("solve measurement add", segmentToString(segment));
					segment.clear();
				}
				// } else if(isOperator(currentToken)) {
			} else {
				operators.add(currentToken);
				// Log.i("solve operator add", currentToken);
				measurements.add(segmentToMeasurement(segment));
				// Log.i("solve measurement add", segmentToString(segment));
				segment.clear();

				if (isEqualOperator(currentToken))
					break;
			}
		}

		// for (Measurement measurement : measurements) {
		// Log.i("solve measurement", measurement.toString());
		// }
		// for (String operator : operators) {
		// Log.i("solve measurement", operator.toString());
		// }

		// calculate
		Measurement subTotal = Calculate(measurements, operators);

		// Log.i("solve =>", subTotal.toString());
		// if the calculation had units, convert back to default unit
		if (!unitlessCalculation) {
			Measurement newSubTotal = new Measurement();
			newSubTotal.setFraction(subTotal.getFraction());
			newSubTotal.setUnitType(displayUnit);

			subTotal = convertFromMeters(newSubTotal);
		} else {
			// return the current subTotal
		}

		return subTotal;
	}

	private Measurement Calculate(Queue<Measurement> measurements,
			Queue<String> operators) throws ExpressionParserException {

		// for (Measurement measurement : measurements) {
		// Log.i("solve measurement", measurement.toString());
		// }
		// for (String operator : operators) {
		// Log.i("solve measurement", operator.toString());
		// }

		// add up all the compound measurements
		Queue<Measurement> m = new LinkedList<Measurement>();
		Queue<String> o = new LinkedList<String>();

		Measurement subTotal = new Measurement();
		Measurement initial = measurements.remove();
		BigFraction initialFraction;

		if (initial.getUnitType() != null) {
			initialFraction = convertToMeters(initial).getFraction();
		} else {
			String operatorPeek = operators.peek();
			Measurement nextPeek = measurements.peek();
			if ((nextPeek != null && nextPeek.getUnitType() != null)) {
				if ((operatorPeek.equals(context.getString(R.string.plus))
						|| operatorPeek.equals(context
								.getString(R.string.minus)) || operatorPeek
						.equals(context.getString(R.string.compoundplus)))) {
					initial.setUnitType(nextPeek.getUnitType());
					initialFraction = convertToMeters(initial).getFraction();
				} else {
					initialFraction = initial.getFraction();
				}
			} else {
				initialFraction = initial.getFraction();
			}
		}
		subTotal.setFraction(initialFraction);

		boolean isCompoundMeasurement = false;

		Measurement previous = initial;

		if (operators.size() == 1) {
			m.add(subTotal);
			o.add(operators.remove());
		} else {
			while (operators.size() > 1) {
				String operator = operators.remove();
				Measurement next = measurements.remove();
				BigFraction nextFraction;
				if (next.getUnitType() != null) {
					nextFraction = convertToMeters(next).getFraction();
				} else {
					if (previous.getUnitType() != null
							&& (operator.equals(context
									.getString(R.string.plus))
									|| operator.equals(context
											.getString(R.string.minus)) || operator
									.equals(context
											.getString(R.string.compoundplus)))) {
						next.setUnitType(previous.getUnitType());
						nextFraction = convertToMeters(next).getFraction();
					} else {
						nextFraction = next.getFraction();
					}
				}

				if (operator.equals(context.getString(R.string.compoundplus))) {
					subTotal.setFraction(subTotal.getFraction().add(
							nextFraction));
					isCompoundMeasurement = true;
				} else if (operator.equals(context.getString(R.string.plus))) {
					m.add(subTotal);
					o.add(operator);
					subTotal = new Measurement();
					// subTotal.setFraction(BigFraction.ZERO);
					subTotal.setFraction(nextFraction);
					isCompoundMeasurement = false;
				} else if (operator.equals(context.getString(R.string.minus))) {
					m.add(subTotal);
					o.add(operator);
					subTotal = new Measurement();
					// subTotal.setFraction(BigFraction.ZERO);
					subTotal.setFraction(nextFraction);
					isCompoundMeasurement = false;
				} else if (operator
						.equals(context.getString(R.string.multiply))) {
					// can't multiply unit by a unit
					if (previous.getUnitType() != null
							&& next.getUnitType() != null) {
						throw new ExpressionParserException(
								context.getString(R.string.errorSolveMultiplyByUnit));
					} else {
						m.add(subTotal);
						o.add(operator);
						subTotal = new Measurement();
						// subTotal.setFraction(BigFraction.ZERO);
						subTotal.setFraction(nextFraction);
						isCompoundMeasurement = false;
					}
				} else if (operator.equals(context.getString(R.string.divide))) {
					// can't divide a unitless number by a unit
					if (previous.getUnitType() == null
							&& next.getUnitType() != null) {
						throw new ExpressionParserException(
								context.getString(R.string.errorSolveDivideByUnitlessMeasurement));
					} else if (previous.getUnitType() != null
							&& next.getUnitType() != null) {
						throw new ExpressionParserException(
								context.getString(R.string.errorSolveDivideUnitByUnit));
					} else {
						m.add(subTotal);
						o.add(operator);
						subTotal = new Measurement();
						// subTotal.setFraction(BigFraction.ZERO);
						subTotal.setFraction(nextFraction);
						isCompoundMeasurement = false;
					}
				}

				if (operators.size() == 1) {
					// equal sign
					if (isCompoundMeasurement) {
						// we've already added the last measurement...?
						// subTotal.setFraction(subTotal.getFraction().add(nextFraction));
					} else {
						// otherwise -- add it?
						subTotal.setFraction(nextFraction);
					}

					m.add(subTotal);
					o.add(context.getString(R.string.equal));
				}
				previous = next;
			}
		}

		// for (Measurement measurement : m) {
		// Log.i("solve m: ", measurement.toString());
		// }
		// for (String operator : o) {
		// Log.i("solve o: ", operator.toString());
		// }

		// total the resulting measurements
		subTotal = new Measurement();

		Measurement inital2 = m.remove();
		// Log.i("solve", "initial: " + initial.toString());
		BigFraction initialFraction2;
		if (inital2.getUnitType() != null) {
			initialFraction2 = convertToMeters(inital2).getFraction();
			// Log.i("solve", "inital fraction (unit): "
			// + initialFraction.toString());
		} else {
			// Log.i("solve", "inital fraction (unitless): "
			// + initialFraction.toString());
			String operatorPeek = o.peek();
			Measurement nextPeek = m.peek();
			if ((nextPeek != null && nextPeek.getUnitType() != null)) {
				if ((operatorPeek.equals(context.getString(R.string.plus)) || operatorPeek
						.equals(context.getString(R.string.minus)))) {
					inital2.setUnitType(nextPeek.getUnitType());
					initialFraction2 = convertToMeters(inital2).getFraction();
				} else {
					initialFraction2 = inital2.getFraction();
				}
			} else {
				initialFraction2 = inital2.getFraction();
			}
		}
		subTotal.setFraction(initialFraction2);

		Measurement previous2 = inital2;

		while (o.size() > 1) {
			String operator = o.remove();
			// Log.i("solve", "operator: " + operator);
			Measurement next2 = m.remove();
			// Log.i("solve", "next: " + next.toString());
			BigFraction nextFraction;
			if (next2.getUnitType() != null) {
				nextFraction = convertToMeters(next2).getFraction();
				// Log.i("solve", "next fraction (unit): "
				// + nextFraction.toString());
			} else {
				// Log.i("solve", "next fraction (unitless): "
				// + nextFraction.toString());
				if (previous2.getUnitType() != null
						&& (operator.equals(context.getString(R.string.plus)) || operator
								.equals(context.getString(R.string.minus)))) {
					next2.setUnitType(previous2.getUnitType());
					nextFraction = convertToMeters(next2).getFraction();
				} else {
					nextFraction = next2.getFraction();
				}
			}

			// Log.i("solve", "pop operator: " + operator);
			if (operator.equals(context.getString(R.string.plus))) {
				subTotal.setFraction(subTotal.getFraction().add(nextFraction));
			} else if (operator.equals(context.getString(R.string.minus))) {
				subTotal.setFraction(subTotal.getFraction().subtract(
						nextFraction));
			} else if (operator.equals(context.getString(R.string.multiply))) {
				// // can't multiply unit by a unit
				// if (previous2.getUnitType() != null
				// && next2.getUnitType() != null) {
				// throw new ExpressionParserException(
				// context.getString(R.string.errorSolveMultiplyByUnit));
				// } else {
				subTotal.setFraction(subTotal.getFraction().multiply(
						nextFraction));
				// }
			} else if (operator.equals(context.getString(R.string.divide))) {
				// // can't divide a unitless number by a unit
				// if (previous2.getUnitType() == null
				// && next2.getUnitType() != null) {
				// throw new ExpressionParserException(
				// context.getString(R.string.errorSolveDivideByUnitlessMeasurement));
				// } else if (previous2.getUnitType() != null
				// && next2.getUnitType() != null) {
				// throw new ExpressionParserException(
				// context.getString(R.string.errorSolveDivideUnitByUnit));
				// } else {
				subTotal.setFraction(subTotal.getFraction()
						.divide(nextFraction));
				// }
			}
			previous2 = next2;
		}

		return subTotal;
	}

	private String segmentToString(List<String> segment) {
		String result = "";
		for (String s : segment) {
			result += s + " ";
		}
		return result;
	}

	// public Measurement convert(String expression, UnitType toUnit)
	public Measurement convert(Measurement m, UnitType toUnit)
			throws ExpressionParserException {

		Log.i("convert measurement", m.toString());

		if (m.getUnitType() == null) {
			throw new ExpressionParserException(
					context.getString(R.string.errorConvertUnitUnspecified));
		}

		Measurement subTotal = new Measurement();
		subTotal.setFraction(convertToMeters(m).getFraction());
		// we're converting everything to meters for the calculation
		subTotal.setUnitType(UnitType.METRE);

		// subtotal is in meters now -- convert it back to whatever it should be
		Measurement newSubTotal = new Measurement();
		newSubTotal.setFraction(subTotal.getFraction());
		newSubTotal.setUnitType(toUnit);

		subTotal = convertFromMeters(newSubTotal);

		return subTotal;
	}

	private Measurement segmentToMeasurement(List<String> segment) {
		Measurement m = new Measurement();

		if (segment.size() == 1) {
			// Log.i("solve", "segment size: 1");
			if (segment.get(0).contains("/")) {
				// first token is a fraction
				BigFraction fraction = new BigFraction(Integer.parseInt(segment
						.get(0).split("/")[0]), Integer.parseInt(segment.get(0)
						.split("/")[1]));
				m.setFraction(fraction);
				m.setUnitType(null);
			} else {
				// token is an integer or a decimal
				m.setFraction(new BigFraction(
						Double.parseDouble(segment.get(0))));
				m.setUnitType(null);
			}
		} else if (segment.size() == 2) {
			// Log.i("solve", "segment size: 2");
			if (Character.isLetter(segment.get(1).charAt(0))) {
				// first token is the measurement
				if (segment.get(0).contains("/")) {
					// fraction
					BigFraction fraction = new BigFraction(
							Integer.parseInt(segment.get(0).split("/")[0]),
							Integer.parseInt(segment.get(0).split("/")[1]));
					m.setFraction(fraction);
				} else {
					// integer or decimal
					m.setFraction(new BigFraction(Double.parseDouble(segment
							.get(0))));
				}
				// second token is the unit
				m.setUnitType(UnitType.getUnitTypeFromAbbreviation(segment
						.get(1)));
				unitlessCalculation = false;
			} else if (segment.get(1).contains("/")) {
				// first and second token are a mixed number
				BigFraction mixedNumber = new BigFraction(
						Double.parseDouble(segment.get(0)));
				BigFraction fraction = new BigFraction(Integer.parseInt(segment
						.get(1).split("/")[0]), Integer.parseInt(segment.get(1)
						.split("/")[1]));
				m.setFraction(mixedNumber.add(fraction));
				m.setUnitType(null);
			}
		} else if (segment.size() == 3) {
			// Log.i("solve", "segment size: 3");
			// mixed number and unit
			BigFraction mixedNumber = new BigFraction(
					Double.parseDouble(segment.get(0)));
			BigFraction fraction = new BigFraction(Integer.parseInt(segment
					.get(1).split("/")[0]), Integer.parseInt(segment.get(1)
					.split("/")[1]));
			m.setFraction(mixedNumber.add(fraction));
			// unit specified
			m.setUnitType(UnitType.getUnitTypeFromAbbreviation(segment.get(2)));
			unitlessCalculation = false;
		}

		return m;
	}

	private Measurement convertFromMeters(Measurement measure) {
		Measurement result = new Measurement();
		double conversion = 1 / measure.getUnitType().getMeters();
		BigFraction scalingFromMeters = new BigFraction(conversion);

		result.setFraction(measure.getFraction().multiply(scalingFromMeters));
		result.setUnitType(measure.getUnitType());

		return result;
	}

	private Measurement convertToMeters(Measurement measure) {
		Measurement result = new Measurement();
		double conversion = measure.getUnitType().getMeters();
		BigFraction scalingToMeters = new BigFraction(conversion);

		result.setFraction(measure.getFraction().multiply(scalingToMeters));
		result.setUnitType(UnitType.METRE);

		return result;
	}

	public String logTokens(String expression) {
		String tokenReconstitution = "";
		List<String> tokens = tokenize(expression);

		for (int i = 0; i <= tokens.size() - 1; i++) {
			// Log.i("Solve", tokens.get(i));
			tokenReconstitution += tokens.get(i) + " ";
		}
		return tokenReconstitution;
	}

	private List<String> tokenize(String expression) {
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
			} else if (Character.isDigit(c) || (String.valueOf(c).equals(context.getString(R.string.point)))) // number -- add to tokens
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

	public boolean isEqualOperator(String c) {
		if (c.equals(context.getString(R.string.equal))) {
			return true;
		} else {
			return false;
		}
	}

	public String getPrintableResult(Measurement result) {
		if (result == null) {
			return context.getString(R.string.error);
		}
		String printable = "";
		precisionPreference = preferences.getInt("precision", 16);
		roundUpPreference = preferences.getBoolean("roundUp", true);
		displayOptionsPreference = preferences.getString("displayOptions",
				context.getString(R.string.displayAutomatic));

		// TEMP: use display unit to mod this for conversions???
//		displayUnit = UnitType.getUnitTypeFromId(preferences.getInt(
//				"displayUnit", UnitType.FOOT.getId()));
		displayUnit = UnitType.getUnitTypeFromId(preferences.getInt(
				"displayUnit", UnitType.FOOTINCH.getId()));
		// use display unit to mod this for conversions???

		Measurement feet = null;
		Measurement inches = null;
		if (displayUnit == UnitType.FOOTINCH) {
			BigInteger numerator = result.getFraction().getNumerator();
			BigInteger denominator = result.getFraction().getDenominator();
			BigInteger wholeNumberFeet = numerator.divide(denominator);
			BigInteger remainderFeet = numerator.mod(denominator);
			
			if(wholeNumberFeet.compareTo(BigInteger.ZERO) == 0 &&
				remainderFeet.compareTo(BigInteger.ZERO) == 0) {
				feet = new Measurement(new BigFraction(0), UnitType.FOOT);
				inches = new Measurement(new BigFraction(0), UnitType.INCH);
			} else {
				BigInteger remainderInches = remainderFeet.multiply(new BigInteger("12"));
				BigDecimal remainderFeetRounded = new BigDecimal(remainderFeet, 20);
				BigDecimal denominatorRounded = new BigDecimal(denominator, 20);
				
				if (remainderFeetRounded.intValue() == denominatorRounded
					.intValue()	&& result.getUnitType() != null) {
					wholeNumberFeet = wholeNumberFeet.add(new BigInteger("1"));
					inches = new Measurement(new BigFraction(0, 1), UnitType.INCH);
				} else {
					inches = new Measurement(new BigFraction(remainderInches,
							denominator), UnitType.INCH);
				}
		
				feet = new Measurement(new BigFraction(wholeNumberFeet),
						UnitType.FOOT);
			}			
			result = inches;
		}

		BigInteger numerator = result.getFraction().getNumerator();
		BigInteger denominator = result.getFraction().getDenominator();
		BigInteger wholeNumber = numerator.divide(denominator);
		BigInteger remainder = numerator.mod(denominator);
		BigDecimal precision = new BigDecimal(precisionPreference);
		BigDecimal remains = new BigDecimal(remainder);
		BigDecimal numeratorTimesPrecision = remains.multiply(precision);
		BigDecimal top = numeratorTimesPrecision.divide(new BigDecimal(
				denominator), 9, BigDecimal.ROUND_HALF_EVEN);
		BigDecimal halfUp = top.setScale(0, BigDecimal.ROUND_HALF_UP);
		BigDecimal halfDown = top.setScale(0, BigDecimal.ROUND_HALF_DOWN);

		BigInteger roundedNumerator;
		if (roundUpPreference) {
			roundedNumerator = new BigInteger(String.valueOf(halfUp));
		} else {
			roundedNumerator = new BigInteger(String.valueOf(halfDown));
		}

		denominator = new BigInteger(precisionPreference.toString());

		if (roundedNumerator.equals(BigInteger.ZERO)) {
			if (result.getUnitType() != null) {
				if (displayUnit == UnitType.FOOTINCH) {
					printable = feet.toString() + " "
							+ String.valueOf(wholeNumber) + " in";
				} else {
					printable = String.valueOf(wholeNumber) + " "
							+ result.getUnitType().getAbbreviation();
				}
			} else {
				printable = String.valueOf(wholeNumber);
			}
		} else if (!roundedNumerator.equals(denominator)) {
			if (result.getUnitType() != null) {
				if (wholeNumber.equals(BigInteger.ZERO)) {
					if (displayUnit == UnitType.FOOTINCH) {
						printable = feet.toString() + " "
								+ roundedNumerator.toString() + "/"
								+ denominator.toString() + " in";
					} else {
						printable = roundedNumerator.toString() + "/"
								+ denominator.toString() + " "
								+ result.getUnitType().getAbbreviation();
					}
				} else {
					if (displayUnit == UnitType.FOOTINCH) {
						printable = feet.toString() + " "
								+ String.valueOf(wholeNumber) + " "
								+ roundedNumerator.toString() + "/"
								+ denominator.toString() + " in";
					} else {
						printable = String.valueOf(wholeNumber) + " "
								+ roundedNumerator.toString() + "/"
								+ denominator.toString() + " "
								+ result.getUnitType().getAbbreviation();
					}
				}
			} else {
				if (wholeNumber.equals(BigInteger.ZERO)) {
					printable = roundedNumerator.toString() + "/"
							+ denominator.toString();
				} else {
					printable = String.valueOf(wholeNumber) + " "
							+ roundedNumerator.toString() + "/"
							+ denominator.toString();
				}
			}
		} else {
			if (result.getUnitType() != null) {
				if (displayUnit == UnitType.FOOTINCH) {
					printable = feet.toString() + " "
							+ String.valueOf(wholeNumber.add(BigInteger.ONE))
							+ " in";
				} else {
					printable = String.valueOf(wholeNumber.add(BigInteger.ONE))
							+ " " + result.getUnitType().getAbbreviation();
				}
			} else {
				printable = String.valueOf(wholeNumber.add(BigInteger.ONE));
			}
		}

		// display automatic - fractional - decimal
		if (displayOptionsPreference.equals(context
				.getString(R.string.displayAutomatic))) {
			if (result.getUnitType() != null) {
				if (result.getUnitType().getFractionalOrDecimal() == "decimal") {
					int numberOfDecimalPlaces = precisionPreference.toString()
							.length() - 1;

					if (roundUpPreference) {
						if (displayUnit == UnitType.FOOTINCH) {
							printable = feet.toString()
									+ " "
									+ result.getFraction()
											.bigDecimalValue(
													numberOfDecimalPlaces,
													BigDecimal.ROUND_HALF_UP)
											.toString() + " "
									+ result.getUnitType().getAbbreviation();
						} else {
							printable = result
									.getFraction()
									.bigDecimalValue(numberOfDecimalPlaces,
											BigDecimal.ROUND_HALF_UP)
									.toString()
									+ " "
									+ result.getUnitType().getAbbreviation();
						}
					} else {
						if (displayUnit == UnitType.FOOTINCH) {
							printable = feet.toString()
									+ " "
									+ result.getFraction()
											.bigDecimalValue(
													numberOfDecimalPlaces,
													BigDecimal.ROUND_HALF_DOWN)
											.toString() + " "
									+ result.getUnitType().getAbbreviation();
						} else {
							printable = result
									.getFraction()
									.bigDecimalValue(numberOfDecimalPlaces,
											BigDecimal.ROUND_HALF_DOWN)
									.toString()
									+ " "
									+ result.getUnitType().getAbbreviation();
						}
					}
				}
			}
		} else if (displayOptionsPreference.equals(context
				.getString(R.string.displayFractional))) {
			// always fractional
			// leave results as they currently are
		} else if (displayOptionsPreference.equals(context
				.getString(R.string.displayDecimal))) {
			// always decimal
			int numberOfDecimalPlaces = precisionPreference.toString().length() - 1;

			if (result.getUnitType() != null) {
				if (roundUpPreference) {
					if (displayUnit == UnitType.FOOTINCH) {
						printable = feet.toString()
								+ " "
								+ result.getFraction()
										.bigDecimalValue(numberOfDecimalPlaces,
												BigDecimal.ROUND_HALF_UP)
										.toString() + " "
								+ result.getUnitType().getAbbreviation();
					} else {
						printable = result
								.getFraction()
								.bigDecimalValue(numberOfDecimalPlaces,
										BigDecimal.ROUND_HALF_UP).toString()
								+ " " + result.getUnitType().getAbbreviation();
					}
				} else {
					if (displayUnit == UnitType.FOOTINCH) {
						printable = feet.toString()
								+ " "
								+ result.getFraction()
										.bigDecimalValue(numberOfDecimalPlaces,
												BigDecimal.ROUND_HALF_DOWN)
										.toString() + " "
								+ result.getUnitType().getAbbreviation();
					} else {
						printable = result
								.getFraction()
								.bigDecimalValue(numberOfDecimalPlaces,
										BigDecimal.ROUND_HALF_DOWN).toString()
								+ " " + result.getUnitType().getAbbreviation();
					}
				}
			} else {
				if (roundUpPreference) {
					printable = result
							.getFraction()
							.bigDecimalValue(numberOfDecimalPlaces,
									BigDecimal.ROUND_HALF_UP).toString();
				} else {
					printable = result
							.getFraction()
							.bigDecimalValue(numberOfDecimalPlaces,
									BigDecimal.ROUND_HALF_DOWN).toString();
				}
			}
		}

		return printable;
	}
}
