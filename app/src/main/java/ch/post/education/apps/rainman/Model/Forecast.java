package ch.post.education.apps.rainman.Model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Forecast {

    private Date receivingTime;
    private Temperature temperature;
    private double pressure;
    private double humidity;
    private Weather weather;
    private Wind wind;
    private int clouds;
    private double rain;

    public Forecast(JSONObject object) throws JSONException{
        receivingTime = new Date(object.getLong("dt"));
        temperature = new Temperature(object.getJSONObject("temp"));
        pressure = object.getDouble("pressure");
        humidity = object.getDouble("humidity");
        weather = new Weather(object.getJSONArray("weather").getJSONObject(0));
        wind = new Wind(object.getDouble("speed"),object.getInt("deg"));
        clouds = object.getInt("clouds");
        rain = object.getDouble("rain");
    }

    public Date getReceivingTime() {
        return receivingTime;
    }

    public Temperature getTemperature() {
        return temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getHumidity() {
        return humidity;
    }

    public Weather getWeather() {
        return weather;
    }

    public Wind getWind() {
        return wind;
    }

    public int getClouds() {
        return clouds;
    }

    public double getRain() {
        return rain;
    }
}
