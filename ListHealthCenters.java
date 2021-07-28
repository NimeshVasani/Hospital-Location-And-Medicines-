/*
 * %W% %E% Zain-Ul-Abedin
 *
 * Copyright (c) 2017-2018. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of ZainMustafaaa.
 * You shall not disclose such Confidential Information and
 * shall use it only in accordance with the terms of the license agreement
 * for learning purposes.
 *
 */
package com.example.runningapp.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.runningapp.R;
import com.example.runningapp.adapter.CustomPlacesAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ListHealthCenters extends AppCompatActivity {

    public static StringBuffer stringBuffer = new StringBuffer();
    ListView centersListView;
    double latitude, longitude;
    Button scanButton, viewMapButton;
    LocationManager locationManager;

    Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_centers_list);

        centersListView = (ListView) findViewById(R.id.centersListView);

        centersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("Selected=> ", i + "");
                listSelection(i);
            }
        });

        viewMapButton = (Button) findViewById(R.id.viewMapButton);
        viewMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewMapButton();
            }
        });

        scanButton = (Button) findViewById(R.id.scanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                try {
                    updateLoc();
                    GeometryController.loading = true;
                    loadLocation();
                    while (GeometryController.loading) {
                        Log.d("Message=>>>>", "Waiting");
                    }
                    fillList();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(ListHealthCenters.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    /** update current location */
                    updateLoc();
                    /** setting up loading true */
                    GeometryController.loading = true;
                    /** load nearby locations */
                    loadLocation();
                    /** loading */
                    while (GeometryController.loading) {
                        Log.d("Message=>>>>", "Waiting");
                    }
                    /** calling fillList method */
                    fillList();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(ListHealthCenters.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    NearestHospital.progressDialog.cancel();
                    e.printStackTrace();
                    finish();
                }
            }
        });
    }

    void listSelection(int i) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        dialog.setTitle(GeometryController.detailArrayList.get(i).getHospitalName());
        dialog.setMessage(GeometryController.detailArrayList.get(i).getAddress());
        dialog.setIcon(R.drawable.marker);
        dialog.show();
    }

    void viewMapButton() {

        Intent intent = new Intent(ListHealthCenters.this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);
    }

    public void updateLoc() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw new IllegalArgumentException("No GPS");
        } else if (!isGooglePlayServicesAvailable(this)) {
            throw new IllegalArgumentException("No Google Play Services Available");
        } else getLocation();

    }

    public boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    void getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Permission", "Not Granted moving back");
            return;
        }

        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            Log.d("Achieved latitude=>", location.getLatitude() + ", longitide=> " + location.getLongitude());
        }

        if (location == null) {
            Log.d("GPS PRovider", "Enabled");
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        if (location == null) throw new IllegalArgumentException("Can't trace location");

        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    protected void fillList() {

        ArrayList<String> placeName = new ArrayList();
        for (int i = 0; i < GeometryController.detailArrayList.size(); i++){
            placeName.add(GeometryController.detailArrayList.get(i).getHospitalName());
        }

        ArrayList<String> ratingText = new ArrayList();
        for (int i = 0; i < GeometryController.detailArrayList.size(); i++){
            ratingText.add(GeometryController.detailArrayList.get(i).getRating());
        }
        Log.d("placename",placeName.toString());

        ArrayList<String> openNow = new ArrayList<>();
        for (int i = 0; i < GeometryController.detailArrayList.size(); i++){
            openNow.add(GeometryController.detailArrayList.get(i).getOpeningHours());
        }

        CustomPlacesAdapter customPlacesAdapter = new CustomPlacesAdapter(this, placeName, ratingText, openNow);
        centersListView.setAdapter(customPlacesAdapter);
        NearestHospital.progressDialog.cancel();
    }

    void loadLocation() {
        try {
            new RetrieveFeedTask().execute();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (stringBuffer.length() == 0) Log.d("Messege", "buffer reading");
                    GeometryController.manipulateData(ListHealthCenters.stringBuffer);
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class RetrieveFeedTask extends AsyncTask<StringBuffer, StringBuffer, StringBuffer > {

        @Override
        protected StringBuffer doInBackground(StringBuffer... stringBuffers) {
            try {
                /** initializing StringBuilder  */
                StringBuilder stringBuilder = new StringBuilder()
                        .append("https://maps.googleapis.com/maps/api/place/search/json?rankby=distance&keyword=hospital&location=")
                        .append(latitude)
                        .append(",")
                        .append(longitude)
                        .append("&key=AIzaSyC6-gwhsbRMAbtSNhR56y2EBV9S16bZhHE&sensor=false&libraries=places");

                /** searching for url */
                URL url = new URL(stringBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer buffer = new StringBuffer();

                String n = "";
                while((n=bufferedReader.readLine())!=null){
                    buffer.append(n);
                }

                Log.d("loaded with size of  => ", "Size is " + buffer.length());

                ListHealthCenters.stringBuffer = buffer;
                return buffer;

            } catch (Exception e) {
                return null;
            }
        }
    }
}
