package com.example.daxing.qualitytest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class SearchingTabActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final String TAG = SearchingTabActivity.class.getSimpleName();
    public final String VIDEO_ID_MESSAGE = "com.example.daxing.VIDEO_ID";

    private Button b_search;
    private ListView lv_videolist;
    private EditText et_keyword;
    private ArrayList<DeviceSchema> log;
    private ArrayList<ListItem> newResult;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.i(TAG, "API UNAVAILABLE");
        }
        Log.e(TAG, "Connection to Google Play Service Failed.");
    }

    class DeviceSchema {
        public String name;
        public ArrayList<VideoSchema> cache = new ArrayList<VideoSchema>();
        public int d2d = 0;
        public double time;
        public double loc[];
        DeviceSchema(){}
    }
    class VideoSchema {
        public String name;
        public double size;
        VideoSchema(String name, double size){
            this.name = name;
            this.size = size;
        }
    }
    private AsyncHttpClient ajax = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_tab);

        String content = "";
        Gson gson = new Gson();

        try {
            File targetFile = new File(Environment.getExternalStorageDirectory() + "/Loginfo/", "log.txt");
            if(targetFile.length() > 0){
                content =  FileUtils.readFileToString(targetFile);
                Log.e("onCreate content",content);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(content.length() > 0){
            Log.e("onCreate", "logs found!");
            DeviceSchema[] array = gson.fromJson(content, DeviceSchema[].class);
            log = new ArrayList<DeviceSchema>(Arrays.asList(array));
        } else {
            Log.e("onCreate", "logs not found :(");
            log = new ArrayList<DeviceSchema>();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

        }

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting application");
        mGoogleApiClient.connect();
        setUI();
        super.onStart();
    }//onStart

    private void setUI() {
        Log.i(TAG, "Set UI");

        b_search = (Button) findViewById(R.id.b_search);
        b_search.setOnClickListener(this);

        lv_videolist = (ListView) findViewById(R.id.ls_video);
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemVideoClicked(adapterView, view, i, l);
            }
        });

        et_keyword = (EditText) findViewById(R.id.et_keyword);
    }

    @Override
    public void onClick(View view) {
        initGPS();
        switch (view.getId()) {
            case R.id.b_search: {
                onButtonSearchClicked();
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        onButtonWriteJsonClicked();
        super.onStop();
    }

    public void onButtonWriteJsonClicked() {
        Log.e("Write Log button", "clicked");
        Log.e("log contents", log.toString());
        String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
        File targetLocation = new File(PATH);
        if (!targetLocation.exists()) {
            targetLocation.mkdirs();
        }
        Gson gson = new Gson();
        // Get Directory of SD card
        String FILE_PATH = Environment.getExternalStorageDirectory() + "/Loginfo/" + "log.txt";
        try {
            PrintWriter writer = new PrintWriter(FILE_PATH);
            writer.print(gson.toJson(log.toArray(),DeviceSchema[].class));
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void onButtonSearchClicked()  {
        String keyword = et_keyword.getText().toString();
        try {
            newResult = new SearchYoutube().execute(keyword).get();
            if (newResult == null) {
                Log.i(TAG, "newResult is null");
            }
            prettyPrint(newResult);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    void onItemVideoClicked(AdapterView<?> adapterView, View view, int i, long l) {
        String video_id = ((TextView)(view.findViewById(R.id.VideoID))).getText().toString();
        String video_name = ((TextView)(view.findViewById(R.id.VideoTitle))).getText().toString();
        sendJson(video_name, 123);
        Intent intent = new Intent(this, PlayVideoActivity.class);
        intent.putExtra(VIDEO_ID_MESSAGE, video_id);
        startActivity(intent);
    }

    public void sendJson(String title, int size) {
        DeviceSchema device;
        Gson gson  = new Gson();
        if(log.size() > 0){
            device =log.get(log.size()-1);
        } else {
            device = new DeviceSchema();
        }

//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        Date date =new java.util.Date();
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        device.name = android_id;
        device.time = date.getTime();
        Location temp = getLastLocation(mGoogleApiClient);
        device.loc = new double[]{temp.getLongitude(), temp.getLatitude()};
        device.cache.add(new VideoSchema(title,size));


        String json = gson.toJson(device);

        //store DeviceSchema in the global log
        log.add(device);


        StringEntity se;
        try {
            se = new StringEntity(json);
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            ajax.post(null, "http://www.edward-hu.com/logs", se, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.e("ajax", "success");
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.e("ajax", "success");
                }
            });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e("string entity", "failed");
        }
    }

    protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;
        Log.d("google play onConnected", "connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

    public void prettyPrint(ArrayList<ListItem> mylist) {
        lv_videolist.setAdapter(new CustomAdapter(this, mylist));
    }

    protected void onResume() {
        super.onResume();
        initGPS();
    }

//start: check if the location service or GPS is open
    private void initGPS() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);
        if ((!locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))&&(!locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER))) {
            Toast.makeText(getApplicationContext(), "Please open Location Service",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Please open Location Service");
            dialog.setPositiveButton("OK",
                    new android.content.DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);

                        }
                    });
            dialog.setNeutralButton("Cancel", new android.content.DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            } );
            dialog.show();
        } else if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Toast.makeText(getApplicationContext(), "Please open GPS",
                    Toast.LENGTH_SHORT).show();
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Open GPS?");
            dialog.setPositiveButton("Yes",
                    new android.content.DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);
                        }
                    });
            dialog.setNeutralButton("No", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            } );
            dialog.show();
        } else
        {

        }
    }
    //end: check if the location service or GPS is open
}
