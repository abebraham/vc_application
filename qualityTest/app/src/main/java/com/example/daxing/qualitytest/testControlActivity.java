package com.example.daxing.qualitytest;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

public class testControlActivity extends FragmentActivity implements OnClickListener, OnMapReadyCallback
        , GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener{

    private static final String TAG = testControlActivity.class.getSimpleName();
    private testControlActivity myActiv;
    /**
     * Vibration duration, milliseconds
     */
    private static final long VIBRATION_DURATION = 1500;
    public final static String VIDEO_ID_MESSAGE = "com.example.daxing.VIDEO_ID";

    private Button b_wifi;
    private Button b_ping;
//    private Button b_submit;
//    private Button b_clear;
//    private Button b_sendlog;
    private Button b_writelog;
    private Button b_monitor_traff;
    private Button b_search;
    private Button b_trending;

    private TextView t_wifi;
    private TextView t_ping;
//    private TextView t_welcome;
//    private TextView t_traff;
//
//    private EditText et_username;
//    private EditText et_id;
    private EditText et_keyword;

    private ListView lv_videolist;

    private Spinner mySpinner;

    static final String PING_DEST_URL = "8.8.8.8";

    private static String pingError = "";
    private String country_code = "US";

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap gMap;

    private ArrayList<ListItem> newResult;
    private ArrayList<DeviceSchema> log;
    private List<String> region_code = new ArrayList<String>();
    private ArrayAdapter<String> region_adapter;


    private  AsyncHttpClient client = new AsyncHttpClient();
    private StringEntity se = null;

    private HashMap<String, String> region_map = new HashMap<String, String>();
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
        setContentView(R.layout.activity_test_control);
        Log.i(TAG,"onCreate");

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

        initial_region_map();
        region_code.add("United States");
        region_code.add("Austria");
        region_code.add("Australia");
        region_code.add("Brazil");
        region_code.add("Canada");
        region_code.add("Switzerland");
        region_code.add("China");
        region_code.add("Colombia");
        region_code.add("Spain");
        region_code.add("Finland");
        region_code.add("United Kingdom");
        region_code.add("Greece");
        region_code.add("Hong Kong");
        region_code.add("Italy");
        region_code.add("Japan");
        region_code.add("Korea");
        region_code.add("Macau");
        region_code.add("Mexico");
        region_code.add("Malaysia");
        region_code.add("New Zealand");
        region_code.add("Philippines");
        region_code.add("Poland");
        region_code.add("Portugal");
        region_code.add("Singapore");
        region_code.add("Sweden");
        region_code.add("Thailand");
        region_code.add("Taiwan");
        region_code.add("South Africa");
    }

    protected void initial_region_map() {
        region_map.put("United States", "US");
        region_map.put("Austria", "AT");
        region_map.put("Australia", "AU");
        region_map.put("Brazil", "BR");
        region_map.put("Canada", "CA");
        region_map.put("Switzerland", "CH");
        region_map.put("China", "CN");
        region_map.put("Colombia", "CO");
        region_map.put("Spain", "ES");
        region_map.put("Finland", "FI");
        region_map.put("United Kingdom", "GB");
        region_map.put("Greece", "GR");
        region_map.put("Hong Kong", "HK");
        region_map.put("Italy", "IT");
        region_map.put("Japan", "JP");
        region_map.put("Korea", "KR");
        region_map.put("Macau", "MO");
        region_map.put("Mexico", "MX");
        region_map.put("Malaysia", "MY");
        region_map.put("New Zealand", "NZ");
        region_map.put("Philippines", "PH");
        region_map.put("Poland", "PL");
        region_map.put("Portugal", "PT");
        region_map.put("Russian", "RU");
        region_map.put("Singapore", "SG");
        region_map.put("Sweden", "SE");
        region_map.put("Thailand", "TH");
        region_map.put("Taiwan", "TW");
        region_map.put("South Africa", "ZA");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "Starting application");
        setUI();
        mGoogleApiClient.connect();
        super.onStart();
    }//onStart

