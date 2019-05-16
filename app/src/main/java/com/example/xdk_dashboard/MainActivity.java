package com.example.xdk_dashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private TextView description;
    private TextView sensor;
    private ListView dataList;
    private final String url = "https://fas-webteach.sunderland.ac.uk/~ex0eby/getValuesOfDatabase.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        description = findViewById(R.id.description);
        sensor = findViewById(R.id.sensor);
        dataList = findViewById(R.id.dataList);

        description.setText(
                "Welcome to the XDK110 Dashboard\n\n" +
                        "Here You can see the live values of the different sensors of your XDK110 " +
                        "as well as the last saved values."
        );

        downloadJSON(url, "");

        //MQTT
        /*String clientId = MqttClient.generateClientId();
        MqttAndroidClient client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d("MQTTtest", "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d("MQTTtest", "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }*/

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            //setContentView(R.layout.activity_home);
            findViewById(R.id.activity_home).setVisibility(View.VISIBLE);
            findViewById(R.id.content_main).setVisibility(View.GONE);
        } else {
            //setContentView(R.layout.activity_main);
            findViewById(R.id.activity_home).setVisibility(View.GONE);
            findViewById(R.id.content_main).setVisibility(View.VISIBLE);
            if (id == R.id.nav_light) {
                sensor.setText("Light Sensor\n(in milli lux)");
                downloadJSON(url, "Digital_Light");
            } else if (id == R.id.nav_temperature) {
                sensor.setText("Temperature Sensor\n(in milli degres)");
                downloadJSON(url, "Temperature");
            } else if (id == R.id.nav_humidity) {
                sensor.setText("Humidity Sensor\n(in % relative humidity)");
                downloadJSON(url, "Humidity");
            }else if (id == R.id.nav_pressure) {
                sensor.setText("Pressure Sensor\n(in Pa)");
                downloadJSON(url, "Pressure");
            }else if (id == R.id.nav_acoustic) {
                sensor.setText("Acoustic Sensor\n(in Pa)");
                downloadJSON(url, "Acoustic");
            } else if (id == R.id.nav_accelerometer) {
                sensor.setText("Accelerometer Sensor\n(in m/s2)");
                downloadJSON(url, "Accelerometer");
            } else if (id == R.id.nav_gyroscope) {
                sensor.setText("Gyroscope Sensor\n(in mDeg");
                downloadJSON(url, "Gyroscope");
            } else if (id == R.id.nav_magnetometer) {
                sensor.setText("Magnetometer Sensor\n(in mT)");
                downloadJSON(url, "Magnetometer");
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void downloadJSON(final String urlWebService, final String sensor) {

        class DownloadJSON extends AsyncTask<Void, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }


            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    loadIntoListView(s, sensor);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    URL url = new URL(urlWebService);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine()) != null) {
                        sb.append(json + "\n");
                    }
                    return sb.toString().trim();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        DownloadJSON getJSON = new DownloadJSON();
        getJSON.execute();
    }

    private void loadIntoListView(String json, String sensor) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        String[] values = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            switch(sensor) {
                case "Accelerometer":
                case "Gyroscope":
                case "Magnetometer":
                    values[jsonArray.length()-1-i] = obj.getString("Date") + " --- X : "
                            + obj.getString(sensor+"X") + "   Y : "
                            + obj.getString(sensor+"Y") + "   Z : "
                            + obj.getString(sensor+"Z");
                    break;
                default:
                    values[jsonArray.length()-1-i] = obj.getString("Date") + " --- Value : "
                            + obj.getString(sensor) + "    ";
                    break;

            }
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        dataList.setAdapter(arrayAdapter);
    }
}
