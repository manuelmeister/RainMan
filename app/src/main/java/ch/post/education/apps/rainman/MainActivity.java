package ch.post.education.apps.rainman;

import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
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
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=Vishakhapatnam&mode=json&units=metric&cnt=2");
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
            int bar_rain_height = getHeight(forecast.getRain() * 5);
            expand(bar_rain,bar_rain_height);

            TextView bar_rain_value = (TextView) findViewById(R.id.bar_rain_value);
            if(forecast.getRain() != 0){
                bar_rain_value.setText(String.valueOf(forecast.getRain()) + " mm");
            }else {
                bar_rain_value.setText(R.string.no_rain);
            }

            FrameLayout bar_pressure = (FrameLayout) findViewById(R.id.bar_pressure);
            ViewGroup.LayoutParams bar_pressure_layout = bar_pressure.getLayoutParams();
            int bar_pressure_height = getHeight(forecast.getPressure() / 6);
            expand(bar_pressure,bar_pressure_height);

            TextView bar_pressure_value = (TextView) findViewById(R.id.bar_pressure_value);
            bar_pressure_value.setText(String.valueOf(forecast.getPressure()) + " hPa");

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            ViewGroup.LayoutParams bar_temperature_layout = bar_temperature.getLayoutParams();
            int bar_temperature_height = getHeight(forecast.getTemperature().getDay() * 10);
            expand(bar_temperature, bar_temperature_height,forecast.getTemperature().getDay());

            TextView bar_temperature_value = (TextView) findViewById(R.id.bar_temperature_value);
            bar_temperature_value.setText(String.valueOf(forecast.getTemperature().getDay()) + " °C");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getHeight(double height){
        float pixels = (float)((height >= 60) ? height : 60);
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics()));
    }

    public int setBGColor(double temp){
        int color;
        if(temp > 40){
            color = R.color.temperature_background_really_hot;
        }else if(temp > 35){
            color = R.color.temperature_background_hot;
        }else if(temp > 30){
            color = R.color.temperature_background_quite_warm;
        }else if(temp > 25){
            color = R.color.temperature_background_warm;
        }else if(temp > 21){
            color = R.color.temperature_background_quite_comfy;
        }else if(temp > 18){
            color = R.color.temperature_background_comfy;
        }else if(temp > 16){
            color = R.color.temperature_background_quite_normal;
        }else if(temp > 10){
            color = R.color.temperature_background_normal;
        }else if(temp > 5){
            color = R.color.temperature_background_quite_cold;
        }else if(temp > 1){
            color = R.color.temperature_background_cold;
        }else if(temp > -1){
            color = R.color.temperature_background_quite_freezing;
        }else {
            color = R.color.temperature_background_freezing;
        }
        return getResources().getColor(color);
    }

    public static void expand(final View v, final int targetHeight) {
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)*3);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);
        v.requestLayout();
    }

    public void expand(final View v, final int targetHeight,final double temp) {
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (int)(targetHeight * interpolatedTime);
                v.setBackgroundColor(setBGColor(temp * interpolatedTime));
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)*3);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);
        v.requestLayout();
    }
}
