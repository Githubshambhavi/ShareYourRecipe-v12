package com.example.allen.shareyourrecipe;

import android.content.Context;
import android.util.Log;

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

/**
 * Created by shwetha on 5/3/2016.
 * Singleton class to perform db activities
 */
public class DataManager {

    private static DataManager sInstance_ = null;

    // JSON node names
    private static final String TAG_NAME = "RECIPENAME";
    private static final String TAG_ID = "ID";
    private static final String TAG_IMAGE = "RECIPEIMAGE";
    private static final String TAG_AUTHOR = "AUTHOR";
    private static final String TAG_DATE = "DATECREATED";
    private static final String TAG_INGREDIENTS = "INGREDIENTS";
    private static final String TAG_DESC = "DESCRIPTION";
    private static final String TAG_USER_ID = "USERID";

    //  public static String list_url = "http://corvette.ischool.utexas.edu/~smallya/show_recipes.php";

    public ArrayList<RecipeItem> mItems_ = null, recipeList = null;

    public String item_id, item_name, item_user_id, item_image, item_author, item_ingredients, item_desc, item_date;
    private ArrayList<RecipeItem> recipes;


    protected DataManager() {
        // to defeat instantiation
    }

    public static DataManager getInstance() {
        if (sInstance_ == null) {
            sInstance_ = new DataManager();
        }
        return sInstance_;
    }


    public Boolean listItems(Context activityContext, String... urls) {

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(urls[0]);
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();
            //Clear mItems_
            mItems_ = null;


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

                if (json_string != null) {

                    //item_name = "json if";

                    Log.d("JSON STRING", json_string);

                    try {
                        JSONArray inventoryArray = new JSONArray(json_string);
                        // Getting JSON Array node
                        // docs = jsonObj.getJSONArray(TAG_DOCS);

                        //item_name = "try block";


                        mItems_ = new ArrayList<RecipeItem>();
                        recipeList = new ArrayList<RecipeItem>();

                        for (int iter = 0; iter < inventoryArray.length(); iter++) {

                            //item_name = "";
                            JSONObject items = inventoryArray.getJSONObject(iter);

                            //item_name = items.getString(TAG_NAME);
                            //item_name = "loop";

                            item_id = items.getString(TAG_ID);
                            item_name = items.getString(TAG_NAME);
                            item_image = items.getString(TAG_IMAGE);
                            item_author = items.getString(TAG_AUTHOR);
                            item_ingredients = items.getString(TAG_INGREDIENTS);
                            item_desc = items.getString(TAG_DESC);
                            item_user_id = items.getString(TAG_USER_ID);
                            item_date = items.getString(TAG_DATE);

                            RecipeItem recipe_item = new RecipeItem(item_id, item_name, item_image, item_author, item_date, item_ingredients, item_desc, item_user_id);

                            //For the intent
                            mItems_.add(recipe_item);

                            // For adapter
                            //TODO: use only one type of list, mItems_ or recipeList
                            recipeList.add(recipe_item);

                            setItems(mItems_, recipeList);

                            Log.d("**ITEM NAME**", item_name);
                        }

                        return true;


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            } else {

                Log.d("JSON ERROR", "ELSE executed");

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // item_name = "test";

        return false;

    }

    public void setItems(ArrayList<RecipeItem> mItems_, ArrayList<RecipeItem> recipeList) {
        //this. = mItems_;
        this.recipes = recipeList;

    }

    public ArrayList<RecipeItem> getItems() {

        return this.recipes;

    }


}
