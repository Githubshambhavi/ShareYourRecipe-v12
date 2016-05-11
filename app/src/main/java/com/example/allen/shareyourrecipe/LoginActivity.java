package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends AppCompatActivity {

    public String url = "http://corvette.ischool.utexas.edu/~smallya/viewuserid.php";
    private static EditText editMobNoText;
    private static EditText editPasswordText;
    private static Button loginButton;
    private static Button registerButton;

    private String mobile = "";
    private String password = "";
    public String encryptedPassword = "";
    public String encryptedPasswordDB = "";
    public String result = "0";

    ProgressDialog dialog;

    private static final String TAG_password = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editMobNoText = (EditText) findViewById(R.id.mobileNoText);
        editPasswordText = (EditText) findViewById(R.id.passwordText);
    }

    public void onButtonClick(View v) {
        mobile = editMobNoText.getText().toString();
        password = editPasswordText.getText().toString();
        if (v.getId() == R.id.loginButton) {
            if (mobile.equals("") || password.equals("")) {
                if (mobile.equals("")) {
                    editMobNoText.setHint("Enter Mobile No.");
                }
                if (password.equals("")) {
                    editPasswordText.setHint("Enter Password");
                }
            } else  {
                GetFetcher fetcher = new GetFetcher();
                fetcher.execute();

                //dialog.cancel();

            }
        }

        if (v.getId() == R.id.registerButton) {
            Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(i);
        }
    }


    private class GetFetcher extends AsyncTask<Void, Void, String> {

        ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading = ProgressDialog.show(LoginActivity.this, "Logging...", null,true,true);
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
                        encryptedPasswordDB = items.getString(TAG_password);
                        result = "1";
                        //return "true";

                    } catch (JSONException e) {
                        result = "0";
                        e.printStackTrace();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            Log.d("resultssssss", result);
            loading.dismiss();
            //super.onPostExecute(result);
            if (result.equals("1")) {
                try {
                    encryptedPassword = encrypt(password);
                    Log.d("encrypted Password", encryptedPassword);
                    Log.d("encrypted Password DB", encryptedPasswordDB);
                } catch (Exception e) {
                    Log.d("Encryption Error", "");
                }
                if (encryptedPassword.equals(encryptedPasswordDB)) {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.putExtra("MOBILE", mobile);
                    startActivity(i);
                } else {
                    Toast.makeText(LoginActivity.this, "Incorrect Credentials", Toast.LENGTH_LONG).show();
                    editMobNoText.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_dark));
                    editPasswordText.setBackgroundColor(getResources().getColor(android.R.color.secondary_text_dark));
                }
            }
            else{
                Toast.makeText(LoginActivity.this, "User does not exist OR Internet not Connected", Toast.LENGTH_LONG).show();
            }
               /* if (result.equals("false")) {
                    Toast.makeText(getApplicationContext(), "Unable to Login", Toast.LENGTH_LONG).show();

                }*/


        }
    }


    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    public static String encrypt(String text)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md;
        md = MessageDigest.getInstance("SHA-1");
        byte[] sha1hash = new byte[40];
        md.update(text.getBytes("iso-8859-1"), 0, text.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }
}

