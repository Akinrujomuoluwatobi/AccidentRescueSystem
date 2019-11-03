package com.royalteck.progtobi.accidentrescuesystem;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import Model.HospitalModel;

public class ViewHospitalDetails extends AppCompatActivity {

    HospitalModel hospital;
    TextView hospitalnametxtview, emergencynotxtview, detailstxtview;
    Double position_lat, position_long;
    ImageView callbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_hospital_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        hospital = (HospitalModel) intent.getSerializableExtra("hospitalDetails");
        position_lat = intent.getDoubleExtra("currentlat", 0.0);
        position_long = intent.getDoubleExtra("currentlong", 0.0);
        hospitalnametxtview = findViewById(R.id.displayhospitalname);
        emergencynotxtview = findViewById(R.id.displayphoneno);
        detailstxtview = findViewById(R.id.displayhospitaldetails);
        callbtn = findViewById(R.id.button_call);

        hospitalnametxtview.setText(hospital.getHospitalName());
        emergencynotxtview.setText(hospital.getHospitalEmergencyNo());
        detailstxtview.setText(hospital.getHospitalDetails());
        //Toast.makeText(ViewHospitalDetails.this, "Latitude: " + hospital.getPositionLatitude() + "Longitude: " + hospital.getPositionLongitude(), Toast.LENGTH_LONG).show();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewHospitalDetails.this, MapsActivity.class);
                intent.putExtra("healthlat", hospital.getPositionLatitude());
                intent.putExtra("healthlong", hospital.getPositionLongitude());
                intent.putExtra("positionlat", position_lat);
                intent.putExtra("positionlong", position_long);
                startActivity(intent);
            }
        });

        callbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "
                        + hospital.getHospitalEmergencyNo()));
                if (ActivityCompat.checkSelfPermission(ViewHospitalDetails.this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(i);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
