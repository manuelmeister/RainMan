package ch.post.education.apps.rainman;

import org.json.JSONObject;

public interface BasicActivity {

    /**
     * Gets the jsonObject from the {@link JSONAsyncTask} at {@link JSONAsyncTask#onPostExecute(JSONObject)} and processes the JSONObject and visualizes it
     *
     * @param jsonObject {@link JSONObject}
     */
    void display(JSONObject jsonObject);

}
