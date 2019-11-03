package com.royalteck.progtobi.accidentrescuesystem;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.royalteck.progtobi.accidentrescuesystem.Adapter.HospitalAdapter;
import com.royalteck.progtobi.accidentrescuesystem.Adapter.HospitalRecycler;
import com.royalteck.progtobi.accidentrescuesystem.Network.APIService;
import com.royalteck.progtobi.accidentrescuesystem.Network.ApiClient;
import com.royalteck.progtobi.accidentrescuesystem.Network.Mysingleton;
import com.royalteck.progtobi.accidentrescuesystem.Preferences.SharedPreferenceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Model.AccidentResponse;
import Model.HospitalModel;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, HospitalRecycler.ClickListener {

    Location mLocation;
    private AddressResultReceiver mResultReceiver;
    String mAddressOutput;
    TextView latLng, addresstxtview;
    double latitude, longitude;
    GoogleApiClient mGoogleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    ArrayList<HospitalModel> mHospitals;
    APIService mHospitalrequest;

    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 15000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private HospitalAdapter mhosptialAdapter;
    private String url = "https://estateform.000webhostapp.com/view_hospitalapi.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mHospitals = new ArrayList<>();
        mHospitalrequest = ApiClient.getClient().create(APIService.class);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        coordinatorLayout = findViewById(R.id.coordinatorlayout);
        recyclerView = findViewById(R.id.hospitalrecycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.addOnItemTouchListener(new HospitalRecycler.
                RecyclerTouchListener(MainActivity.this, recyclerView, this));

        //swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchHospitals();
            }
        });

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        mResultReceiver = new AddressResultReceiver(null);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


    }

    public void init() {
        swipeRefreshLayout.setRefreshing(true);
        fetchHospitals();
    }

    private void fetchHospitals() {
        if (isOnline(this)) {
            if (mHospitals != null) {
                mHospitals.clear();
                fetchOnline();
            }

        } else {
            if (mHospitals != null) {
                mHospitals.clear();
                popultateDevelopersList();
                swipeRefreshLayout.setRefreshing(false);
                showSnackBarMessage("No Internet Connection");
                //Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
            }
        }

    }

    private void showSnackBarMessage(String s) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, s, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        init();
                    }
                });

        snackbar.setActionTextColor(getResources().getColor(R.color.colorPrimaryDark));
        snackbar.show();
    }

    private void fetchOnline() {

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, (String) null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        HospitalModel item = new HospitalModel(jsonObject.getString("hospital_name"), jsonObject.getString("emergency_no"),
                                jsonObject.getDouble("position_latitude"), jsonObject.getDouble("position_longitude"), jsonObject.getString("hospital_details"));
                        mHospitals.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                swipeRefreshLayout.setRefreshing(false);
                popultateDevelopersList();

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("VOLLEY ERROR", "onErrorResponse: " + error.getMessage());
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    showSnackBarMessage("Connection Could Not be Establish At The Moment, Try Later...");
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    showSnackBarMessage("Failure Authenticating the Request...");
                    error.printStackTrace();

                } else if (error instanceof ServerError) {
                    showSnackBarMessage( "Error Response from the Server...");
                    error.printStackTrace();

                } else if (error instanceof ParseError) {
                    showSnackBarMessage("Server Error...");
                    error.printStackTrace();

                } else if (error instanceof NetworkError) {
                    showSnackBarMessage( "Network Error, Check Your Network Connection...");
                    error.printStackTrace();

                }
                swipeRefreshLayout.setRefreshing(false);
                popultateDevelopersList();


            }
        });

        Mysingleton.getInstance(MainActivity.this).addtorequestque(request);
    }

    private void popultateDevelopersList() {
        if (mHospitals.size() < 1) {
            //Toast.makeText(MainActivity.this, "Hospital list is empty", Toast.LENGTH_LONG).show();
            setHospitalAdapter(mHospitals);
        } else {
            setHospitalAdapter(mHospitals);
            //mDevelopersAdapter.notifyDataSetChanged();
        }
    }

    private void saveDevelopersDetails(final AccidentResponse body) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SharedPreferenceHandler.saveHospitalRecords(MainActivity.this, body);
            }
        };
        AsyncTask.execute(runnable);
    }

    private void fetchLocally() {
/*
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mHospitals = SharedPreferenceHandler.
                        fetchHospitalRecords(MainActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mHospitals.size() < 1) {
                    Toast.makeText(MainActivity.this, "Hospital list is empty", Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    setHospitalAdapter(mHospitals);
                    swipeRefreshLayout.setRefreshing(false);
                    //meventAdapter.notifyDataSetChanged();
                }
            }
        }.execute();
*/
    }

    private void setHospitalAdapter(ArrayList<HospitalModel> mHospitals) {
        mhosptialAdapter = new HospitalAdapter(mHospitals, MainActivity.this, latitude, longitude);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setAddDuration(1000);
        defaultItemAnimator.setMoveDuration(1000);
        defaultItemAnimator.setChangeDuration(1000);
        recyclerView.setItemAnimator(defaultItemAnimator);
        recyclerView.setAdapter(mhosptialAdapter);
    }

    public boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activenet = connectivityManager.getActiveNetworkInfo();
        return activenet != null;
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            latLng.setText("Please install Google Play services.");
        }
    }

    public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        startActivity(intent);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;

        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLocation != null)

        {
            if (!Geocoder.isPresent()) {
                showSnackBarMessage(String.valueOf(R.string.no_geocoder_available));
                return;
            }

            // Start service and update UI to reflect new location
            startIntentService();
            longitude = mLocation.getLongitude();
            latitude = mLocation.getLatitude();
            init();
            //latLng.setText("Latitude : " + mLocation.getLatitude() + " , Longitude : " + mLocation.getLongitude());
        }

        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            if (!Geocoder.isPresent()) {
                Toast.makeText(MainActivity.this,
                        R.string.no_geocoder_available,
                        Toast.LENGTH_LONG).show();
                return;
            }

            // Start service and update UI to reflect new location
            startIntentService();
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            //latLng.setText("Latitude : " + location.getLatitude() + " , Longitude : " + location.getLongitude());
        }

    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLocation);
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                finish();

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String) permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[]) permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }


    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View view, int position) {
        showDeveloperDetails(mHospitals.get(position));

    }

    public void showDeveloperDetails(HospitalModel eventModel) {
        Intent i = new Intent(MainActivity.this, ViewHospitalDetails.class);
        i.putExtra("hospitalDetails", eventModel);
        i.putExtra("currentlat", latitude);
        i.putExtra("currentlong", longitude);
        startActivity(i);
    }

    @Override
    public void onLongClick(View view, int position) {

    }


    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayOnView(mAddressOutput);
                }
            });


            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {

                Toast.makeText(MainActivity.this, getString(R.string.address_found), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void displayOnView(String mAddressOutput) {
        //addresstxtview.setText(mAddressOutput);
    }

}
