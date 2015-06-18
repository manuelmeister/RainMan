package ch.post.education.apps.rainman;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ch.post.education.apps.rainman.Model.Forecast;
import ch.post.education.apps.rainman.Model.Location;


public class MainActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=thun&mode=json&units=metric&cnt=2");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void display(JSONObject jsonObject) {
        try {
            Location location = new Location(jsonObject.getJSONObject("city"));
            Forecast forecast = new Forecast(jsonObject.getJSONArray("list").getJSONObject(0));

            TextView text_location = (TextView) findViewById(R.id.location);
            text_location.setText(location.getName());

            FrameLayout bar_rain = (FrameLayout) findViewById(R.id.bar_rain);
            ViewGroup.LayoutParams bar_rain_layout = bar_rain.getLayoutParams();
            bar_rain.getLayoutParams().height = (int)(10 * forecast.getRain());

            TextView bar_rain_value = (TextView) findViewById(R.id.bar_rain_value);
            bar_rain_value.setText(String.valueOf(forecast.getRain()) + " mm");

            FrameLayout bar_pressure = (FrameLayout) findViewById(R.id.bar_pressure);
            ViewGroup.LayoutParams bar_pressure_layout = bar_rain.getLayoutParams();
            bar_pressure_layout.height = (int)( forecast.getPressure()/5);

            TextView bar_pressure_value = (TextView) findViewById(R.id.bar_pressure_value);
            bar_pressure_value.setText(String.valueOf(forecast.getPressure()) + " hPa");

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            ViewGroup.LayoutParams bar_temperature_layout = bar_rain.getLayoutParams();
            bar_temperature_layout.height = (int)(100 * forecast.getTemperature().getDay());

            TextView bar_temperature_value = (TextView) findViewById(R.id.bar_temperature_value);
            bar_temperature_value.setText(String.valueOf(forecast.getTemperature().getDay()) + " °C");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
