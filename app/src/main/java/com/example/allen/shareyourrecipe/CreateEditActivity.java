package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

public class CreateEditActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    EditText recipeNameEdit, authorEdit, ingredientsListEdit, descriptionEdit;
    ImageView image;
    byte[] encodedImageByte = null;
    String date, id;
    Bitmap bp;
    private static int RESULT_LOAD_IMG = 1;
    String imgPath;
    RequestParams params = new RequestParams();

    private static final String TAG = "MyActivity";
    public String createUrl = "http://corvette.ischool.utexas.edu/~smallya/insertrecipe.php";
    public String updateUrl = "http://corvette.ischool.utexas.edu/~smallya/update_recipe.php";
    private String methodToPerform;
    public StatusLine statusLine;
    public String res = "0";
    final String TAG_RESULT = "RESULT";
    ProgressDialog dialog;

    Bitmap bitmap;

    String mobile, recipename, recipeAuthor, ingredients, method, fileName, path, dateCreated;
    String encodedString;
    ProgressDialog prgDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit);

        // this section gets all the GUI widget ID's for use in the Listeners
        recipeNameEdit = (EditText) findViewById(R.id.recipeNameEditText);
        authorEdit = (EditText) findViewById(R.id.authorNameEditText);
        ingredientsListEdit = (EditText) findViewById(R.id.ingredientsEditText);
        descriptionEdit = (EditText) findViewById(R.id.descriptionEditText);
        image = (ImageView) findViewById(R.id.imageView);

        prgDialog = new ProgressDialog(this);
        prgDialog.setCancelable(false);

        // to store today's date in the DB
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        date = df.format(c.getTime());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("ID") != null) {
                methodToPerform = "Update";
                id = extras.getString("ID");
                recipename = extras.getString("RECIPENAME");
                ingredients = extras.getString("INGREDIENTS");
                method = extras.getString("DESCRIPTION");
                path = extras.getString("RECIPEIMAGE");
                recipeAuthor = extras.getString("AUTHOR");
                dateCreated = extras.getString("DATECREATED");

                recipeNameEdit.setText(recipename);
                ingredientsListEdit.setText(ingredients);
                descriptionEdit.setText(method);
                authorEdit.setText(recipeAuthor);
                //editRecipe(id);


            } else {
                methodToPerform = "Create";
            }
            mobile = extras.getString("MOBILE");
        }

        if (savedInstanceState != null) {
            image.setImageBitmap((Bitmap) savedInstanceState.getParcelable("BitmapImage"));
            bp = savedInstanceState.getParcelable("BitmapImage");
        } else {
            image.setImageResource(R.mipmap.camera);
        }


    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("BitmapImage", bp);
    }

    // All button operations

    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.cameraButton:
                /*Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);*/

                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                // Start the Intent
                startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
                break;
            case R.id.saveButton:
                // id = null means it is a create scenario
                recipename = recipeNameEdit.getText().toString();
                recipeAuthor = authorEdit.getText().toString();
                ingredients = ingredientsListEdit.getText().toString();
                method = descriptionEdit.getText().toString();
                if (imgPath != null && !imgPath.isEmpty()) {
                    prgDialog.setMessage("Saving Data");
                    prgDialog.show();
                    // Convert image to String using Base64
                    encodeImagetoString();
                    /*
                    // When Image is not selected from Gallery
                    dialog = new ProgressDialog(CreateEditActivity.this);
                    dialog.setTitle("Connecting to Server");
                    dialog.setMessage("Please Wait...");
                    dialog.show();
                    dialog.setCancelable(false);
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Log.d("Thread Error", "");
                    }
                    dialog.cancel();*/


                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "You must select image from gallery before you try to upload",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.cancelButton:
                Intent j = new Intent(this, MainActivity.class);
                j.putExtra("MOBILE", mobile);
                startActivity(j);
                break;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (data != null) {
            bp = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(bp);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bp.compress(Bitmap.CompressFormat.PNG, 0, baos);
            encodedImageByte = baos.toByteArray();
        }*/

        try {

            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgPath = cursor.getString(columnIndex);
                cursor.close();

                // Set the Image in ImageView
                image.setImageBitmap(BitmapFactory
                        .decodeFile(imgPath));
                // Get the Image's file name


                String fileNameSegments[] = imgPath.split("/");
                fileName = fileNameSegments[fileNameSegments.length - 1];
                // Put file name in Async Http Post Param which will used in Php web app
                params.put("filename", fileName);

            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();

            }

        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }


    }

    private void encodeImagetoString() {
        new AsyncTask<Void, Void, String>() {

            protected void onPreExecute() {
            }

            ;

            @Override
            protected String doInBackground(Void... params) {
                BitmapFactory.Options options = null;
                options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                bitmap = BitmapFactory.decodeFile(imgPath,
                        options);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy
                bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                prgDialog.setMessage("Calling Upload");
                // Put converted Image string into Async Http Post param
                params.put("image", encodedString);
                // Trigger Image upload
                triggerImageUpload();
            }
        }.execute(null, null, null);
    }

    public void triggerImageUpload() {
        makeHTTPCall();
    }

    // Make Http call to upload Image to Php server
    public void makeHTTPCall() {
        prgDialog.setMessage("Uploading..");
        AsyncHttpClient client = new AsyncHttpClient();
        // Don't forget to change the IP address to your LAN address. Port no as well.
        client.post("http://corvette.ischool.utexas.edu/~smallya/image_upload.php",
                params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        prgDialog.hide();

                        path = "http://corvette.ischool.utexas.edu/~smallya/uploadedimages/" + fileName;
                        //Toast.makeText(getApplicationContext(), "Success" + "\n" + path, Toast.LENGTH_LONG).show();


                        backgroundTask backTask = new backgroundTask(CreateEditActivity.this);

                        backTask.execute();


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        // Hide Progress Dialog
                        prgDialog.hide();
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Error Occured n Most Common Error: n1. Device not connected to Internetn2. Web App is not deployed in App servern3. App server is not runningn HTTP Status code : "
                                            + statusCode, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the progress bar when application is closed
        if (prgDialog != null) {
            prgDialog.dismiss();
        }
    }

    private class backgroundTask extends AsyncTask<String, Void, String> {
        Context ctx;
        String strUrl;
        ProgressDialog dialog;

        backgroundTask(Context ctx) {
            this.ctx = ctx;

        }

        @Override
        protected void onPreExecute() {


            super.onPreExecute();
            dialog = ProgressDialog.show(CreateEditActivity.this, "Uploading.....", null, true, true);

        }

        @Override
        protected String doInBackground(String... urls) {

            //http://corvette.ischool.utexas.edu/~smallya/insertrecipe.php?recipes


            if (methodToPerform.equals("Create")) {
                strUrl = createUrl + "?recipes=" + recipename + "&author=" + recipeAuthor + "&date=" + date +
                        "&ingre=" + ingredients + "&desc=" + method + "&path=" + path + "&user_id=" + mobile;
            } else {
                strUrl = updateUrl + "?recipes=" + recipename + "&author=" + recipeAuthor + "&date=" + date +
                        "&ingre=" + ingredients + "&desc=" + method + "&path=" + path + "&user_id=" + mobile +
                        "&id=" + id;
            }
            String aURL = strUrl.replaceAll(" ", "%20");
            String URL = aURL.replaceAll("\n", "%0A");
            Log.v("URL", URL);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(URL);


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
                            //Log.d("RESULT", res);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.d("****************", "RESULT ************************");
                        Log.d("TAG1", "fffffffffffffffffffffffff");

                        Log.d("RESULT", res);


                    }
                } else {
                    CharSequence error_msg = "SOME ERROR";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(CreateEditActivity.this, error_msg, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();

                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Success";
        }

        protected void onPostExecute(String result) {
            dialog.dismiss();
            Intent i = new Intent(CreateEditActivity.this, ViewActivity.class);

            if (id != null) {
                i.putExtra("ID", id);
            }

            i.putExtra("MOBILE", mobile);
            i.putExtra("RECIPENAME", recipename);
            i.putExtra("INGREDIENTS", ingredients);
            i.putExtra("DESCRIPTION", method);
            i.putExtra("RECIPEIMAGE", path);
            i.putExtra("AUTHOR", recipeAuthor);
            i.putExtra("DATECREATED", date);
            i.putExtra("USERID", mobile);
            startActivity(i);

            Intent j = new Intent();
            j.setAction("Created");
            CreateEditActivity.this.sendBroadcast(j);
        }
    }

}
