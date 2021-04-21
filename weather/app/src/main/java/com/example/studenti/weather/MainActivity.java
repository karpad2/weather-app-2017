package com.example.studenti.weather;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    private ArrayAdapter<String> mForecastAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        String [] weatherData={
                "Today Sunny - 25/18",
                "Tomorrow Sunny - 25/18",
                "Weds not Sunny - 25/18",
                "Thurs rain - 95/18",
                "Fri Heavy rain - 15/11",
                "Sat Sunny - 26/12",
                "Sun Sunny - 35/21",
        };

        List<String> weekForecast=new ArrayList<String>(Arrays.asList(weatherData));

        mForecastAdapter=new ArrayAdapter<String>(
                this,
                R.layout.list_item_layout1,
                R.id.list_item_textview,
                //data
                weatherData);
        //  weekForecast);


        ListView list=(ListView)findViewById(R.id.listview_weather);
        list.setAdapter(mForecastAdapter);}
    public void onClick(View v){
        if(checkInternetConenction()) {
            if (wifiConnected)
                Toast.makeText(this, "WIFI network connection!", Toast.LENGTH_LONG).show();
            if (mobileConnected)
                Toast.makeText(this, "Mobile network connection!", Toast.LENGTH_LONG).show();
            DownLoad task_download = new DownLoad();

            task_download.execute("3189595");

        }
        else
            Toast.makeText(this, "No network connection available!!!", Toast.LENGTH_LONG).show();
    }
    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check for network connections
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            wifiConnected = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
            mobileConnected = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
            return true;
        } else {
            return false;
        }

    }
    private class DownLoad extends AsyncTask<String, Void, String>{
        HttpURLConnection urlConnection=null;
        BufferedReader reader=null;



        @Override
        protected String doInBackground(String... params) {
            String format="json";
            String units="metric";
            int numDays=7;
            String code="5459d1266c4c46f65063264f743f6b15";


            String json_data="";
            try{

                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String  QUERY_PARAM="id";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";
                final String CODE_PARAM="APPID";
                Uri builtUri=Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .appendQueryParameter(CODE_PARAM, code)
                        .build();

                URL url=new URL(builtUri.toString());
                urlConnection=(HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                int response =urlConnection.getResponseCode();
                Log.d("doInBack", "Respons is "+ response);
                InputStream inputStream=urlConnection.getInputStream();
                if (inputStream==null)
                    return null;
                StringBuffer buffer=new StringBuffer();
                String line="";
                reader=new BufferedReader(new InputStreamReader(inputStream));
                while ((line=reader.readLine())!=null){
                    buffer.append(line);
                }
                if (buffer.length()==0)
                    return null;

                json_data=buffer.toString();
                Log.d("doinbackg",json_data);

            }catch (Exception e){
                return null;
            }
            return json_data;
        }
        @Override
        protected void onPostExecute(String s) {
            getData(s);
        }
    }

    private void getData(String res){
        Log.d("begin","Im here");
        String b="";
        JSONObject tmpa=null, tmpb=null, list=null;
        JSONArray array=null;
        ArrayAdapter<String> adapter;
        ListView lv=findViewById(R.id.listview_weather);
        List<String> add;
        add = new ArrayList<String>();
            try {
                list = new JSONObject(res);
                Log.d("err",list.toString());
                array = list.getJSONArray("list");
            } catch (Exception ex) {
                Log.i("Error", ex.toString());
            }
            for (int i = 0; i < array.length(); i++) {
                b = "";
                try {
                    tmpa = array.getJSONObject(i);
                    tmpb = tmpa.getJSONObject("temp");
                } catch (Exception ex) {
                    Log.w("Error", "next_one");
                }
                try {
                    b += tmpb.getString("min") + "/" + tmpb.getString("max") + ", ";
                } catch (Exception ex) {
                    Log.w("string", "concatt1:" + ex.toString());
                }
                try {
                    tmpb = tmpa.getJSONArray("weather").getJSONObject(0);
                } catch (Exception ex) {
                    Log.w("string", "transfrom: " + ex.toString());
                }
                try {
                    b += tmpb.getString("main") + ", Wind=" + tmpa.getString("speed");
                } catch (Exception ex) {
                    Log.w("string", "concatt2: " + ex.toString());
                }
                add.add(b);
            }
            adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, add.toArray(new String[0]));
            try {
                lv.setAdapter(adapter);
            } catch (Exception ex) {
                Log.w("ListView", ex.toString());
            }
        }

 @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.new_game:
                newGame();
                return true;
            case R.id.help:
                showHelp();
                return true;
            case R.id.help2:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newGame() {
        Toast.makeText(this,
                "New Game menu",
                Toast.LENGTH_LONG).show();
    }
    private void showHelp() {
        Toast.makeText(this,
                "Help menu",
                Toast.LENGTH_LONG).show();
    }
}