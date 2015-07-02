package ch.post.education.apps.rainman;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;

public class JSONAsyncTask extends AsyncTask<String,Integer,JSONObject> {

    private BasicActivity activity;

    public JSONAsyncTask(BasicActivity activity) {
        this.activity = activity;
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject result = null;

        try {
            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();
            String msg = IOUtils.toString(connection.getInputStream());
            result = new JSONObject(msg);
        } catch (Exception e) {
            Log.v("Connection",e.getMessage());
        }


        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        activity.display(jsonObject);
    }
}