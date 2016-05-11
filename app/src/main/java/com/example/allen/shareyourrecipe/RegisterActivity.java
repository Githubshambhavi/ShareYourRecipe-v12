package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    private static Button registerButton;
    private static EditText mobileNumberEditText, passwordEditText, confirmPasswordEditText, nameEditText, emailEditText;
    private String mobile = "";
    private String password = "";
    private String confirmPassword = "";
    private String name = "";
    private String email = "";
    String encryptedPassword = "";

    public StatusLine statusLine;
    final String TAG_RESULT = "RESULT";
    public String res = "";
    public String url = "http://corvette.ischool.utexas.edu/~smallya/adduser.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mobileNumberEditText = (EditText) findViewById(R.id.mobileNumberEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
    }

    public void onButtonClick(View v) {
        mobile = mobileNumberEditText.getText().toString();
        password = passwordEditText.getText().toString();
        confirmPassword = confirmPasswordEditText.getText().toString();
        name = nameEditText.getText().toString();
        email = emailEditText.getText().toString();
        // TO DO - Add user details to Users DB
        if (mobile.equals("") || password.equals("") || confirmPassword.equals("") || name.equals("") || email.equals("")) {
            Toast.makeText(this, "Enter all fields", Toast.LENGTH_LONG).show();
        } else {
            if (isValidEmail(email)) {
                if (!(mobile.length() == 10 && isNumeric(mobile))) {
                    Toast.makeText(this, "Mobile Number must be 10 digits", Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(this, "Passwords must be min 6 characters", Toast.LENGTH_LONG).show();
                } else if (password.equals(confirmPassword)) {
                    try {
                        encryptedPassword = encrypt(password);
                    } catch (Exception e) {
                        Log.d("Encryption Error", "");
                    }
                    GetFetcher fetcher = new GetFetcher();
                    fetcher.execute();
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Enter Valid Email", Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private class GetFetcher extends AsyncTask<Void, Void, String> {

        ProgressDialog loading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            loading = ProgressDialog.show(RegisterActivity.this, "Registering...", null, true, true);
        }

        protected String doInBackground(Void... params) {

            String strUrl = url + "?mobile=" + mobile + "&password=" + encryptedPassword + "&name=" + name + "&email=" + email;

            String GET_URL = strUrl.replaceAll(" ", "%20");

            Log.v("URL", GET_URL);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(GET_URL);


            try {

                HttpResponse response = client.execute(get);
                statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(content, "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String contentString;
                    while ((contentString = streamReader.readLine()) != null)
                        responseStrBuilder.append(contentString);

                    String json_string = responseStrBuilder.toString();
                    if (json_string != null) {
                        try {
                            JSONObject result = new JSONObject(json_string);
                            res = result.getString(TAG_RESULT);
                            Log.d("RESULT", res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(RegisterActivity.this, "Error", duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return res;
        }

        protected void onPostExecute(String result) {
            Log.d("Insert Result", res);
            loading.dismiss();
            if (res.equals("1")) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
            } else {
                Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_LONG).show();
            }
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
