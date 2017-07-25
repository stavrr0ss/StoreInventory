package com.example.android.storeinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.storeinventory.R.id.quantity;
import static com.example.android.storeinventory.data.ProductContract.ProductEntry;

/**
 * Created by Bogdan on 7/20/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final Uri mUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI,
                cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry._ID)));

        TextView productName = (TextView) view.findViewById(R.id.product_name);
        TextView productQuantity = (TextView) view.findViewById(quantity);
        TextView productPrice = (TextView) view.findViewById(R.id.price);

        String product = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT));
        final int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE));

        productName.setText(product);
        productQuantity.setText(String.valueOf(quantity));
        productPrice.setText(price);

        ImageView sellProduct = (ImageView) view.findViewById(R.id.sale_icon);
        sellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    int remainingQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_QUANTITY, remainingQuantity);
                    // Update the database
                    context.getContentResolver().update(mUri, values, null, null);
                } else {
                    Toast.makeText(context, R.string.quantity_not_available_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
