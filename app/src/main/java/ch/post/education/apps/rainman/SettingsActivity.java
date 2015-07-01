package ch.post.education.apps.rainman;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity implements BasicActivity {

    PreferenceCategory catManually;
    ListPreference locationList;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    ProgressDialog dialog;

    HashMap<String, String> locations = new HashMap<String, String>();

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        dialog = new ProgressDialog(this);
        settings = getSharedPreferences("RainMan", MODE_PRIVATE);
        editor = settings.edit();
        addPreferencesFromResource(R.xml.pref_location);
        prefLocationAuto();
        prefLocationManually();
        //setupSimplePreferencesScreen();
    }

    /**
     *
     */
    private void prefLocationAuto() {
        catManually = (PreferenceCategory) findPreference(getResources().getString(R.string.prefManually));

        catManually.setEnabled(!settings.getBoolean("useGPS", true));

        CheckBoxPreference useGPS = (CheckBoxPreference) findPreference(getResources().getString(R.string.prefUseGPS));
        useGPS.setChecked(settings.getBoolean("useGPS", true));
        useGPS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                catManually.setEnabled(!((boolean) newValue));
                editor.putBoolean("useGPS", (boolean) newValue).apply();
                return true;
            }
        });

    }

    /**
     *
     */
    private void prefLocationManually() {

        EditTextPreference searchLocation = (EditTextPreference) findPreference(getResources().getString(R.string.prefinput_Location));
        searchLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getSuggestions(newValue.toString());
                return false;
            }
        });

        locationList = (ListPreference) findPreference(getResources().getString(R.string.prefLocation));

        locations.put(settings.getString("locationID", "2661552"), settings.getString("locationName", "Bern, CH"));

        CharSequence[] keys = locations.keySet().toArray(new CharSequence[locations.size()]);
        CharSequence[] values = locations.values().toArray(new CharSequence[locations.size()]);

        locationList.setEntries(values);
        locationList.setEntryValues(keys);
        locationList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                for (Map.Entry<String, String> location : locations.entrySet()) {
                    if (location.getKey().equals(newValue)) {
                        locationList.setTitle(location.getValue());
                        editor.putString("locationID", location.getKey()).apply();
                        editor.putString("locationName", location.getValue()).apply();
                        break;
                    }
                }
                return true;
            }

        });
        locationList.setValueIndex(0);
        locationList.setTitle(locationList.getEntry());
    }


    /**
     * @param value
     */
    private void getSuggestions(String value) {
        dialog.show();
        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/find?mode=json&type=like&q=" + value);
    }

    /**
     * @param jsonObject {@link JSONObject}
     */
    @Override
    public void display(JSONObject jsonObject) {
        try {
            JSONArray list = jsonObject.getJSONArray("list");
            int array_length = list.length();

            if(array_length == 0){
                dialog.dismiss();
                AlertDialog.Builder hint = new AlertDialog.Builder(this);
                hint.setMessage("no citys found"); //todo use sting ressources
                hint.setTitle("Error");
                hint.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        PostEditTextPreferences editText = (PostEditTextPreferences) findPreference(getResources().getString(R.string.prefinput_Location));
                        editText.show();
                    }
                });
                hint.show();
            } else {
                locations.clear();
                for (int i = 0; i < array_length; i++) {
                    JSONObject location = list.getJSONObject(i);
                    locations.put(location.getString("id"), location.getString("name") + ", " + location.getJSONObject("sys").getString("country"));
                }

                CharSequence[] keys = locations.values().toArray(new CharSequence[locations.size()]);
                CharSequence[] values = locations.keySet().toArray(new CharSequence[locations.size()]);

                locationList.setEntries(keys);
                locationList.setEntryValues(values);
                locationList.setValueIndex(0);
                editor.putString("locationID", locationList.getValue()).apply();
                editor.putString("locationName", locationList.getEntry().toString()).apply();
                locationList.setTitle(locationList.getEntry());
                dialog.dismiss();
            }

        } catch (Exception e) {
            Log.v("Search", e.getMessage());
        }
    }
}
