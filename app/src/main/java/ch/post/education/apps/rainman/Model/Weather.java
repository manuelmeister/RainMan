package ch.post.education.apps.rainman.Model;

import org.json.JSONException;
import org.json.JSONObject;

public class Weather {

    private int id;
    private String main;
    private String description;

    public Weather(JSONObject object) throws JSONException {
        id = object.getInt("id");
        main = object.getString("main");
        description = object.getString("description");
    }

    public int getId() {
        return id;
    }

    public String getMain() {
        return main;
    }

    public String getDescription() {
        return description;
    }
}
