package com.satoripop.intigo_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.satoripop.intigo_demo.geofence.data.GeofenceData;
import com.satoripop.intigo_demo.geofence.receivers.BoundaryEventBroadcastReceiver;
import com.satoripop.intigo_demo.location.LocationCoordinates;
import com.satoripop.intigo_demo.location.LocationUpdatesService;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Response;

import static com.satoripop.intigo_demo.geofence.services.BoundaryEventJobIntentService.GEOFENCE_DATA_TO_EMIT;

public class MainActivity extends AppCompatActivity  {

    ArrayList<Locations> locationsList = new ArrayList<>();

    RecyclerView rv_location ;
    private BroadcastReceiver mEventReceiver;

    private BroadcastReceiver mEventReceiverGeofence;

    public boolean hasGeofence = false ;

    Locations GEOLOCATION ;


    //Geofence
    public static final String TAG = "BoundaryEvent";

    private GeofencingClient mGeofencingClient;
    private PendingIntent mBoundaryPendingIntent;

    Button start ;
    Button stop ;
    Button clear ;
    Button configure ;

    private  Gson mGson = new Gson();


    // Tracks the bound state of the service.
    private boolean mBound = false;


    // Tracks the bound state of the service.
    private boolean stateOfLocation = false;


    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
    private String deviceId = "";

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection, Context.BIND_AUTO_CREATE);

    }



    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = Build.VERSION.SDK_INT > Build.VERSION_CODES.P ? new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } : new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                };

        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                goToIgnoreBatteryOptimization();
                createEventReceiver();
                registerEventReceiver();
                mGeofencingClient = LocationServices.getGeofencingClient(getApplicationContext());
            }



            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                super.onDenied(context, deniedPermissions);
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                return super.onBlocked(context, blockedList);
            }
        });





        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        clear = findViewById(R.id.clear);
        configure = findViewById(R.id.configure);
        Button map = findViewById(R.id.map);

        rv_location = findViewById(R.id.rc_locations);


        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBackgroundLocation();

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // changeConfig(LocationRequest.PRIORITY_HIGH_ACCURACY,0, 1000 * 5);
                stopBackgroundLocation();
                arrayOfDistance.clear();
                arrayOfPoints.clear();
                removeAll();
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createMapPoints();
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationsList.clear();
                Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();

            }
        });

        configure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            /*    if (stateOfLocation)
                {
                    Toast.makeText(getApplicationContext(),"Stop Location first !!!",Toast.LENGTH_SHORT).show();
                    return;
                }*/

               /* if (GEOLOCATION == null)
                { Toast.makeText(getApplicationContext(),"No locations to show !!!",Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = "https://www.google.com/maps/dir/"+GEOLOCATION.getLatitude()+"+"+GEOLOCATION.getLongitude();


                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);*/




                AndroidNetworking.get("https://tracking-demo.herokuapp.com/api/tracking/published/"+deviceId)
                        .build().getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // do anything with response
                        Log.d("AndroidNetworking", "onResponse: "+response);

                        if (response.length() == 0)
                        { Toast.makeText(getApplicationContext(),"No locations to show !!!",Toast.LENGTH_SHORT).show();
                            return;
                        }


                        Intent i = new Intent(MainActivity.this, ConfigureActivity.class);
                        i.putExtra("list", String.valueOf(response));
                        startActivity(i);


                    }
                    @Override
                    public void onError(ANError error) {
                        // handle error
                    }
                });

            }
        });



        rv_location.setLayoutManager(new LinearLayoutManager(this));
        LocationsAdapter locationsAdapter = new LocationsAdapter(locationsList);
        rv_location.setAdapter(locationsAdapter);

    }


    public void changeConfig(int priority,long distance, long interval) {
        Log.d("BackgroundLocation", "changePriority:  called" );
        Intent eventIntent = new Intent("LocationUpdatesService.startStopLocation");
        eventIntent.putExtra("ChangeConfigPriority",priority );
        eventIntent.putExtra("ChangeConfigDistance",distance );
        eventIntent.putExtra("ChangeConfigInterval",interval );

        getApplicationContext().sendBroadcast(eventIntent);

    }



    @SuppressLint("ResourceAsColor")
    public void startBackgroundLocation() {
        Log.d("BackgroundLocation", "startBackgroundLocation: start called" );
        Intent eventIntent = new Intent("LocationUpdatesService.startStopLocation");
        eventIntent.putExtra("StartStopLocation", "START");
        getApplicationContext().sendBroadcast(eventIntent);
        stop.setEnabled(true);
        stop.setTextColor(Color.RED);

        start.setEnabled(false);
        start.setTextColor(Color.GRAY);

        stateOfLocation = true;


    }


    @SuppressLint("ResourceAsColor")
    public void stopBackgroundLocation() {
        Log.d("BackgroundLocation", "stopBackgroundLocation: stopped called" );
        Intent eventIntent = new Intent("LocationUpdatesService.startStopLocation");
        eventIntent.putExtra("StartStopLocation", "STOP");
        getApplicationContext().sendBroadcast(eventIntent);
        stop.setEnabled(false);
        stop.setTextColor(Color.GRAY);

        start.setEnabled(true);
        start.setTextColor(Color.GREEN);

        stateOfLocation = false;


    }

    ArrayList<Locations> arrayOfPoints = new ArrayList<>();
    ArrayList<Float> arrayOfDistance = new ArrayList<>();


    public void createEventReceiver() {

        if (mEventReceiver == null) {
            mEventReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocationCoordinates locationCoordinates = mGson.fromJson(
                            intent.getStringExtra(LocationUpdatesService.LOCATION_EVENT_DATA_NAME),
                            LocationCoordinates.class);

                 /*   if(hasGeofence)
                        return;*/

                    arrayOfPoints.add(new Locations("",new Date().getTime(),locationCoordinates.getLatitude(),locationCoordinates.getLongitude()));

                    if (arrayOfPoints.size() > 8){

                        Location endPoint=new Location("locationB");
                        endPoint.setLatitude(locationCoordinates.getLatitude());
                        endPoint.setLongitude(locationCoordinates.getLongitude());


                        for(Locations loc : arrayOfPoints)
                           { Location startPoint=new Location("locationA");
                             startPoint.setLatitude(loc.getLatitude());
                             startPoint.setLongitude(loc.getLongitude());
                               if(startPoint.distanceTo(endPoint) < 3)
                                   arrayOfDistance.add(startPoint.distanceTo(endPoint));
                           }


                       if(arrayOfDistance.size() >= 8 ) {
                              add(new GeofenceData("current",locationCoordinates.getLatitude(),locationCoordinates.getLongitude(),10));
                              GEOLOCATION = new Locations("",0,locationCoordinates.getLatitude(),locationCoordinates.getLongitude());
                              //changePriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                              //stopBackgroundLocation();
                              changeConfig(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY,1, 0);
                             // locationsList.add(0,new Locations("Change Location Manager Config : "+" Time: "+convertTimestamp(new Date().getTime()),new Date().getTime(),0,0));
                              Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();
                              arrayOfPoints.clear();
                              arrayOfDistance.clear();
                       }

                       if(arrayOfPoints.size() > 0)
                         {
                            arrayOfPoints.remove(0);
                            arrayOfDistance.remove(0);
                         }



                    }


                   /* add(new GeofenceData("current",locationCoordinates.getLatitude(),locationCoordinates.getLongitude(),10));
                    stopBackgroundLocation();
                    locationsList.add(0,new Locations("Stop Location Manager: "+" Time: "+convertTimestamp(new Date().getTime()),new Date().getTime(),0,0));
                    Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();*/


                    // if(!hasGeofence)
                    locationsList.add(0,new Locations("Time: "+ convertTimestamp(locationCoordinates.getTimestamp()) +"\nLatitude: "+locationCoordinates.getLatitude()+
                            "\nLongitude: "+locationCoordinates.getLongitude(),locationCoordinates.getTimestamp(),locationCoordinates.getLatitude(),locationCoordinates.getLongitude()));
                    Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();


                    if(!hasGeofence)
                    AndroidNetworking.post("https://tracking-demo.herokuapp.com/api/tracking")
                            .addBodyParameter("phoneid", deviceId)
                            .addBodyParameter("lat", String.valueOf(locationCoordinates.getLatitude()))
                            .addBodyParameter("lon", String.valueOf(locationCoordinates.getLongitude()))
                            .addBodyParameter("timestamp", String.valueOf(locationCoordinates.getTimestamp()))
                            .build().getAsOkHttpResponse(new OkHttpResponseListener() {



                        @Override
                        public void onResponse(Response response) {

                        }

                        @Override
                        public void onError(ANError anError) {
                            Log.i("service", " anError == anError"+anError);
                        }
                    });

                }
            };
        }



        //Geofence
        if (mEventReceiverGeofence == null) {
            mEventReceiverGeofence = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.d(TAG, "onReceive: "+intent.getStringExtra("event"));
                    Log.d(TAG, "onReceive: "+intent.getStringArrayExtra("params"));

                    ArrayList<String> ids = new ArrayList<>();
                    String event = "";


                    ids = intent.getStringArrayListExtra("params");
                    event = intent.getStringExtra("event");

                    if(event.equals("onExit")){
                        locationsList.add(0,new Locations("EXIT : "+ids.get(0)+" Time: "+convertTimestamp(new Date().getTime()),new Date().getTime(),0,0));
                        Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();
                        //changePriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        vibrate();
                        arrayOfPoints.clear();
                        arrayOfDistance.clear();
                      //  startBackgroundLocation();
                        changeConfig(LocationRequest.PRIORITY_HIGH_ACCURACY,0, 1000 * 5);
                        removeAll();
                    }

                }
            };
        }

    }

    public void registerEventReceiver() {
        IntentFilter eventFilter = new IntentFilter();
        eventFilter.addAction(LocationUpdatesService.LOCATION_EVENT_NAME);
        getApplicationContext().registerReceiver(mEventReceiver, eventFilter);

        IntentFilter eventFilterGeofence = new IntentFilter();
        eventFilterGeofence.addAction(GEOFENCE_DATA_TO_EMIT);
        getApplicationContext().registerReceiver(mEventReceiverGeofence, eventFilterGeofence);
    }


    @Override
    protected void onDestroy() {
        Intent eventIntent = new Intent("LocationUpdatesService.startStopLocation");
        eventIntent.putExtra("StartStopLocation", "STOP");
        this.sendBroadcast(eventIntent);
        arrayOfDistance.clear();
        arrayOfPoints.clear();
       // changeConfig(LocationRequest.PRIORITY_HIGH_ACCURACY,0, 1000 * 5);
        removeAll();
        super.onDestroy();
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


    public void removeAll() {
        mGeofencingClient.removeGeofences(getBoundaryPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully removed all geofences");
                        locationsList.add(0,new Locations("Successfully removed all geofences : "+" Time: "+convertTimestamp(new Date().getTime()),new Date().getTime(),0,0));
                        Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();
                        hasGeofence = false ;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to remove all geofences");
                    }
                });
    }

    private PendingIntent getBoundaryPendingIntent() {
        if (mBoundaryPendingIntent != null) {
            return mBoundaryPendingIntent;
        }
        Intent intent = new Intent(getApplicationContext(), BoundaryEventBroadcastReceiver.class);
        mBoundaryPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mBoundaryPendingIntent;
    }

   /* private GeofencingRequest createGeofenceRequest(List<Geofence> geofences) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofences)
                .build();
    }*/

    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

