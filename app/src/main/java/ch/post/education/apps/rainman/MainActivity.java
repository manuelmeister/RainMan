package ch.post.education.apps.rainman;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import org.json.JSONObject;

import ch.post.education.apps.rainman.Model.Forecast;
import ch.post.education.apps.rainman.Model.Location;

public class MainActivity extends AppCompatActivity implements BasicActivity {

    private LocationManager locationManager;
    private LocationListener locationListener;

    public String query = "";

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    /**
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("RainMan", MODE_PRIVATE);
        editor = settings.edit();

        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(android.location.Location location) {
                query = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
                runTask();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                if (settings.getBoolean("useGPS", true)) {
                    locationManager.removeUpdates(locationListener);
                    AlertDialog.Builder elem = new AlertDialog.Builder(getApplicationContext());
                    //noGPS.setIcon();//TODO: create no position icon
                    elem.setTitle(getResources().getString(R.string.Error));
                    elem.setMessage(getResources().getString( R.string.error_gps_disabled));
                    elem.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            editor.putBoolean("useGPS", false).apply();
                            getLocation();
                        }
                    });
                    elem.show();
                }
            }


        };

        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setProgressViewEndTarget(false, (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 58, getResources().getDisplayMetrics())));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                getLocation();
            }
        });
        if (this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }

    }

    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setRefreshing(true);
        getLocation();
    }

    /**
     *
     */
    private void getLocation() {
        if (settings.getBoolean("useGPS", true)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            //2661552 is the openweathermap.org ID for Berne, CH and is used as default value
            query = "id=" + settings.getString("locationID", "2661552");
            runTask();
        }
    }

