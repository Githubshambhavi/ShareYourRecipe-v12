package com.example.allen.shareyourrecipe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.Blob;
import java.util.Date;

/**
 * Created by allen on 23-03-2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // declare the DB name here
    public static final String DATABASE_NAME = "Recipes.db";

    // now declare the TABLE name that will be part of the DB
    public static final String TABLE_NAME = "Recipes";

    // declare the COLUMNS of the TABLE
    public static final String COL_1 = "ID";
    public static final String COL_2 = "RECIPEIMAGE";
    public static final String COL_3 = "RECIPENAME";
    public static final String COL_4 = "AUTHOR";
    public static final String COL_5 = "DATECREATED";
    public static final String COL_6 = "INGREDIENTS";
    public static final String COL_7 = "DESCRIPTION";

    // this is referencing the java class that will manage the SQL DB
    public DatabaseHelper(Context context) {

        // whenever the constructor below is called, our DB will now be created
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // this is the execute sql query method that takes a string sql query and executes this query
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,RECIPEIMAGE BLOB, RECIPENAME TEXT, AUTHOR TEXT, DATECREATED TEXT, INGREDIENTS TEXT, DESCRIPTION TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // upgrade the table if version number is increased and call onCreate to create a new DB
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME);
        onCreate(db);

    }

    public boolean insertData(byte[] recipeImage, String recipeName, String author, String dateCreated, String ingredients, String description) {

        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // This class is used to store a set of values that a ContentResolver can process.
        ContentValues contentValues = new ContentValues();

        // you need to specify the column and the data for that column

        contentValues.put(COL_2,recipeImage);
        contentValues.put(COL_3,recipeName);
        contentValues.put(COL_4,author);
        contentValues.put(COL_5,dateCreated);
        contentValues.put(COL_6,ingredients);
        contentValues.put(COL_7,description);

        // need to give this the table name and the content values
        long result = db.insert(TABLE_NAME, null, contentValues);

        // method will return -1 if the insert did not work
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData() {
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // A Cursor represents the result of a query and basically points to one row of the query result.
        // This way Android can buffer the query results efficiently; as it does not have to load all data into memory.
        // the "*" means select "all"
        Cursor res = db.rawQuery("select * from " + TABLE_NAME + " Order by ID DESC",null);
        return res;
    }

    public Cursor getData(String Id) {
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();
        // Get data based on recipe id
        Cursor res = db.rawQuery("select * from " + TABLE_NAME +" where id = ?",new String[] {Id});
        return res;
    }

    public Cursor getLastData() {
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();
        // get last created data. Using descending order and limiting to 1,we get the last recipe added
        Cursor res = db.rawQuery("select ID from " + TABLE_NAME +" Order by ID DESC Limit 1",null);
        return res;
    }

    public Cursor getId(String recipeName, String author) {
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();
        // get last created data. Using descending order and limiting to 1,we get the last recipe added
        Cursor res = db.rawQuery("select ID from " + TABLE_NAME +" where recipeName = ? and author = ?",new String[] {recipeName,author});
        return res;
    }

    public boolean updateData(String Id, byte[] recipeImage, String recipeName, String author, String dateCreated, String ingredientsList, String description){
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1,Id);
        contentValues.put(COL_2,recipeImage);
        contentValues.put(COL_3,recipeName);
        contentValues.put(COL_4,author);
        contentValues.put(COL_5,dateCreated);
        contentValues.put(COL_6,ingredientsList);
        contentValues.put(COL_7,description);
        db.update(TABLE_NAME, contentValues, "id = ?", new String[] {Id});
        return true;
    }

    public Integer deleteData(String Id) {
        // Open the database for reading and writing
        SQLiteDatabase db = this.getWritableDatabase();

        // delete based on recipe id
        return db.delete(TABLE_NAME, "ID = ?", new String[] {Id});
    }
}