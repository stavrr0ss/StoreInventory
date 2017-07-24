package com.example.android.storeinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.storeinventory.R.id.quantity;
import static com.example.android.storeinventory.data.ProductContract.ProductEntry;
import static com.example.android.storeinventory.data.ProductProvider.LOG_TAG;

/**
 * Created by Bogdan on 7/20/2017.
 */

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int EXISTING_PRODUCT_LOADER = 0;

    /** Content URI for the existing productt (null if it's a new product) */
    private Uri mUri;
    private Uri imageUri;

    /** EditText field to enter the product name */
    private EditText mNameEditText;

    /** EditText field to enter the quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the suplier */
    private EditText mSuplierEditText;

    private EditText mPriceEditText ;

    private Button sendOrder ;

    private boolean mProductHasChanged = false ;

    private int mQuantity ;

    private ImageView mImageView ;

    private Button mSetImage ;

    private Button mDecrementButton ;

    private Button mIncrementButton ;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Intent intent = getIntent();
        mUri = intent.getData();

        if (mUri == null) {
            // This is a new pet, so change the app bar to say "Add a Pet"
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mNameEditText = (EditText)findViewById(R.id.edit_product_name);
        mQuantityEditText = (EditText)findViewById(R.id.edit_product_quantity);
        mSuplierEditText = (EditText)findViewById(R.id.edit_suplier);
        mPriceEditText = (EditText)findViewById(R.id.edit_product_price);
        mDecrementButton = (Button)findViewById(R.id.decrement_button);
        mIncrementButton = (Button)findViewById(R.id.increment_button);
        mSetImage = (Button)findViewById(R.id.choose_picture);
        mImageView = (ImageView)findViewById(R.id.product_picture);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSuplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mQuantity++;
                mQuantityEditText.setText(String.valueOf(mQuantity));
            }
        });
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mQuantity > 0){
                    mQuantity--;
                    mQuantityEditText.setText(String.valueOf(mQuantity));
                }else {
                    Toast.makeText(EditActivity.this, R.string.invalid_decrement_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });
        mSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }
    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                imageUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + imageUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(imageUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String suplierString = mSuplierEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(suplierString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(priceString) && imageUri == null) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }
        if (TextUtils.isEmpty(nameString)){
            Toast.makeText(EditActivity.this, "Product requires a name !", Toast.LENGTH_LONG).show();
            return;
        }
        else if (TextUtils.isEmpty(suplierString)){
            Toast.makeText(EditActivity.this,"No suplier mentioned !",Toast.LENGTH_LONG).show();
            return;
        }
        else if (mQuantity <= 0 ){
            Toast.makeText(EditActivity.this, "Quantity required !", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(priceString)){
            Toast.makeText(EditActivity.this, "Price needed for product !", Toast.LENGTH_SHORT).show();

        }else{
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT, nameString);
            values.put(ProductEntry.COLUMN_SUPLIER, suplierString);
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }

            values.put(ProductEntry.COLUMN_QUANTITY,quantity);
            int price = 0;
            if (!TextUtils.isEmpty(priceString)) {
                price = Integer.parseInt(priceString);
            }
            values.put(ProductEntry.COLUMN_PRICE,price);
            if (imageUri == null) {
                imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                        "://" + getResources().getResourcePackageName(R.drawable.empty_cart) +
                        '/' + getResources().getResourceTypeName(R.drawable.empty_cart) +
                        '/' + getResources().getResourceEntryName(R.drawable.empty_cart));
            }
            values.put(ProductEntry.COLUMN_IMAGE,imageUri.toString());


            if (mUri == null) {
                // This is a NEW pet, so insert a new pet into the provider,
                // returning the content URI for the new pet.
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_productt_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }else {
                // Otherwise this is an EXISTING pet, so update the pet with content URI: mCurrentPetUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentPetUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT,
                ProductEntry.COLUMN_SUPLIER,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT);
            int suplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPLIER);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String suplier = cursor.getString(suplierColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            mNameEditText.setText(name);
            mSuplierEditText.setText(suplier);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            mImageView.setImageURI(Uri.parse(image));
        }
        sendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEmailToSuplier();
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mSuplierEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        imageUri = null;
    }
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void deleteProduct() {
        // Only perform the delete if this is an existing pet.
        if (mUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
    public void sendEmailToSuplier () {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_TEXT, new StringBuilder().append(getString(R.string.email_text)).append(mNameEditText).toString());
        intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.email_subject) );
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
