package com.snoffleware.android.rationalcalcbase;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class RationalCalc extends Activity {
	EventListener listener = new EventListener();
	private Brain brain;
	private ExpressionParser parser;
	private Display display;
	private SharedPreferences preferences;
	private Integer displayUnit;
	private Integer precisionPreference;
	private boolean roundUpPreference;
	private Integer useCount;
	private String displayOptions;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		display = (Display) findViewById(R.id.display);

		// logic
		parser = new ExpressionParser(this);
		brain = new Brain(this, parser, display);
		listener.setHandler(brain);
		
		// if there is a saveInstanceState, load it
		if (savedInstanceState != null) {
			brain.setExpression(savedInstanceState.getString("expression"));
			display.clear();
			display.add(savedInstanceState.getString("displayText"));
		}

		// test brain
		// brain.testSanity();

		boolean firstrun = preferences.getBoolean("firstrun_1.0.8", true);
		if (firstrun) {
			SharedPreferences.Editor e = preferences.edit();
			e.putBoolean("firstrun_1.0.8", false);
			e.commit();
			showNewReleaseDialog();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = preferences.edit();
		editor = preferences.edit();
		editor.putInt("useCount", ++useCount);
		editor.putInt("displayUnit", displayUnit);
		editor.putInt("precision", precisionPreference);
		editor.putBoolean("roundUp", roundUpPreference);
		editor.putString("displayOptions", displayOptions);
		editor.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		useCount = preferences.getInt("useCount", 0);
		//displayUnit = preferences.getInt("displayUnit", UnitType.FOOT.getId());
		displayUnit = preferences.getInt("displayUnit", UnitType.FOOTINCH.getId());
		precisionPreference = preferences.getInt("precision", 16);
		roundUpPreference = preferences.getBoolean("roundUp", true);
		displayOptions = preferences.getString("displayOptions",
				getString(R.string.displayAutomatic));
		Log.i("Use Count:", String.valueOf(useCount));
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		String expression = brain.getExpression();
		savedInstanceState.putString("expression", expression);
		String displayText = display.get();
		savedInstanceState.putString("displayText", displayText);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		brain.setExpression(savedInstanceState.getString("expression"));
		display.clear();
		display.add(savedInstanceState.getString("displayText"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem itemDisplayUnit = menu.add(R.string.menuDisplayDisplayUnit);
		itemDisplayUnit.setIcon(android.R.drawable.ic_menu_day);
		MenuItem itemPrecision = menu.add(R.string.menuPrecision);
		itemPrecision.setIcon(android.R.drawable.ic_menu_sort_by_size);
		MenuItem itemRounding = menu.add(R.string.menuRounding);
		itemRounding.setIcon(android.R.drawable.ic_menu_info_details);
		MenuItem itemDisplayOptions = menu.add(R.string.menuDisplayOptions);
		itemDisplayOptions.setIcon(android.R.drawable.ic_menu_crop);
		MenuItem itemSelfTest = menu.add(R.string.menuSelfTest);
		itemSelfTest.setIcon(android.R.drawable.ic_menu_compass);
		MenuItem itemHelp = menu.add(R.string.menuHelp);
		itemHelp.setIcon(android.R.drawable.ic_menu_help);
		MenuItem itemAbout = menu.add(R.string.menuAbout);
		itemAbout.setIcon(android.R.drawable.ic_menu_info_details);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		String selection = (String) item.getTitle();
		if (selection.equals(getString(R.string.menuDisplayDisplayUnit))) {
			setupDisplayUnitPreferenceDialog();
		} else if (selection.equals(getString(R.string.menuPrecision))) {
			setupPrecisionPreferenceDialog();
		} else if (selection.equals(getString(R.string.menuRounding))) {
			setupRoundUpPreferenceDialog();
		} else if (selection.equals(getString(R.string.menuDisplayOptions))) {
			setupDisplayOptionsDialog();
		} else if (selection.equals(getString(R.string.menuSelfTest))) {
			Builder selfTestDialog = new AlertDialog.Builder(this);
			selfTestDialog.setTitle(getString(R.string.app_name) + " "
					+ getString(R.string.menuSelfTest));
			selfTestDialog.setMessage(brain.testSanity());
			selfTestDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
		} else if (selection.equals(getString(R.string.menuHelp))) {
			Builder helpDialog = new AlertDialog.Builder(this);
			helpDialog.setTitle(getString(R.string.app_name) + " "
					+ getString(R.string.helpTitle));
			helpDialog.setMessage(R.string.help);
			helpDialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).show();
		} else if (selection.equals(getString(R.string.menuAbout))) {
			showAboutDialog();
		}
		return true;
	}

	private void showAboutDialog() {
		String title = getString(R.string.aboutTitle) + " "
				+ getString(R.string.app_name);
		String about = getString(R.string.app_name) + " v."
				+ getString(R.string.aboutVersionNumber) + "\n"
				+ getString(R.string.about) + "\n\n"
				+ getString(R.string.aboutDescription) + "\n\n"
				+ getString(R.string.aboutContact) + "\n\n"
				+ getString(R.string.aboutChangeLogTitle) + "\n\n"
				+ getString(R.string.aboutChangeLog);

		Builder aboutDialog = new AlertDialog.Builder(this);
		aboutDialog.setTitle(title);
		aboutDialog.setMessage(about);
		aboutDialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}

	private void showNewReleaseDialog() {
		String title = getString(R.string.aboutTitle) + " "
				+ getString(R.string.app_name);
		String about = getString(R.string.app_name) + " v."
				+ getString(R.string.aboutVersionNumber) + "\n"
				+ getString(R.string.about) + "\n\n"				
				+ getString(R.string.aboutNewReleaseTitle) + "\n\n"
				+ getString(R.string.aboutNewReleaseChangeLog);

		Builder aboutDialog = new AlertDialog.Builder(this);
		aboutDialog.setTitle(title);
		aboutDialog.setMessage(about);
		aboutDialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).show();
	}
	
	private void setupDisplayOptionsDialog() {
		ArrayList<String> displayOptionsList = new ArrayList<String>();
		displayOptionsList.add(getString(R.string.displayAutomatic));
		displayOptionsList.add(getString(R.string.displayFractional));
		displayOptionsList.add(getString(R.string.displayDecimal));

		int size = displayOptionsList.size();
		final CharSequence[] displayOptionsCharSequence = displayOptionsList
				.toArray(new CharSequence[size]);
		AlertDialog.Builder displayOptionsDialog = new AlertDialog.Builder(this);
		displayOptionsDialog.setTitle(getString(R.string.menuDisplayOptions)
				+ " (" + displayOptions + ")");
		displayOptionsDialog.setItems(displayOptionsCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = (String) displayOptionsCharSequence[item];

						SharedPreferences.Editor editor = preferences.edit();
						editor = preferences.edit();
						displayOptions = s;
						editor.putString("displayOptions", displayOptions);
						editor.commit();
					}
				});
		AlertDialog alert = displayOptionsDialog.create();
		alert.show();
	}

	private void setupDisplayUnitPreferenceDialog() {
		final CharSequence[] unitCharSequence = UnitType
				.getCharSequenceItemsForDisplay();
		AlertDialog.Builder unitDialog = new AlertDialog.Builder(this);
		unitDialog.setTitle(getString(R.string.menuDisplayDisplayUnit) + " ("
				+ UnitType.getUnitTypeFromId(displayUnit).getAbbreviation()
				+ ")");
		unitDialog.setItems(unitCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = (String) unitCharSequence[item];
						UnitType ut = UnitType.getUnitTypeFromDisplayFormat(s);

						displayUnit = ut.getId();

						SharedPreferences.Editor editor = preferences.edit();
						editor = preferences.edit();
						editor.putInt("displayUnit", displayUnit);
						editor.commit();
					}
				});
		AlertDialog alert = unitDialog.create();
		alert.show();
	}

	private void setupPrecisionPreferenceDialog() {
		ArrayList<String> precision = new ArrayList<String>();
		precision.add("1/1");
		precision.add("1/2");
		precision.add("1/3");
		precision.add("1/4");
		precision.add("1/5");
		precision.add("1/6");
		precision.add("1/7");
		precision.add("1/8");
		precision.add("1/9");
		precision.add("1/10");
		precision.add("1/12");
		precision.add("1/16");
		precision.add("1/32");
		precision.add("1/64");
		precision.add("1/100");
		precision.add("1/1000");
		precision.add("1/10000");
		precision.add("1/100000");
		precision.add("1/1000000");

		int size = precision.size();
		final CharSequence[] precisionCharSequence = precision
				.toArray(new CharSequence[size]);
		AlertDialog.Builder precisionDialog = new AlertDialog.Builder(this);
		precisionDialog.setTitle(getString(R.string.menuPrecision) + " (1/"
				+ String.valueOf(precisionPreference) + ")");
		precisionDialog.setItems(precisionCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						String s = (String) precisionCharSequence[item];
						String denominator = s.split("/")[1];

						SharedPreferences.Editor editor = preferences.edit();
						editor = preferences.edit();
						int precision = Integer.parseInt(denominator);
						precisionPreference = precision;
						editor.putInt("precision", precision);
						editor.commit();
					}
				});
		AlertDialog alert = precisionDialog.create();
		alert.show();
	}

	private void setupRoundUpPreferenceDialog() {
		final CharSequence[] roundingCharSequence = { getString(R.string.up),
				getString(R.string.down) };

		AlertDialog.Builder roundingDialog = new AlertDialog.Builder(this);
		if (roundUpPreference) {
			roundingDialog.setTitle(getString(R.string.menuRounding) + " ("
					+ getString(R.string.up) + ")");
		} else {
			roundingDialog.setTitle(getString(R.string.menuRounding) + " ("
					+ getString(R.string.down) + ")");
		}
		roundingDialog.setItems(roundingCharSequence,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (roundingCharSequence[item] == getString(R.string.up)) {
							roundUpPreference = true;
						} else {
							roundUpPreference = false;
						}

						SharedPreferences.Editor editor = preferences.edit();
						editor = preferences.edit();
						editor.putBoolean("roundUp", roundUpPreference);
						editor.commit();
					}
				});
		AlertDialog alert = roundingDialog.create();
		alert.show();
	}
}