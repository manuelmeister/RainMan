package ch.post.education.apps.rainman.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Coordinates {

    private double lon;
    private double lat;

    public Coordinates(JSONObject object) throws JSONException {
        lon = object.getDouble("lon");
        lat = object.getDouble("lat");
    }

    public double getLon() {
        return lon;
    }

    public double getLat() {
        return lat;
    }
}
