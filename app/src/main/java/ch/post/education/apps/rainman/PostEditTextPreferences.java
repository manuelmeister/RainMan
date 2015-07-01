package ch.post.education.apps.rainman;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

public class PostEditTextPreferences extends EditTextPreference {
    public PostEditTextPreferences(Context context) {
        super(context);
    }

    public PostEditTextPreferences(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PostEditTextPreferences(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PostEditTextPreferences(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void show(){
        showDialog(null);
    }
}
