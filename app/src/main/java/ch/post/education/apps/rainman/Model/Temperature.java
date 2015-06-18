package ch.post.education.apps.rainman.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Temperature {

    private double day;
    private double min;
    private double max;
    private double night;
    private double evening;
    private double morning;

    public Temperature(JSONObject object) throws JSONException{
        day = object.getDouble("day");
        min = object.getDouble("min");
        max = object.getDouble("max");
        night = object.getDouble("night");
        evening = object.getDouble("eve");
        morning = object.getDouble("morn");
    }

    public double getDay() {
        return day;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getNight() {
        return night;
    }

    public double getEvening() {
        return evening;
    }

    public double getMorning() {
        return morning;
    }
}