//    @Override
//    protected void onStop() {
//        Log.d(TAG,"Application stopped");
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }//onStop

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d("google play onConnected", "connected");
        String content = generate_log_info("",0);
//        write_to_log(content);
//
//        //Google Map Fragment Display
//        gMap.setMyLocationEnabled(true);
//        camera_move(gMap, CURRENT_LOCATION);
//        addMarker(gMap,CURRENT_LOCATION);
    }

    public String generate_log_info(String video_name, int video_size) {
        if (video_name == "") {
            video_name = "No video Played";
        }
        if (video_size == 0) {
            video_size = 0;
        }
        Location mLastLocation = getLastLocation(mGoogleApiClient);
//        LatLng CURRENT_LOCATION = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        String mLongtitudeText = String.valueOf(mLastLocation.getLongitude());
        String mLatitudeText = String.valueOf(mLastLocation.getLatitude());
        Log.i(TAG, "Last Location: " + " Long: " + mLongtitudeText + " Lat: " +mLatitudeText );
        String android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        int timestamp = (int) (new java.util.Date().getTime()/1000);
//        String date = sDateFormat.format(new java.util.Date());
        String content = "{\n" + "name:" + android_id + "\ncache:" + "\n" + "name:" + video_name + "\n" + "size:" + video_size + "\ntime:" + timestamp  +"\nlocation:" +"[" + mLongtitudeText + "," + mLatitudeText + "]" +"\n}\n";
        return content;
    }


    protected Location getLastLocation(GoogleApiClient mGoogleApiClient) {
        Log.i(TAG,"Get last location");
        Location mLastLocation;


            Log.d("google play onConnected", "connected");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLastLocation;
    }

//    // Add marker on Google Map
//    protected void addMarker(GoogleMap map, LatLng CURRENT_LOCATION) {
//        map.addMarker(new MarkerOptions()
//                .title("Sydney")
//                .snippet("The most populous city in Australia.")
//                .position(CURRENT_LOCATION));
//    }

//    // Move Camera to CURRENT_LOCATION
//    protected void camera_move(GoogleMap map, LatLng CURRENT_LOCATION) {
//        CameraPosition cameraPosition = new CameraPosition.Builder()
//                .target(CURRENT_LOCATION)
//                .zoom(14)
//                .build();
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 13));
//    }

    private void setUI() {
        Log.i(TAG, "Set UI");
        b_wifi = (Button) findViewById(R.id.b_wifi);
        b_wifi.setOnClickListener(this);

        b_ping = (Button) findViewById(R.id.b_ping);
        b_ping.setOnClickListener(this);

//        b_submit = (Button) findViewById(R.id.b_submit);
//        b_submit.setOnClickListener(this);

//        b_clear = (Button) findViewById(R.id.b_clear);
//        b_clear.setOnClickListener(this);

//        b_sendlog = (Button) findViewById(R.id.b_sendlog);
//        b_sendlog.setOnClickListener(this);

        b_writelog = (Button) findViewById(R.id.b_writelog);
        b_writelog.setOnClickListener(this);

//        b_monitor_traff = (Button) findViewById(R.id.b_mon_traff);
//        b_monitor_traff.setOnClickListener(this);

        b_search = (Button) findViewById(R.id.b_search);
        b_search.setOnClickListener(this);

        b_trending = (Button) findViewById(R.id.b_trending);
        b_trending.setOnClickListener(this);

        t_wifi = (TextView) findViewById(R.id.wifiInfo);

        t_ping = (TextView) findViewById(R.id.pingInfo);

//        t_welcome = (TextView) findViewById(R.id.welcome);

        lv_videolist = (ListView) findViewById(R.id.ls_video);
        lv_videolist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onItemVideoClicked(adapterView, view, i, l);
            }
        });

//        // Monitor data usage for every application
//        t_traff = (TextView) findViewById(R.id.traff_info);

//        et_username = (EditText) findViewById(R.id.user_name);
//        et_id = (EditText) findViewById(R.id.user_id);
        et_keyword = (EditText) findViewById(R.id.et_keyword);

        mySpinner = (Spinner)findViewById(R.id.region_code);
        region_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, region_code);
        region_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(region_adapter);
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                country_code = region_map.get(region_adapter.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
                country_code = "US";
                arg0.setVisibility(View.VISIBLE);
            }
        });

