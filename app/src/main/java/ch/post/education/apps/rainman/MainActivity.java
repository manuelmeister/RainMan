package ch.post.education.apps.rainman;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JSONAsyncTask task = new JSONAsyncTask(this);
        task.execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=thun&mode=json&units=metric&cnt=2");
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
            JSONObject location = jsonObject.getJSONObject("city");
            JSONObject forecast = jsonObject.getJSONArray("list").getJSONObject(0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
