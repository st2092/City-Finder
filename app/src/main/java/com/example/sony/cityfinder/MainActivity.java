package com.example.sony.cityfinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Scanner;


public class MainActivity extends ActionBarActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private LatLng my_location;

    /*
     * This method gets call at the start to set up the application. Specifically,
     * this method starts the map when it is ready.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment map_fragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        map_fragment.getMapAsync(this);         // calls onMapReady when loaded
    }

    /*
     * This method gets call when the map fragment is loaded.
     * The map is loaded, but not laid out yet.
     */
    @Override
    public void
    onMapReady(GoogleMap map)
    {
        this.map = map;
        map.setOnMapLoadedCallback(this);       // calls onMapLoaded when layout is done
    }

    /*
     * This method gets call when the map is ready.
     * Runs the start up tasks such as reading a list
     * of cities to add markers and determine user's location
     * if possible.
     */
    @Override
    public void
    onMapLoaded()
    {
        readCities();
        map.setOnMarkerClickListener(this);

        // determines user's current location, if possible
        my_location = getMyLocation();
        if (my_location == null)
        {
            Toast.makeText(this, "Unable to access your location. Consider enabling Location services in your device's settings.", Toast.LENGTH_LONG).show();
        }
        else
        {
            // add my location
            map.addMarker(new MarkerOptions().position(my_location).title("Me"));
        }
    }

    /*
     * This function reads a list of cities from a text file and draws a marker on the map
     * for each city.
     *
     * Note: The structure of an entry of a city is:
     *          City, State
     *          latitude
     *          longitude
     *
     * latitude: North/South relative to the equator (north pole = +90; south pole = -90)
     * longitude: East/West relative to prime meridian (west = 0 -> -180; east = 0 -> 180)
     */
    private void
    readCities()
    {
        Scanner scan = new Scanner(getResources().openRawResource(R.raw.cities));
        while(scan.hasNextLine())
        {
            String city_name = scan.nextLine();
            if (city_name == null)
            {
                break;
            }
            double city_latitude = Double.parseDouble(scan.nextLine());
            double city_longitude = Double.parseDouble(scan.nextLine());
            map.addMarker(new MarkerOptions()
                    .position(new LatLng(city_latitude, city_longitude))
                    .title(city_name)
            );
            //Log.d("test", "Added marker for " + city_name );
        }
    }

    /*
     * Returns the user's location as a LatLng object. Returns null
     * if location cannot be found (e.g. location service is off).
     * We will try to find out the user location in 3 ways:
     *      GPS,
     *      Cell or wifi network,
     *      and "passive" mode
     *
     * Passive mode: passively receive location updates when other applications or services request
     *              them without actually requesting the location yourself
     */
    private LatLng
    getMyLocation()
    {
        // try to obtain user location in 3 ways: GPS, cell or wifi network, and "passive" mode
        LocationManager location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
        {
            // gps method failed or not available; fallback to network
            location = location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null)
        {
            // gps and network failed or not available; fallback to "passive" location
            location = location_manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        if (location == null)
        {
            // cannot get the user's location
            return null;
        }
        else
        {
            double my_latitude = location.getLatitude();
            double my_longitude = location.getLongitude();
            return new LatLng(my_latitude, my_longitude);
        }
    }

    /*
     * This method gets call when user clicks on any city map markers.
     * Adds a line from the user's location to the chosen city.
     */
    @Override
    public boolean
    onMarkerClick(Marker marker)
    {
        if (my_location != null)
        {
            LatLng marker_LatLng = marker.getPosition();
            map.addPolyline(new PolylineOptions()
                            .add(my_location)
                            .add(marker_LatLng)
            );
            return true;
        }
        else
        {
            return false;
        }
    }
}