//        // Add a Map Fragment display Google Map
//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    /**
     * OnClick event handler
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b_wifi: {
                getWifiStats();
                break;
            }
            case R.id.b_ping: {
                onButtonPingClicked();
                break;
            }
//            case R.id.b_submit: {
//                onButtonSubmitClicked();
//                break;
//            }
//            case R.id.b_clear: {
//                onButtonClearClicked();
//                break;
//            }
//            case R.id.b_sendlog: {
//                onButtonSendLogClicked();
//                break;
//            }
            case R.id.b_writelog: {
                onButtonWriteJsonClicked();
                break;
            }
//            case R.id.b_mon_traff: {
//                onButtonMonitorClicked();
//            }
            case R.id.b_search: {
                onButtonSearchClicked();
                break;
            }

            case R.id.b_trending: {
                try {
                    onButtonTrendClicked();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    // Executed when the map is ready to display
    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
    }

//    public void onButtonSubmitClicked() {
//        String username = et_username.getText().toString();
//        String userid = et_id.getText().toString();
//        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
//        String date = sDateFormat.format(new java.util.Date());
//        String android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
////        String content = "********\n" + "TimeStamp:" + date + "\n" + "Username:" + username + "\n" + "User ID:" + userid + "\n";
////        String content = "{\n" + "name:" + android_id + ",\ncache:" + "{\n" + "name: GnamStyle"+ ",\nsize:" + "450000" + "\n}" +",\nd2d:5" +",\ntime:" + date + ",\n" + "loc:" + username + ",\n" + "id:" + userid +"\n" + "}\n";
////        write_to_log(content);
//        t_welcome.setText("Welcome! " + username + "(" + userid + ").\n" + "Now, you can begin your test");
//        b_ping.setEnabled(true);
//        b_wifi.setEnabled(true);
////        b_sendlog.setEnabled(true);
//        b_monitor_traff.setEnabled(true);
//        b_submit.setEnabled(false);
//        b_clear.setEnabled(false);
//        b_trending.setEnabled(true);
//        b_writelog.setEnabled(true);
//    }

//    public void onButtonClearClicked() {
//        et_username.setText("");
//        et_id.setText("");
//    }


    public void onButtonPingClicked() {
        try {
            String back_server_info = ping(PING_DEST_URL);
            t_ping.setText("rtt min/avg/max/mdev = " + back_server_info);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//    public void onButtonSendLogClicked() {
//        read_and_send();
//    }

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

    @Override
    protected void onStop() {
        onButtonWriteJsonClicked();
        super.onStop();
    }

//    public void onButtonMonitorClicked(){
//        getAppTrafficList();
//    }


    public void onButtonTrendClicked() throws ExecutionException, InterruptedException, JSONException {
        String trend_url = "https://www.googleapis.com/youtube/v3/videos?part=contentDetails,statistics,status,snippet&chart=mostPopular&regionCode=" + country_code +"&maxResults=25&key=AIzaSyAzmrXIdc2sU6zqUUhCBLsxCtoB1EtoicM";
        JSONObject trend_vid = new GetTrendVideo().execute(trend_url).get();
        JSONArray items = (JSONArray) trend_vid.get("items");
        ArrayList<ListItem> myList = new ArrayList<ListItem>();
        int size = items.length();
        for (int i = 0; i < size; i++) {
            ListItem newItem = new ListItem();
//            HashMap<String, Object> myMap = new HashMap<String, Object>();

            JSONObject single_video = (JSONObject) items.get(i);

            String vid_id = single_video.get("id").toString();
            System.out.println("ID is " + vid_id);
            newItem.setVideoID(vid_id);

            JSONObject video_snippet = (JSONObject) single_video.get("snippet");
            String video_title = video_snippet.get("title").toString();
            System.out.println("Title is " + video_title);

            JSONObject vid_thumbnails = (JSONObject) video_snippet.get("thumbnails");
            JSONObject vid_thumb_default = (JSONObject) vid_thumbnails.get("default");
            String thumb_url = vid_thumb_default.get("url").toString();
            newItem.setVideoTitle(video_title);
            newItem.setUrl(thumb_url);
            myList.add(newItem);
        }
        prettyPrint(myList);
    }

    public void read_and_send() {
//        StringBuffer info = new StringBuffer();
        String content = "";
        DeviceSchema data = new DeviceSchema();
        JSONObject jsonObject;
//        ArrayList<JSONObject> arr = new ArrayList<JSONObject>();
//        ArrayList<String> arr = new ArrayList<String>();
        int LINE_LIMIT = 10;
        int count = 0;
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                System.out.println("We do have SD card");

                String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
                File targetLocation = new File(PATH);
                if (!targetLocation.exists()) {
                    targetLocation.mkdirs();
                }
                String FILE_PATH = Environment.getExternalStorageDirectory() + "/Loginfo/" + "log.txt";
                File targetFile = new File(FILE_PATH);
                InputStream instream = new FileInputStream(targetFile);
                if (instream != null) {
                    InputStreamReader inputreader = new InputStreamReader(instream);
                    BufferedReader buffreader = new BufferedReader(inputreader);
                    String line;
                    while ((line = buffreader.readLine()) != null) {
                        content += line + "\n";
                        System.out.println("Current line is " + line);
                        if (count == 1) {   // name
                            String device_name = content.substring(7);
                            System.out.println("name is " + device_name);
                            data.name = device_name.toString();
                        } else if (count == 2) {    // cache
                            line = buffreader.readLine();
                            count++;
                            String video_name = line.substring(5);
                            System.out.println("Video Name is " + video_name);
                            line = buffreader.readLine();
                            count++;
                            String video_size = line.substring(5);
                            int video_size_int = Integer.parseInt(video_size);
                            System.out.println("Size is " + video_size_int);
                            //data.cache = new VideoSchema[]{new VideoSchema(video_name,video_size_int)};

                        } else if (count == 5) { // time
                            String time = line.toString().substring(5);
                            int time_int = Integer.parseInt(time);
                            System.out.println("Time is " + time_int);
                            data.time = time_int;
                        } else if (count == 6) { // loc
                            String longtitude = line.toString().substring(10, 22).toString();
                            String latitude = line.toString().substring(23, 33).toString();
                            double long_doub = Double.valueOf(longtitude).doubleValue();
                            double lat_doub = Double.valueOf(latitude).doubleValue();
                            System.out.println("long is " + long_doub);
                            System.out.println("Lat is " + lat_doub);
                            data.loc = new double[]{long_doub,lat_doub};
                        }
                        count++;
                        if (count == LINE_LIMIT) {
                            Gson gson = new Gson();
                            String json = gson.toJson(data);
                            Log.e("JSON",json);
                            try {
                                se = new StringEntity(json);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                                Log.e("string entity", "failed");
                            }
                            if(se != null){
                                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                            }
                            // loop 10 times for each block
                            // pressure test
//                            int t = 10;
//                            while (t > 0) {
                                client.post(null, "http://www.edward-hu.com/logs", se, "application/json", new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        Log.e("ajax", "success");
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                        Log.e("ajax", "error");
                                    }
                                });
//                                t--;
//                            }

                            //System.out.println(content + "Line Limit reached \n");
//                            jsonObject = string_to_json(content);
//                            arr.add(jsonObject);
//                            //arr.add(content);
//                            sendLog(arr);
//                            count = 0;
//                            content = "";
//                            arr.clear();
                        }
                    }
                }
            }
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
//        SearchResult singleVideo = newResult.get(i);
//        ResourceId rId = singleVideo.getId();
//        String video_name = singleVideo.getSnippet().getTitle();

//        String content = generate_log_info(video_name, 44332);
//        write_to_log(content);
//        read_and_send();
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
        String android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);

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

    public List<HashMap<String, Object>> getVideoInfoByResearchResult(Iterator<SearchResult> iteratorSearchResults){

        final List<HashMap<String, Object>> mylist = new ArrayList<HashMap<String, Object>>();
        if (!iteratorSearchResults.hasNext()) {
            Log.i(TAG," There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().getDefault();
//                URL url = new URL(thumbnail.getUrl());
//                Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("VideoTitle",singleVideo.getSnippet().getTitle());
                System.out.println("Video Title is " + singleVideo.getSnippet().getTitle());
                map.put("VideoID", rId.getVideoId());
                System.out.println("Video ID is " + rId.getVideoId());
                map.put("img", R.drawable.youtube);
                mylist.add(map);
            }
        }
        return mylist;
    }


    public void prettyPrint(ArrayList<ListItem> mylist) {
//        final SimpleAdapter adapter = new SimpleAdapter(this, mylist, R.layout.listitem,new String[] {"VideoTitle", "VideoID", "img"},new int[] {R.id.VideoTitle, R.id.VideoID, R.id.img});
//        lv_videolist.setAdapter(adapter);
        lv_videolist.setAdapter(new CustomAdapter(this, mylist));
    }

