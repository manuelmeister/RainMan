package ch.post.education.apps.rainman;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
     * Creates the view and adds the locationListener
     * @param savedInstanceState SavedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences("RainMan", MODE_PRIVATE);
        editor = settings.edit();

        this.locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        this.locationListener = new PostLocationListener(this) {

            @Override
            public void onLocationChanged(android.location.Location location) {
                query = "lat=" + location.getLatitude() + "&lon=" + location.getLongitude();
                runTask();
            }

            @Override
            public void onProviderDisabled(String provider) {
                if (settings.getBoolean("useGPS", true)) {
                    locationManager.removeUpdates(locationListener);
                    AlertDialog.Builder elem = new AlertDialog.Builder(context);
                    //noGPS.setIcon();//TODO: create no position icon
                    elem.setTitle(getResources().getString(R.string.Error));
                    elem.setMessage(getResources().getString(R.string.error_gps_disabled));
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

    /**
     * Removes the locationListener from the System service
     */
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    /**
     * If the activity is activated, the spinner gets displayed and the location is searched
     */
    @Override
    protected void onResume() {
        super.onResume();
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
        swipeRefreshLayout.setRefreshing(true);
        getLocation();
    }

    /**
     * Either gets the location via GPS or the manual settings
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
     * Runs the network task
     */
    public void runTask() {
        if(isNetworkConnected()){
            JSONAsyncTask task = new JSONAsyncTask(this);
            task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?" + query + "&mode=json&units=metric&cnt=10");
            //TODO: Put this to the beginning
            locationManager.removeUpdates(locationListener);
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            locationManager.removeUpdates(locationListener);
            SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.Swipe);
            swipeRefreshLayout.setRefreshing(false);
            showError(getResources().getString(R.string.Error), getResources().getString(R.string.error_no_internet), getResources().getString(R.string.error_retry));
        }

    }

    /**
     * Creates menu
     * @param menu Menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Menu Action Handler
     * @param item MenuItem
     * @return boolean
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
     * Sets the bars according to the JSONObject
     * Description at {@link ch.post.education.apps.rainman.BasicActivity#display(JSONObject)}
     * @param jsonObject JSONObject
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
                frameLayoutHelper(R.id.bar_rain, getPXHeight(h), R.color.rain_background);
            } else {
                rainbarText = getResources().getString(R.string.no_rain);
                frameLayoutHelper(R.id.bar_rain, getPXHeight(h), R.color.rain_background_fail);
            }
            textViewHelper(R.id.bar_rain_value, rainbarText, View.VISIBLE);

            frameLayoutHelper(R.id.bar_pressure, getPXHeight(forecast.getPressure() / 5), R.color.pressure_background);

            textViewHelper(R.id.bar_pressure_value, String.valueOf(forecast.getPressure()) + " hPa", View.VISIBLE);

            FrameLayout bar_temperature = (FrameLayout) findViewById(R.id.bar_temperature);
            int bar_temperature_height = getPXHeight(forecast.getTemperature().getDay() * 10);
            TextView bar_temperature_value = (TextView) findViewById(R.id.bar_temperature_value);
            TextView bar_temperature_title = (TextView) findViewById(R.id.bar_temperature_title);
            expand(bar_temperature, bar_temperature_value, bar_temperature_title, bar_temperature.getHeight(), bar_temperature_height, forecast.getTemperature().getDay());

            textViewHelper(R.id.bar_temperature_value, String.valueOf(forecast.getTemperature().getDay()) + " °C", View.VISIBLE);

            ImageView weather_icon = (ImageView) findViewById(R.id.weather_icon);
            weather_icon.setImageDrawable(getResources().getDrawable(getWeatherIcon(forecast.getWeather().getMain())));

        } catch (Exception e) {
            //TODO: use Resource files
            showError("Error", "No data", e.getMessage());
        }
    }

    /**
     * Converts dip to px
     * @param dip double
     * @return int
     */
    public int getPXHeight(double dip) {
        float pixels = (float) ((dip >= 60) ? dip : 60);
        return (int) (pixels * getResources().getDisplayMetrics().density);
    }

    /**
     * Converts px to dip
     * @param px float
     * @return int
     */
    public int getDIPHeight(float px) {
        return (int) (px / getResources().getDisplayMetrics().density);
    }

    /**
     * Gets the weather icon according to the String given
     * @param weather String
     * @return int R.drawable resource
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
     * Displays the error using the weather bars
     * @param category String
     * @param title String
     * @param message String
     */
    public void showError(String category, String title, String message) {
        locationManager.removeUpdates(locationListener);
        Point dimen = new Point();
        getWindowManager().getDefaultDisplay().getSize(dimen);
        int height = 100;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            height = (int) (dimen.x / 2.5);
        }else {
            height = (int) (dimen.y / 2.5);
        }

        frameLayoutHelper(R.id.bar_rain, height, R.color.error);
        textViewHelper(R.id.bar_rain_value, "", View.INVISIBLE);

        frameLayoutHelper(R.id.bar_pressure, height, R.color.error);
        textViewHelper(R.id.bar_pressure_value, "", View.INVISIBLE);

        frameLayoutHelper(R.id.bar_temperature, height, R.color.error);
        textViewHelper(R.id.bar_temperature_value, "", View.INVISIBLE);

        textViewHelper(R.id.location, category, View.VISIBLE);
        textViewHelper(R.id.ErrorTitle, title, View.VISIBLE);
        textViewHelper(R.id.message, message, View.VISIBLE);

        TextClock time = (TextClock) findViewById(R.id.time);
        time.setVisibility(View.INVISIBLE);
        time.requestLayout();
    }

    /**
     * Gets the color according to the temperature
     * @param temp double
     * @return int
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
     * @param v View
     * @param initalHeight int
     * @param targetHeight int
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
     * Enlarges a view from its initalHeight to the targetHeight and gives the temperature
     * @param v View
     * @param bar_value TextView
     * @param bar_title TextView
     * @param initalHeight int
     * @param targetHeight int
     * @param temp double
     */
    public void expand(final View v, final TextView bar_value, final TextView bar_title, final int initalHeight, final int targetHeight, final double temp) {
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
        int contrastTextColor = getContrastTextColor(colorTo);
        bar_value.setTextColor(contrastTextColor);
        bar_title.setTextColor(contrastTextColor);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 3);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int color = (Integer) animator.getAnimatedValue();
                v.setBackgroundColor(color);
            }
        });
        colorAnimation.start();
        v.requestLayout();
    }

    /**
     * Gets the contrast color of the given backgroundColor
     * @param backgroundColor Integer
     * @return int contrastTextColor
     */
    public int getContrastTextColor(Integer backgroundColor) {
        int color = (int) Long.parseLong(backgroundColor.toString(), 16);
        return (((((color >> 16) & 0xFF) * 0.299 + ((color >> 8) & 0xFF) * 0.58 + ((color) & 0xFF) * 0.114) > 186) ? Color.BLACK : Color.WHITE);
    }

    /**
     * Sets the text and visibility of a TextView
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
     * Sets the color and the height of a frameLayout
     * @param elem                   Target element {@link #onCreate}
     * @param desired_element_height Height of the bar
     * @param color                  Color
     */
    private void frameLayoutHelper(int elem, int desired_element_height, int color) {
        FrameLayout element = (FrameLayout) findViewById(elem);
        int inital_element_height = element.getLayoutParams().height;
        element.setBackgroundColor(getResources().getColor(color));
        expand(element, inital_element_height, desired_element_height);
    }

    /**
     * Checks if you are connected to the internet
     * @return boolean
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }
}
