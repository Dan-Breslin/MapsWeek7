package com.example.mapsweek7;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapsweek7.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    final private int REQUEST_COARSE_ACCESS = 123;
    boolean permissionGranted = false;
    LocationManager lm;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(@NonNull Location location) {
            if(location != null){
                Toast.makeText(getBaseContext(),addressInfo(location.getLatitude(),location.getLongitude(),1)
                        + "\nLatitude: "+ location.getLatitude()
                        + "\nLongitude: " + location.getLongitude(),Toast.LENGTH_SHORT).show();

                LatLng p = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(p).title(addressInfo(location.getLatitude(),location.getLongitude(),0)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p,15.0f));
            }
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            LocationListener.super.onProviderEnabled(provider);
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            LocationListener.super.onProviderDisabled(provider);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //set the Location identifier to add 1 to the location of each new click
        String addressInfo="";
        String addressInfoMini = "";

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION}, REQUEST_COARSE_ACCESS);
            return;
        }else{
            permissionGranted = true;
        }

        if(permissionGranted){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2,locationListener);
        }

        // create Map click Listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            // On click Set the Marker and options
            public void onMapClick(@NonNull LatLng latLng) {

                    googleMap.addMarker(new MarkerOptions().position(latLng)
                            // Use find AddressInfo Function to get minimal information on current location
                            .title(addressInfo(latLng.latitude,latLng.longitude,0)) // display condensed address info
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));

                    // Use find AddressInfo Function to get information on current location
                    Toast.makeText(getBaseContext(),addressInfo(latLng.latitude,latLng.longitude,1),Toast.LENGTH_LONG).show();

            }


        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_COARSE_ACCESS:
                if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                    if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2, locationListener);
                }else {
                    permissionGranted = false;
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    ACCESS_FINE_LOCATION}, REQUEST_COARSE_ACCESS);
            return;
        }else{
            permissionGranted = true;
        }
        if(permissionGranted){
            lm.removeUpdates(locationListener);
        }
    }

    // This was being called too many times so created a function to make it easier.
    // It gets fed the location and a tag to determine which info is required back
    public String addressInfo(double lat,double lon, int i){
        String addressInfo="";
        String addressInfoMini = "";

        // Reverse GeoLocation
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = geocoder.getFromLocation(lat,lon,1);
            // code for Address String taken from StackOverflow examples
            if(addresses != null && addresses.size() > 0 ){
                Address address = addresses.get(0);
                //Address Info
                addressInfo = String.format("%s, %s, \n%s, %s, \n%s, \n%s",
                        address.getSubThoroughfare(),
                        address.getThoroughfare(),
                        address.getPostalCode(),
                        address.getLocality(),
                        address.getSubAdminArea(),
                        address.getCountryName());

                // Condensed Address info
                addressInfoMini = String.format("%s, %s, %s",
                        address.getSubThoroughfare(),
                        address.getThoroughfare(),
                        address.getPostalCode());
            }

        }catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (i ==1)
        return addressInfo;
        else return
                addressInfoMini;
    }

}