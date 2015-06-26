package ch.post.education.apps.rainman.Model;

import org.json.JSONException;
import org.json.JSONObject;

import ch.post.education.apps.rainman.Exception.AttributeNotFoundException;

public class Location {

    private int id;
    private String name;
    private Coordinates coordinates;
    private String country;
    private int population;

    public Location(JSONObject object) throws JSONException{
        id = object.getInt("id");
        name = object.getString("name");
        coordinates = new Coordinates(object.getJSONObject("coord"));
        country = object.getString("country");
        population = object.getInt("population");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String getCountry() {
        return country;
    }

    public int getPopulation() {
        return population;
    }
}
