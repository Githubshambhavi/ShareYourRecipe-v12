package com.example.allen.shareyourrecipe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

public class RecipeAdapter extends ArrayAdapter<RecipeItem> implements Filterable{


    ArrayList<RecipeItem> recipeList;
    ArrayList<RecipeItem> filterList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;
    CustomFilter filter;
    ArrayList<RecipeItem> items;
    RecipeItem recipes;


    public RecipeAdapter(Context context, int resource, ArrayList<RecipeItem> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        recipeList = objects;
        filterList = objects;
        items = objects;
        Log.d("****************", "***********");
        Log.d("Scope", "RecipeAdapter");
        Log.d("****************", "***********");

    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter == null){
            filter = new CustomFilter();
        }
        return filter;
    }
    //Inner class
    class CustomFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                System.out.println("Inside query..................");
                // query to upper
                constraint = constraint.toString().toUpperCase();
                //HOLD our filter
                ArrayList<RecipeItem> filters = new ArrayList<>();
                System.out.println("QUERY IS ..." + constraint);
                //get specific items
                for (int i = 0; i < filterList.size(); i++) {
                    if (filterList.get(i).getRecipeName().toUpperCase().contains(constraint)) {

                        System.out.println("Filtered recipe list is ............." + filterList);
                        recipes = new RecipeItem(filterList.get(i).getId(), filterList.get(i).getRecipeName(),
                                filterList.get(i).getRecipeImage(), filterList.get(i).getAuthor(), filterList.get(i).getDateCreated(),
                                filterList.get(i).getIngredients(), filterList.get(i).getDescription(), filterList.get(i).getUserId());
                        System.out.println("Adding to filters ............." + filterList);
                        filters.add(recipes);


                    }
                    results.count = filters.size();
                    results.values = filters;
                }


            }else {
                results.count = recipeList.size();
                results.values = recipeList;

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            recipeList=(ArrayList<RecipeItem>) results.values;
            System.out.println("Recipe list published ......" + recipeList);
            notifyDataSetChanged();

        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v;
        Log.d("****************", "***********");
        Log.d("Scope", "entered");
        Log.d("****************", "***********");

        holder = new ViewHolder();
        v = vi.inflate(Resource, null);
        holder.imageview = (ImageView) v.findViewById(R.id.imageView1);
        holder.recipeName = (TextView) v.findViewById(R.id.textView1);
        v.setTag(holder);

        holder.imageview.setImageResource(R.mipmap.curry);
        new DownloadImageTask(holder.imageview).execute(recipeList.get(position).getRecipeImage());
        holder.recipeName.setText(recipeList.get(position).getRecipeName());

        return v;
    }


    public class ViewHolder {
        public ImageView imageview;
        public TextView recipeName;
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
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}