/*    private void addGeofence(final GeofencingRequest geofencingRequest) {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission != PackageManager.PERMISSION_GRANTED) {

        }


        if (permission != PackageManager.PERMISSION_GRANTED) {


        } else {
            mGeofencingClient.addGeofences(geofencingRequest, getBoundaryPendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {


                        }
                    });
        }

    }*/

    private void addGeofence(final GeofencingRequest geofencingRequest, final String requestId) {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);


        if (permission != PackageManager.PERMISSION_GRANTED) {

        }

        if (permission != PackageManager.PERMISSION_GRANTED) {

        } else {
            Log.i(TAG, "Attempting to add geofence.");

            mGeofencingClient.addGeofences(geofencingRequest, getBoundaryPendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Successfully added geofence. "+requestId);
                            locationsList.add(0,new Locations("Successfully added geofence. : "+" Time: "+convertTimestamp(new Date().getTime()),new Date().getTime(),0,0));
                            Objects.requireNonNull(rv_location.getAdapter()).notifyDataSetChanged();
                            hasGeofence = true ;
                            //vibrate();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Failed to add geofence.");

                        }
                    });
        }
    }

/*    private void removeGeofence(List<String> requestIds) {
        Log.i(TAG, "Attempting to remove geofence.");
        mGeofencingClient.removeGeofences(requestIds)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Successfully removed geofence.");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Failed to remove geofence.");

                    }
                });
    }*/

