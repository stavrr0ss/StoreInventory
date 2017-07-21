package com.example.android.storeinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.storeinventory.data.ProductContract.ProductEntry;

/**
 * Created by Bogdan on 7/20/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " +
            ProductEntry.TABLE_NAME + "(" +
            ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            ProductEntry.COLUMN_PRODUCT + " TEXT NOT NULL," +
            ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0," +
            ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL DEFAULT 0," +
            ProductEntry.COLUMN_SUPLIER + " TEXT NOT NULL);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;

    public static final String DATABASE_NAME = "store.db";
    public static final int DATABASE_VERSION = 1 ;

    public ProductDbHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
