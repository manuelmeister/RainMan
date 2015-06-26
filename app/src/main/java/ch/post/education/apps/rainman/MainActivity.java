package ch.post.education.apps.rainman;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ch.post.education.apps.rainman.Model.Coordinates;
import ch.post.education.apps.rainman.Model.Forecast;
import ch.post.education.apps.rainman.Model.Location;

public class MainActivity extends BasicActivity {

    public Coordinates cords;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(android.location.Location location) {
                cords = new Coordinates(location.getLongitude(), location.getLatitude());
                runTask();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}


        };

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                getLocation();
            }
        });
        
        try{
            getLocation(this);
            showError("Error", "not found");
        }catch (Exception e){
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try{
            getLocation();
            showError("Error", "not found");
        }catch (Exception e){}

    }

    private void getLocation() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public void runTask(){
        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + cords.getLat() + "&lon=" + cords.getLon() + "&mode=json&units=metric&cnt=2");
        locationManager.removeUpdates(locationListener);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setRefreshing(false);
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
            int bar_rain_height = getHeight(forecast.getRain() * 5);
            expand(bar_rain,bar_rain_height);

            TextView bar_rain_value = (TextView) findViewById(R.id.bar_rain_value);
            if(forecast.getRain() != 0){
                bar_rain_value.setText(String.valueOf(forecast.getRain()) + " mm");
            }else {
                bar_rain_value.setText(R.string.no_rain);
            }

            FrameLayout bar_pressure = (FrameLayout) findViewById(R.id.bar_pressure);
            int bar_pressure_height = getHeight(forecast.getPressure() / 6);
            expand(bar_pressure, bar_pressure_height);

            TextView bar_pressure_value = (TextView) findViewById(R.id.bar_pressure_value);
            bar_pressure_value.setText(String.valueOf(forecast.getPressure()) + " hPa");

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            int bar_temperature_height = getHeight(forecast.getTemperature().getDay() * 10);
            expand(bar_temperature, bar_temperature_height, forecast.getTemperature().getDay());

            TextView bar_temperature_value = (TextView) findViewById(R.id.bar_temperature_value);
            bar_temperature_value.setText(String.valueOf(forecast.getTemperature().getDay()) + " °C");

            ImageView weather_icon = (ImageView) findViewById(R.id.weather_icon);
            weather_icon.setImageDrawable(getResources().getDrawable(getWeatherIcon(forecast.getWeather().getMain())));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getHeight(double height){
        float pixels = (float)((height >= 60) ? height : 60);
        return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics()));
    }

    public int getWeatherIcon(String weather){
        int msg = 0;
        switch (weather){
            case "clear sky":
                msg = R.drawable.weather_sun;
                break;
            case "few clouds":
                msg = R.drawable.weather_partly_cloudy_day;
                break;
            case "scattered clouds":
                msg = R.drawable.weather_clouds;
                break;
            case "broken clouds":
                msg = R.drawable.weather_clouds;
                break;
            case "shower rain":
                msg = R.drawable.weather_rain;
                break;
            case "rain":
                msg = R.drawable.weather_rainy_weather;
                break;
            case "thunderstorm":
                msg = R.drawable.weather_storm;
                break;
            case "snow":
                msg = R.drawable.weather_snow;
                break;
            case "mist":
                msg = R.drawable.weather_fog_day;
                break;
            default:
                msg = R.drawable.weather_sun;
                break;
        }
        return msg;
    }

    public void showError(String title, String message){
        FrameLayout bar_rain = (FrameLayout) findViewById(R.id.bar_rain);
        int bar_rain_height = getHeight(300);
        expand(bar_rain, bar_rain_height);
        bar_rain.setBackgroundColor(getResources().getColor(R.color.error));

        FrameLayout bar_pressure = (FrameLayout) findViewById(R.id.bar_pressure);
        int bar_pressure_height = getHeight(300);
        expand(bar_pressure, bar_pressure_height);
        bar_pressure.setBackgroundColor(getResources().getColor(R.color.error));

        FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
        int bar_temperature_height = getHeight(300);
        expand(bar_temperature, bar_temperature_height);
        bar_temperature.setBackgroundColor(getResources().getColor(R.color.error));

        TextView title_message = (TextView) findViewById(R.id.title);
        title_message.setText(title);
        title_message.setVisibility(View.VISIBLE);

        TextView message_message = (TextView) findViewById(R.id.message);
        message_message.setText(message);
        message_message.setVisibility(View.VISIBLE);

        TextClock time = (TextClock) findViewById(R.id.time);
        time.setVisibility(View.INVISIBLE);
        time.requestLayout();
    }

    public int getBGColor(double temp){
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
                v.getLayoutParams().height = (int) (targetHeight * interpolatedTime);
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

        Integer colorFrom = getBGColor(targetHeight*0.002);
        Integer colorMiddle = getBGColor(targetHeight * 0.03);
        Integer colorTo = getBGColor(targetHeight * 0.05);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom,colorMiddle, colorTo);
        colorAnimation.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density)*3);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                v.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
        v.requestLayout();
    }
}
