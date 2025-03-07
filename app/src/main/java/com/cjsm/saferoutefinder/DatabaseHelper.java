package com.cjsm.saferoutefinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SafeRouteDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_RATINGS = "street_ratings";
    private static final String COLUMN_STREET = "street_name";
    private static final String COLUMN_RATING = "safety_rating";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_RATINGS + " (" +
                COLUMN_STREET + " TEXT PRIMARY KEY, " +
                COLUMN_RATING + " REAL DEFAULT 3.0);";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RATINGS);
        onCreate(db);
    }

    // Add or update a street's rating
    public void updateRating(String street, double rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STREET, street);
        values.put(COLUMN_RATING, rating);

        db.insertWithOnConflict(TABLE_RATINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    // Retrieve all ratings as a map
    public Map<String, Double> getAllRatings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Double> ratings = new HashMap<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RATINGS, null);
        if (cursor.moveToFirst()) {
            do {
                ratings.put(cursor.getString(0), cursor.getDouble(1));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ratings;
    }
}