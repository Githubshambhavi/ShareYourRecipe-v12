package com.example.allen.shareyourrecipe;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    String mobile;
    public String res = null;
    ArrayList<RecipeItem> recipeList;
    Context context;
    RecipeAdapter adapter;
    ListView listview;
    SearchView inputSearch;


    // JSON node names
    private static final String TAG_NAME = "RECIPENAME";
    private static final String TAG_ID = "ID";
    private static final String TAG_IMAGE = "RECIPEIMAGE";
    private static final String TAG_AUTHOR = "AUTHOR";
    private static final String TAG_DATE = "DATECREATED";
    private static final String TAG_INGREDIENTS = "INGREDIENTS";
    private static final String TAG_DESC = "DESCRIPTION";
    private static final String TAG_USER_ID = "USERID";

    public static String list_url = "http://corvette.ischool.utexas.edu/~smallya/show_recipes.php";

    DataManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputSearch = (SearchView) findViewById(R.id.searchView);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mobile = extras.getString("MOBILE");
        }

        context = this;
        new JSONAsyncTask().execute(list_url);

        listview = (ListView) findViewById(R.id.listView);

        hidekeyboard();



    }

    private void hidekeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(MainActivity.this.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // this is for the menu at the top right
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        return true;
    }

    // this is for the menu at the top right
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Intent i = new Intent(this, SettingsActivity.class);
                i.putExtra("MOBILE", mobile);
                startActivity(i);
                return true;
            case R.id.notify:
                if (item.isChecked()) {
                    item.setChecked(false);
                } else
                    item.setChecked(true);
                return true;
            case R.id.logout:
                Intent j = new Intent(this, LoginActivity.class);
                startActivity(j);
                finish();
        }
        return false;
    }

    // All button operations
    public void addRecipe(View v) {
        Intent k = new Intent(this, CreateEditActivity.class);
        k.putExtra("MOBILE", mobile);
        startActivity(k);
    }




    class JSONAsyncTask extends AsyncTask<String, Void, Boolean> {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... urls) {


            manager = DataManager.getInstance();
            Boolean returnStatus = manager.listItems(MainActivity.this,urls);
            return returnStatus;

        }



        protected void onPostExecute(Boolean result) {
            //super.onPostExecute(result);

            recipeList = manager.getItems();
            adapter = new RecipeAdapter(MainActivity.this, R.layout.program_list, recipeList);

            // adding items to listview
            listview.setAdapter(adapter);

            inputSearch.setOnQueryTextListener(new OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    Toast.makeText(getBaseContext(),newText ,Toast.LENGTH_LONG).show();
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });

            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                        long id) {
                    //Toast.makeText(getApplicationContext(), recipeList.get(position).getRecipeName() + "\n" + recipeList.get(position).getId(), Toast.LENGTH_LONG).show();

                    Intent i = new Intent(MainActivity.this, ViewActivity.class);
                    i.putExtra(TAG_ID, recipeList.get(position).getId());
                    i.putExtra(TAG_NAME, recipeList.get(position).getRecipeName());
                    i.putExtra(TAG_INGREDIENTS, recipeList.get(position).getIngredients());
                    i.putExtra(TAG_DESC, recipeList.get(position).getDescription());
                    i.putExtra(TAG_IMAGE, recipeList.get(position).getRecipeImage());
                    i.putExtra(TAG_AUTHOR, recipeList.get(position).getAuthor());
                    i.putExtra(TAG_DATE, recipeList.get(position).getDateCreated());
                    i.putExtra(TAG_USER_ID, recipeList.get(position).getUserId());
                    i.putExtra("MOBILE", mobile);
                    startActivity(i);
                }
            });

            dialog.cancel();
            adapter.notifyDataSetChanged();
            if (!result)
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();

        }
    }
}
