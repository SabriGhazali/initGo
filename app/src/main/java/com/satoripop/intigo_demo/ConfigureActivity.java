package com.satoripop.intigo_demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ConfigureActivity extends AppCompatActivity {

 /*   EditText distance;
    EditText time;

    Button save ;*/
    Button map ;
    ArrayList<Locations> locationsList = new ArrayList<>();

    RecyclerView rv_location ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        rv_location = findViewById(R.id.rc_locations_2);

        map = findViewById(R.id.map_2);

        Intent intent = getIntent();


        String str =  intent.getExtras().getString("list") ;

        try {
            JSONArray jsonArr = new JSONArray(str);
            for (int i = 0; i < jsonArr.length(); i++)
            {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                locationsList.add(0, new Locations("Timestamp: "+convertTimestamp(Long.parseLong(String.valueOf(jsonObj.get("timestamp"))) ) +"\nLatitude: "+jsonObj.get("lat") +"\nLongitude: "+jsonObj.get("lon") ,
                        Long.parseLong(String.valueOf(jsonObj.get("timestamp"))),
                        Double.parseDouble(String.valueOf(jsonObj.get("lat"))) ,
                        Double.parseDouble(String.valueOf(jsonObj.get("lon")))));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Log.d("", "onCreate: ");

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMapPoints();
            }
        });

        rv_location.setLayoutManager(new LinearLayoutManager(this));
        LocationsAdapter locationsAdapter = new LocationsAdapter(locationsList);
        rv_location.setAdapter(locationsAdapter);






    /*    time = findViewById(R.id.time);

        distance.setText(UPDATE_INTERVAL_IN_DISPLACEMENT+"");
        time.setText(UPDATE_INTERVAL_IN_MILLISECONDS+"");


        save = findViewById(R.id.save);
        cancel = findViewById(R.id.cancel);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(time.getText().equals("") || distance.getText().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"You must set params !!!",Toast.LENGTH_SHORT).show();
                    return;
                }


                UPDATE_INTERVAL_IN_MILLISECONDS = Long.parseLong(String.valueOf(time.getText()));

                UPDATE_INTERVAL_IN_DISPLACEMENT = Long.parseLong(String.valueOf(distance.getText())) ;

                finish();



            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();

            }
        });*/


    }


    public void createMapPoints(){

        if (locationsList.size() == 0)
        { Toast.makeText(this,"No locations to show !!!",Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "https://www.google.com/maps/dir/";

        for(int i=0;i<locationsList.size();i++){
            url+= locationsList.get(i).getLatitude()+"+"+locationsList.get(i).getLongitude()+"/";
        }

        Log.d("createMapPoints", "createMapPoints: "+url);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);

    }


    public String convertTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return  DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

    }



}