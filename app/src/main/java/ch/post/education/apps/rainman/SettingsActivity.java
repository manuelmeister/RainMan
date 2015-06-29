package ch.post.education.apps.rainman;

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

    HashMap<String,String> locations = new HashMap<String, String>();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_location);
        prefLocationAuto();
        prefLocationManually();
        //setupSimplePreferencesScreen();
    }

    private void prefLocationAuto(){
        catManually = (PreferenceCategory) findPreference(getResources().getString(R.string.prefManually));

        CheckBoxPreference useGPS = (CheckBoxPreference) findPreference(getResources().getString(R.string.prefUseGPS));
        useGPS.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                catManually.setEnabled(!((boolean) newValue));
                return true;
            }
        });

    }

    private void prefLocationManually(){

        EditTextPreference searchLocation = (EditTextPreference) findPreference(getResources().getString(R.string.prefinput_Location));
        searchLocation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                getSuggestions(newValue.toString());
                return false;
            }
        });

        locationList = (ListPreference) findPreference(getResources().getString(R.string.prefLocation));

        locations.put("Bern, CH", "2661552");
        locations.put("London, GB", "2643743");
        locations.put("Stockholm, SE","2673730");

        CharSequence[] keys = locations.keySet().toArray(new CharSequence[locations.size()]);
        CharSequence[] values = locations.values().toArray(new CharSequence[locations.size()]);

        locationList.setEntries(keys);
        locationList.setEntryValues(values);
        locationList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                for (Map.Entry<String, String> location : locations.entrySet()) {
                    if (location.getValue().equals(newValue)) {
                        locationList.setTitle(location.getKey());
                        break;
                    }
                }
                return true;
            }

        });
        locationList.setValueIndex(0);
        locationList.setTitle(locationList.getEntry());
    }

    private void getSuggestions(String value) {
        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/find?mode=json&type=like&q=" + value);
    }

    @Override
    public void display(JSONObject jsonObject) {
        try{
            JSONArray list = jsonObject.getJSONArray("list");
            int array_length = list.length();

            locations.clear();
            for(int i = 0; i < array_length; i++){
                JSONObject location = list.getJSONObject(i);
                locations.put(location.getString("name") + ", " + location.getJSONObject("sys").getString("country"), location.getString("id"));
            }

            CharSequence[] keys = locations.keySet().toArray(new CharSequence[locations.size()]);
            CharSequence[] values = locations.values().toArray(new CharSequence[locations.size()]);

            locationList.setEntries(keys);
            locationList.setEntryValues(values);
            locationList.setValueIndex(0);
            locationList.setTitle(locationList.getEntry());

        }catch (Exception e){
            Log.v("Search",e.getMessage());
        }
    }
}
