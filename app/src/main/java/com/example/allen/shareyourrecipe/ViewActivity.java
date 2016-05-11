package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;

public class ViewActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    String id, name, method, ingredients, imageurl, recipeAuthor, dateCreated, mobile, userId;
    TextView recipeName, date, author, ingredientsList, description;
    ImageView image;
    Boolean deleteResult = false;
    public StatusLine statusLine;
    ProgressDialog dialog;
    public String deleteUrl = "http://corvette.ischool.utexas.edu/~smallya/delete.php";
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);

        myDb = new DatabaseHelper(this);

        // this section gets all the GUI widget ID's for use in the Listeners
        recipeName = (TextView) findViewById(R.id.recipeName);
        author = (TextView) findViewById(R.id.author);
        date = (TextView) findViewById(R.id.date);
        ingredientsList = (TextView) findViewById(R.id.ingredientsList);
        description = (TextView) findViewById(R.id.description);
        image = (ImageView) findViewById(R.id.imageView);

        deleteButton = (Button) findViewById(R.id.deleteButton);


        dialog = new ProgressDialog(ViewActivity.this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mobile = extras.getString("MOBILE");
            name = extras.getString("RECIPENAME");
            ingredients = extras.getString("INGREDIENTS");
            method = extras.getString("DESCRIPTION");
            imageurl = extras.getString("RECIPEIMAGE");
            recipeAuthor = extras.getString("AUTHOR");
            dateCreated = extras.getString("DATECREATED");
            if (extras.getString("USERID") != null) {
                userId = extras.getString("USERID");
            }
            if (extras.getString("ID") != null) {
                id = extras.getString("ID");
            }
        }
        //viewRecipe(id);

        recipeName.setText(name);
        ingredientsList.setText(ingredients);
        description.setText(method);
        date.setText(dateCreated);
        author.setText(recipeAuthor);

        new DownloadImageTask(image).execute(imageurl);

        /*
        // TODO Use contacts info to fetch from DB - Not here, to be used in Main
        // START - testing contacts

        Cursor c = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        String name, number = "";
        String contactId;
        c.moveToFirst();
        for(int i = 0; i < 5; i++) {
            name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

            if (Integer.parseInt(c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contactId },
                        null);
                while (pCur.moveToNext()) {
                    number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            }
            Log.i("name ", name + " ");
            Log.i("number ", number + " ");
            c.moveToNext();
        }*/

        if (mobile.equals(userId)) {
            deleteButton.setEnabled(true);
        } else {
            deleteButton.setEnabled(false);
            deleteButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }


    // All button operations
    public void onButtonClick(View v) {
        switch (v.getId()) {
            case R.id.editButton:
                Intent i = new Intent(this, CreateEditActivity.class);
                i.putExtra("ID", id);
                i.putExtra("RECIPENAME", name);
                i.putExtra("INGREDIENTS", ingredients);
                i.putExtra("DESCRIPTION", method);
                i.putExtra("RECIPEIMAGE", imageurl);
                i.putExtra("AUTHOR", recipeAuthor);
                i.putExtra("DATECREATED", dateCreated);
                i.putExtra("MOBILE", mobile);
                startActivity(i);
                break;

            case R.id.backButton:
                Intent j = new Intent(this, MainActivity.class);
                j.putExtra("MOBILE", mobile);
                startActivity(j);
                break;

            case R.id.deleteButton:

                backgroundTask backTask = new backgroundTask(this);
                backTask.execute();
                Intent k = new Intent(this, MainActivity.class);
                k.putExtra("MOBILE", mobile);
                startActivity(k);
                break;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }

    private class backgroundTask extends AsyncTask<String, Void, String> {
        Context ctx;

        backgroundTask(Context ctx) {
            this.ctx = ctx;

        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Deleting...");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... urls) {

            //http://corvette.ischool.utexas.edu/~smallya/insertrecipe.php?recipes

            String strUrl = deleteUrl + "?ID=" + id;
            String DELETE_URL = strUrl.replaceAll(" ", "%20");
            Log.v("URL", DELETE_URL);
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(DELETE_URL);


            try {

                HttpResponse response = client.execute(get);
                statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();

                    deleteResult = true;
                    Log.d("RESULT", "Deleted");
                    return "Deleted";
                } else {
                    CharSequence error_msg = "SOME ERROR";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(ViewActivity.this, error_msg, duration);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            deleteResult = false;
            return "Delete Failed";
        }

        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            dialog.cancel();
            if (deleteResult)
                Toast.makeText(getApplicationContext(), "Recipe Deleted", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getApplicationContext(), "Recipe NOT Deleted", Toast.LENGTH_LONG).show();
            if (result.equals("Delete Failed"))
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();

        }
    }

}
