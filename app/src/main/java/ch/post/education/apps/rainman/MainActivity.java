package ch.post.education.apps.rainman;

import android.os.Bundle;
import android.util.TypedValue;
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
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=Alaghsas&mode=json&units=metric&cnt=2");
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
            bar_rain_layout.height = setHeight(forecast.getRain() * 5);

            TextView bar_rain_value = (TextView) findViewById(R.id.bar_rain_value);
            if(forecast.getRain() != 0){
                bar_rain_value.setText(String.valueOf(forecast.getRain()) + " mm");
            }else {
                bar_rain_value.setText(R.string.no_rain);
            }

            FrameLayout bar_pressure = (FrameLayout) findViewById(R.id.bar_pressure);
            ViewGroup.LayoutParams bar_pressure_layout = bar_pressure.getLayoutParams();
            bar_pressure_layout.height = setHeight(forecast.getPressure()/6);

            TextView bar_pressure_value = (TextView) findViewById(R.id.bar_pressure_value);
            bar_pressure_value.setText(String.valueOf(forecast.getPressure()) + " hPa");

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            ViewGroup.LayoutParams bar_temperature_layout = bar_temperature.getLayoutParams();
            bar_temperature_layout.height = setHeight(forecast.getTemperature().getDay()*10);
            bar_temperature.setBackgroundColor(setBGColor(forecast.getTemperature().getDay()));

            TextView bar_temperature_value = (TextView) findViewById(R.id.bar_temperature_value);
            bar_temperature_value.setText(String.valueOf(forecast.getTemperature().getDay()) + " °C");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int setHeight(double height){
        float pixels = (float)((height >= 60) ? height : 60);
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics()));
    }

    public int setBGColor(double temp){
        int color;
        if(temp > 35){
            color = R.color.temperature_background_hot;
        }else if(temp > 30){
            color = R.color.temperature_background_warm;
        }else if(temp > 21){
            color = R.color.temperature_background_comfy;
        }else if(temp > 16){
            color = R.color.temperature_background_normal;
        }else if(temp > 1){
            color = R.color.temperature_background_cold;
        }else {
            color = R.color.temperature_background_freezing;
        }
        return getResources().getColor(color);
    }
}