    /**
     *
     */
    public void runTask() {
        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?" + query + "&mode=json&units=metric&cnt=10");
        locationManager.removeUpdates(locationListener);
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @param jsonObject
     */
    @Override
    public void display(JSONObject jsonObject) {
        try {
            textViewHelper(R.id.ErrorTitle, "", View.INVISIBLE);
            textViewHelper(R.id.message, "", View.INVISIBLE);

            TextClock time = (TextClock) findViewById(R.id.time);
            time.setVisibility(View.VISIBLE);
            time.requestLayout();

            Location location = new Location(jsonObject.getJSONObject("city"));
            Forecast forecast = new Forecast(jsonObject.getJSONArray("list").getJSONObject(0));

            TextView text_location = (TextView) findViewById(R.id.location);
            text_location.setText(location.getName());

            double x = forecast.getRain();
            int h = (int) ((2 * (Math.pow(x, 2))) / (Math.pow(x, 2) + 1) * 100);

            String rainbarText;
            if (forecast.getRain() != 0) {
                rainbarText = String.valueOf(forecast.getRain()) + " mm";
                frameLayoutHelper(R.id.bar_rain, getPXHeight(h), R.color.rain_background, false);
            } else {
                rainbarText = getResources().getString(R.string.no_rain);
                frameLayoutHelper(R.id.bar_rain, getPXHeight(h), R.color.rain_background_fail, false);
            }
            textViewHelper(R.id.bar_rain_value, rainbarText, View.VISIBLE);

            frameLayoutHelper(R.id.bar_pressure, getPXHeight(forecast.getPressure() / 5), R.color.pressure_background, false);

            textViewHelper(R.id.bar_pressure_value, String.valueOf(forecast.getPressure()) + " hPa", View.VISIBLE);

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            int bar_temperature_height = getPXHeight(forecast.getTemperature().getDay() * 10);
            expand(bar_temperature, bar_temperature.getHeight(), bar_temperature_height, forecast.getTemperature().getDay());

            textViewHelper(R.id.bar_temperature_value, String.valueOf(forecast.getTemperature().getDay()) + " °C", View.VISIBLE);

            ImageView weather_icon = (ImageView) findViewById(R.id.weather_icon);
            weather_icon.setImageDrawable(getResources().getDrawable(getWeatherIcon(forecast.getWeather().getMain())));

        } catch (Exception e) {
            //TODO: use Resource files
            showError("Error", "No data", e.getMessage());
        }
    }

    /**
     * @param height
     * @return
     */
    public int getPXHeight(double height) {
        float pixels = (float) ((height >= 60) ? height : 60);
        return (int) (pixels * getResources().getDisplayMetrics().density);
    }

    /**
     * @param height
     * @return
     */
    public int getDIPHeight(float height) {
        return (int) (height / getResources().getDisplayMetrics().density);
    }

    /**
     * @param weather
     * @return
     */
    public int getWeatherIcon(String weather) {
        int msg;
        Log.v("Weather", weather);
        if (weather.equals("Clear")) {
            msg = R.drawable.weather_sun;
        } else if (weather.equals("Clouds")) {
            msg = R.drawable.weather_clouds;
        } else if (weather.equals("Shower Rain")) {
            msg = R.drawable.weather_rain;
        } else if (weather.equals("Rain")) {
            msg = R.drawable.weather_rainy_weather;
        } else if (weather.equals("Thunderstorm")) {
            msg = R.drawable.weather_storm;
        } else if (weather.equals("Snow")) {
            msg = R.drawable.weather_snow;
        } else if (weather.equals("Mist")) {
            msg = R.drawable.weather_fog_day;
        } else {
            msg = R.drawable.weather_sun;
        }
        return msg;
    }

    /**
     * @param category
     * @param message
     */
    public void showError(String category, String title,String message) {
        locationManager.removeUpdates(locationListener);
        Point dimen = new Point();
        getWindowManager().getDefaultDisplay().getSize(dimen);
        int height = (int) (dimen.x / 2.5);

        frameLayoutHelper(R.id.bar_rain, height, R.color.error, false);
        textViewHelper(R.id.bar_rain_value, "", View.INVISIBLE);

        frameLayoutHelper(R.id.bar_pressure, height, R.color.error, false);
        textViewHelper(R.id.bar_pressure_value, "", View.INVISIBLE);

        frameLayoutHelper(R.id.bar_temperature, height, R.color.error, false);
        textViewHelper(R.id.bar_temperature_value, "", View.INVISIBLE);

        textViewHelper(R.id.location, category, View.VISIBLE);
        textViewHelper(R.id.ErrorTitle, title, View.VISIBLE);
        textViewHelper(R.id.message, message, View.VISIBLE);

        TextClock time = (TextClock) findViewById(R.id.time);
        time.setVisibility(View.INVISIBLE);
        time.requestLayout();
    }

    /**
     * @param temp
     * @return
     */
    public int getBGColor(double temp) {
        int color;
        if (temp > 40) {
            color = R.color.temperature_background_really_hot;
        } else if (temp > 35) {
            color = R.color.temperature_background_hot;
        } else if (temp > 30) {
            color = R.color.temperature_background_quite_warm;
        } else if (temp > 25) {
            color = R.color.temperature_background_warm;
        } else if (temp > 21) {
            color = R.color.temperature_background_quite_comfy;
        } else if (temp > 18) {
            color = R.color.temperature_background_comfy;
        } else if (temp > 16) {
            color = R.color.temperature_background_quite_normal;
        } else if (temp > 10) {
            color = R.color.temperature_background_normal;
        } else if (temp > 5) {
            color = R.color.temperature_background_quite_cold;
        } else if (temp > 1) {
            color = R.color.temperature_background_cold;
        } else if (temp > -1) {
            color = R.color.temperature_background_quite_freezing;
        } else {
            color = R.color.temperature_background_freezing;
        }
        return getResources().getColor(color);
    }

    /**
     * @param v
     * @param initalHeight
     * @param targetHeight
     */
    public static void expand(final View v, final int initalHeight, final int targetHeight) {
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (int) (initalHeight + (targetHeight - initalHeight) * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);
        v.requestLayout();
    }

    /**
     * @param v
     * @param initalHeight
     * @param targetHeight
     * @param temp
     */
    public void expand(final View v, final int initalHeight, final int targetHeight, final double temp) {
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = (int) (initalHeight + (targetHeight - initalHeight) * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        a.setInterpolator(new AccelerateDecelerateInterpolator());
        v.startAnimation(a);

        Integer colorFrom = getBGColor(getDIPHeight(initalHeight) / 10);
        Integer colorTo = getBGColor(temp);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                v.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
        v.requestLayout();
    }

    /**
     * @param elem    Target element {@link #onCreate}
     * @param text    Content of the value
     * @param visible One of {@link android.view.View#VISIBLE}, {@link android.view.View#INVISIBLE}, or {@link android.view.View#GONE}.
     */
    private void textViewHelper(int elem, String text, int visible) {
        TextView element = (TextView) findViewById(elem);
        element.setText(text);
        element.setVisibility(visible);
    }

    /**
     * @param elem                   Target element {@link #onCreate}
     * @param desired_element_height Height of the bar
     * @param color                  Color
     * @param error                  If the frame transitions to an error
     */
    private void frameLayoutHelper(int elem, int desired_element_height, int color, boolean error) {
        FrameLayout element = (FrameLayout) findViewById(elem);
        int inital_element_height = element.getLayoutParams().height;
        element.setBackgroundColor(getResources().getColor(color));
        expand(element, inital_element_height, desired_element_height);
    }
}
