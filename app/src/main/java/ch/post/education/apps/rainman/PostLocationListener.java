package ch.post.education.apps.rainman;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Custom Listener to give the context
 */
public class PostLocationListener implements LocationListener {

    public Context context;

    public PostLocationListener(Context context) {
        this.context = context;
    }

    /**
     * Triggers if the location changes
     * @param location Location
     */
    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Triggers if GPS has been turned off
     * @param provider String
     */
    @Override
    public void onProviderDisabled(String provider) {

    }
}