//    public JSONObject string_to_json(String str) throws JSONException {
//        Gson gson = new Gson();
//        gson.toJson(str);
//        return gson;
//        JSONObject jsonObject = new JSONObject(str);
//        return jsonObject;
//    }

//    public void sendLog(ArrayList<JSONObject> arr) throws FileNotFoundException, JSONException {
//        //String android_id = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
//        File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Loginfo/log.txt");
//        Log.i("myFile path: ", Environment.getExternalStorageDirectory().getAbsolutePath());
//        RequestParams params = new RequestParams();
//        JSONObject jsonObject = arr.get(0);
//        Iterator<String> it  =  jsonObject.keys();
//        while( it.hasNext() ){
//
//            String key = it.next();
//            Object value = jsonObject.get(key);
//
//            System.out.println(key);
//            System.out.println(value);
//            if (key.equals("location")) {
//                System.out.println("helloword");
//                String longtitude = value.toString().substring(1, 13).toString();
//                String latitude = value.toString().substring(14, 24).toString();
//                System.out.println("Longtitude is " + longtitude);
//                System.out.println("Latitude is " + latitude);
//
//                double[] loc = {Double.valueOf(longtitude).doubleValue(), Double.valueOf(latitude).doubleValue()};
//                params.put(key, loc);
//            } else if (key.equals("cache")) {
//                JSONObject tmp = new JSONObject(value.toString());
//                ArrayList<JSONObject> content = new ArrayList<JSONObject>();
//                content.add(tmp);
//                params.put(key,content);
//                content.clear();
//            } else {
//                params.put(key, value);
//            }
//        }
//
//        ajax.post("/logs", params, new AsyncHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
//                Log.e("sendLog()", "ajax executed");
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                Log.e("sendLog()", "ajax failed");
//            }
//        });
//
//    }

