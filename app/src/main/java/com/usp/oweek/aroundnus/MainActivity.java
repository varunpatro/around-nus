package com.usp.oweek.aroundnus;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends ActionBarActivity {

    private Button scan;
    SharedPreferences settings;
    public static int HOUSE_NUM = 1;
    public TextView currHouse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getPreferences(MODE_PRIVATE);
        HOUSE_NUM = settings.getInt("HOUSE_NUM", HOUSE_NUM);

        scan = (Button)findViewById(R.id.btnScan);

        scan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        currHouse = (TextView) findViewById(R.id.currHouse);
        currHouse.setText("Hi " + current_house());
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");

                // Handle successful scan

                JSONObject data = new JSONObject();
                try {
                    data.put("location", Integer.parseInt(contents));
                } catch (Exception e) {
                    Log.i("ex", e.toString());
                }

                sync(data);


            } else if (resultCode == RESULT_CANCELED) {
                // Handle cancel
                Log.i("App","Scan unsuccessful");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.set_house:
                setHOUSE_NUM();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected String current_house() {
        switch(HOUSE_NUM) {
            case 0:
                return "Ianthe";
            case 1:
                return "Saren";
            case 2:
                return "Nocturna";
            case 3:
                return "Ursaisa";
            case 4:
                return "Triton";
            case 5:
                return "Ankaa";
            default:
                setHOUSE_NUM();
                return "Please set a house.";
        }
    }

    protected void setHOUSE_NUM() {

        CharSequence houses[] = new CharSequence[] {"Ianthe", "Saren", "Nocturna", "Ursaia", "Triton", "Ankaa"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select your house");
        builder.setItems(houses, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                HOUSE_NUM = which;
                currHouse.setText("Hi " + current_house());
            }
        });
        builder.show();
    }

    public void sync(JSONObject data) {
        int house = HOUSE_NUM;

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String sync_url = "http://vpatro.me:8080/tasks/request.json?";
        String sync_params = "";
        try {
            sync_params = "house=" + HOUSE_NUM + "&" + "location=" + data.getInt("location");
        } catch(Exception e){};
        sync_url += sync_params;
        final TextView taskLoc = (TextView) findViewById(R.id.taskLoc);
        final TextView taskDesc = (TextView) findViewById(R.id.taskDesc);
        taskLoc.setText("Retrieving task details...");

        // Prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, sync_url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());

                        try {
                            taskLoc.setText(response.getString("taskLoc"));
                            taskDesc.setText(response.getString("taskDesc"));
                        } catch(Exception e) {
                            taskLoc.setText("");
                            taskDesc.setText(e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        taskLoc.setText("Error: Cannot connect to server");
                        taskDesc.setText("Please connect to the internet and try again.");
                        taskDesc.setText(error.toString());
                    }
                }

        );

//      add it to the RequestQueue
        queue.add(getRequest);


    }

    public void test() {
        Toast.makeText(MainActivity.this,
                "test", Toast.LENGTH_SHORT).show();
    }

    protected void onPause() {
        super.onPause();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("HOUSE_NUM", HOUSE_NUM);

        // Commit the edits!
        editor.commit();
    }
}