/*    public void remove(final String boundaryRequestId) {
        removeGeofence(Collections.singletonList(boundaryRequestId));
    }*/

 /*   public void remove(List<GeofenceData> readableArray) {

        final List<String> boundaryRequestIds = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); ++i) {
            boundaryRequestIds.add(String.valueOf(readableArray.get(i)));
        }

        removeGeofence(boundaryRequestIds);
    }*/

    public void add(GeofenceData readableMap) {
        final GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofence(readableMap));
        addGeofence(geofencingRequest, geofencingRequest.getGeofences().get(0).getRequestId());
    }

 /*   public void add(List<GeofenceData> readableArray) {
        final List<Geofence> geofences = createGeofences(readableArray);
        final List<String> geofenceRequestIds = new ArrayList();
        for (int i=0;i<geofences.size();i++)
            geofenceRequestIds.set(i,geofences.get(i).getRequestId());



        GeofencingRequest geofencingRequest = createGeofenceRequest(createGeofences(readableArray));

        addGeofence(geofencingRequest, geofenceRequestIds.toString());
    }*/

    private Geofence createGeofence(GeofenceData geofence) {
        return new Geofence.Builder()
                .setRequestId(geofence.getId())
                .setCircularRegion(geofence.getLat(), geofence.getLng(), (float) geofence.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

  /*  public List<Geofence> createGeofences(List<GeofenceData> readableArray) {
        List<Geofence> geofences = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); ++i) {
            geofences.add(createGeofence(readableArray.get(i)));
        }
        return geofences;
    }*/

    public String convertTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        return  DateFormat.format("dd-MM-yyyy hh:mm:ss", cal).toString();

    }

    public void goToIgnoreBatteryOptimization() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName))
            {intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
       // startActivity(intent);
                }

        else {
            startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:"+getPackageName())));
        }


    }

    public void vibrate (){
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 400 milliseconds
        v.vibrate(400);
    }
}