//    public void write_to_log(String info) {
//        try {
//            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                Log.d(TAG, "We do have SD card");
//
//                String PATH = Environment.getExternalStorageDirectory()+ "/Loginfo/";
//                File targetLocation = new File(PATH);
//                if (!targetLocation.exists()) {
//                    targetLocation.mkdirs();
//                }
//
//                // Get Directory of SD card
//                File targetFile = new File(Environment.getExternalStorageDirectory() + "/Loginfo/", "log.txt");
//                // creating RandomAccessFile object using target file
//                RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
//                // move cursor to the end of the filetest
//                raf.seek(targetFile.length());
//                // Output content to log.txt
//                String content = info;
//                raf.write(content.getBytes());
//                // close RandomAccessFile
//                raf.close();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//    public void getAppTrafficList(){
//        String res = "";
//        //Obtain app infomation and get permission info of those apps
//        PackageManager pm=getPackageManager();//获取系统应用包管理
//        //extract permission & data info from androidmanifest.xml
//        List<PackageInfo> pinfos = pm.getInstalledPackages
//                (PackageManager.GET_PERMISSIONS | PackageManager.GET_UNINSTALLED_PACKAGES);
//        for(PackageInfo info:pinfos){
//            //request permission according to androidManifest.xml
//            String[] premissions=info.requestedPermissions;
//
//            if(premissions!=null && premissions.length>0){
//                Log.i(TAG,"Permission approved");
//                //find out those apps needs INTERNET permission
//                for(String premission : premissions){
//                    if("android.permission.INTERNET".equals(premission)){
//                        //get uID
//                        int uId=info.applicationInfo.uid;
//                        long rx= TrafficStats.getUidRxBytes(uId);
//                        long tx=TrafficStats.getUidTxBytes(uId);
//                        if(rx<0 || tx<0){
//                            continue;
//                        }else{
//                            res += "rx+tx of "+info.applicationInfo.loadLabel(pm) + "is" + Formatter.formatFileSize(this, rx+tx) + "\n";
//                        }
//                    }
//                }
//            } else {
//                Log.d(TAG,"permission denied");
//            }
//        }
//        t_traff.setText(res);
//    }


    public static String ping(String host) throws IOException, InterruptedException {
        StringBuffer echo = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("ping -c 3 " + host);
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line = "";
            while ((line = buffer.readLine()) != null) {
                echo.append(line + "\n");
            }
            return getPingStats(echo.toString());
        } else if (exit == 1) {
            pingError = "failed, exit = 1";
            System.out.println(pingError);
            return null;
        } else {
            pingError = "error, exit = 2";
            System.out.println(pingError);
            return null;
        }
    }


    public static String getPingStats(String s) {
        /**
         * Format:
         * --- 127.0.0.1 ping statistics ---
         * 4 packets transmitted, 4 received, 0% packet loss, time 0ms
         * rtt min/avg/max/mdev = 0.251/0.285/0.300/0.019 ms
         **/
        if (s.contains("0% packet loss")) {
            int start = s.indexOf("/mdev = ");
            int end = s.indexOf(" ms\n", start);
            s = s.substring(start + 8, end);
            return s;
            //s = s.substring(0,2);
//            String stats[] = s.split("/");
//            return stats[2];
        } else if (s.contains("100% packet loss")) {
            pingError = "100% packet loss";
            System.out.println(pingError);
            return null;
        } else if (s.contains("% packet loss")) {
            pingError = "partial packet loss";
            System.out.println(pingError);
            return null;
        } else if (s.contains("unknown host")) {
            pingError = "unknown host";
            System.out.println(pingError);
            return null;
        } else {
            pingError = "unknown error in getPingStats";
            System.out.println(pingError);
            return null;
        }
    }


    public void getWifiStats() {
        //Wifi signal level
        String wserviceName = Context.WIFI_SERVICE;
        WifiManager wm = (WifiManager) getSystemService(wserviceName);
        WifiInfo info = wm.getConnectionInfo();
        int strength = info.getRssi();
        int speed = info.getLinkSpeed();
        String units = WifiInfo.LINK_SPEED_UNITS;
        String ssid = info.getSSID();
        String ip_addr = intToIp(info.getIpAddress());

        String text = "We are connecting to " + ssid + " at " + String.valueOf(speed) + "  " + String.valueOf(units) + " with IP addr. " + ip_addr  + ". Strength : " + strength;
        t_wifi.setText(text);
    }


    /**
     * Show the Android toast message
     * @param msg
     */
    public void showToast(final String msg) {
        Log.d(TAG, "Showing Toast: '" + msg + "'");

        if (myActiv != null) {

            myActiv.runOnUiThread(new Runnable() { // Run the Toast on the
                // Activity UI thread
                @Override
                public void run() {
                    Toast toast = Toast.makeText(testControlActivity.this, msg, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        } else {
            Toast toast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
            toast.show();
        }
    }// showToast


    /**
     * Vibrate notification
     */
    public void vibrate() {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        v.vibrate(VIBRATION_DURATION); // Vibrate time in milli sec

        // s
    }// vibrate


    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            Log.i(TAG, "API UNAVAILABLE");
        }
        Log.e(TAG, "Connection to Google Play Service Failed.");
        showToast("Connection to Google Play Service Failed.");
        vibrate();
    }

}
