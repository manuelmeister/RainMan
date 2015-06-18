package ch.post.education.apps.rainman;

import android.app.Activity;

import org.json.JSONObject;

public abstract class BasicActivity extends Activity{

    public abstract void display(JSONObject jsonObject);

}
