package ch.post.education.apps.rainman;

import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

public abstract class BasicActivity extends AppCompatActivity {

    public abstract void display(JSONObject jsonObject);

}
