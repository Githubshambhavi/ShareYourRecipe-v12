package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    public String url = "http://corvette.ischool.utexas.edu/~smallya/viewuserid.php";
    String name, email, mobile;
    TextView nameEditText, emailEditText, mobileText;
    // define the SharedPreferences object
    private SharedPreferences savedValues;

    // JSON node names
    private static final String TAG_USER_NAME = "name";
    private static final String TAG_EMAIL = "email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        nameEditText = (TextView) findViewById(R.id.nameEditText);
        emailEditText = (TextView) findViewById(R.id.emailEditText);
        mobileText = (TextView) findViewById(R.id.mobileTextView);
        nameEditText.setText("");
        emailEditText.setText("");
        mobileText.setText("");


        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mobile = extras.getString("MOBILE");
            Log.d("Mobile",mobile);
        }
        GetFetcher fetcher = new GetFetcher();
        fetcher.execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        // save the instance variables
        SharedPreferences.Editor editor = savedValues.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("mobile", mobile);

        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        nameEditText.setText(savedValues.getString("name", ""));
        emailEditText.setText(savedValues.getString("email", ""));
        mobileText.setText(savedValues.getString("mobile", ""));
    }

    public void onButtonClick(View v) {
        if (v.getId() == R.id.backButton) {
            // TO DO - Settings will be saved
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }

    private class GetFetcher extends AsyncTask<Void, Void, String> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(SettingsActivity.this);
            dialog.setTitle("Loading");
            dialog.setMessage("Please Wait..");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(Void... params) {


            try {

                String strUrl = url + "?mobile=" + mobile;

                String GET_URL = strUrl.replaceAll(" ", "%20");

                Log.v("URL", GET_URL);
                HttpClient client = new DefaultHttpClient();
                HttpGet get = new HttpGet(strUrl);
                HttpResponse response = client.execute(get);
                StatusLine statusLine = response.getStatusLine();

                if (statusLine.getStatusCode() == 200) {

                    //item_name = "status code if";

                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(content, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String contentString;
                    while ((contentString = streamReader.readLine()) != null)
                        responseStrBuilder.append(contentString);

                    String json_string = responseStrBuilder.toString();
                    Log.d("JSON String", json_string);
                    try {
                        JSONArray inventoryArray = new JSONArray(json_string);
                        JSONObject items = inventoryArray.getJSONObject(0);

                        name = items.getString(TAG_USER_NAME);
                        email = items.getString(TAG_EMAIL);

                        return "true";
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "false";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            nameEditText.setText(name);
            emailEditText.setText(email);
            mobileText.setText(mobile);

            dialog.cancel();
            if (result.equals("false"))
                Toast.makeText(getApplicationContext(), "Unable to Login", Toast.LENGTH_LONG).show();
        }
    }
}
