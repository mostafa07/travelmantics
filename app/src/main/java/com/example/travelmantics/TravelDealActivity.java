package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.travelmantics.models.TravelDeal;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class TravelDealActivity extends AppCompatActivity {

    private DatabaseReference mTravelDealsDbRef;
    private StorageReference mTravelDealsPicturesStorageRef;

    private boolean mIsAdmin;
    private TravelDeal mTravelDeal;

    private static final String INTENT_EXTRA_IS_ADMIN = "isAdmin";
    private static final String INTENT_EXTRA_TRAVEL_DEAL_OBJECT = "travelDealObject";
    private static final int PICTURE_RESULT = 4242;
    private static final String TRAVEL_DEALS_DB_REF = "travelDeals";
    private static final String TRAVEL_DEALS_PICTURES_STORAGE_DB_REF = "travelDealsPictures";

    private ImageView mImageView;
    private EditText mTitleEditText;
    private EditText mPriceEditText;
    private EditText mDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_deal);

        mTitleEditText = findViewById(R.id.titleEditText);
        mPriceEditText = findViewById(R.id.priceEditText);
        mDescriptionEditText = findViewById(R.id.descriptionEditText);
        mImageView = findViewById(R.id.imageView);
        Button mUploadButton = findViewById(R.id.uploadButton);
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent,
                        view.getContext().getString(R.string.choosePicture)), PICTURE_RESULT);
            }
        });

        Intent intent = getIntent();
        mIsAdmin = intent.getBooleanExtra(INTENT_EXTRA_IS_ADMIN, false);
        if (mIsAdmin) {
            mUploadButton.setVisibility(View.VISIBLE);
            mTitleEditText.setEnabled(true);
            mPriceEditText.setEnabled(true);
            mDescriptionEditText.setEnabled(true);

            invalidateOptionsMenu();
        } else {
            mUploadButton.setVisibility(View.GONE);
            mTitleEditText.setEnabled(false);
            mPriceEditText.setEnabled(false);
            mDescriptionEditText.setEnabled(false);

            invalidateOptionsMenu();
        }

        mTravelDeal = (TravelDeal) intent.getSerializableExtra(INTENT_EXTRA_TRAVEL_DEAL_OBJECT);
        if (mTravelDeal != null) {
            if (mTravelDeal.getImageUrl() != null && !mTravelDeal.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(mTravelDeal.getImageUrl())
                        .resize(300, 300)
                        .centerCrop()
                        .into(mImageView);
            }
            mTitleEditText.setText(mTravelDeal.getTitle());
            mPriceEditText.setText(mTravelDeal.getPrice());
            mDescriptionEditText.setText(mTravelDeal.getDescription());
        } else {
            mTravelDeal = new TravelDeal();
        }

        mTravelDealsDbRef = FirebaseDatabase.getInstance().getReference().child(TRAVEL_DEALS_DB_REF);
        mTravelDealsPicturesStorageRef = FirebaseStorage.getInstance().getReference()
                .child(TRAVEL_DEALS_PICTURES_STORAGE_DB_REF);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.travel_deal_menu, menu);
        if (mIsAdmin) {
            menu.findItem((R.id.travel_deal_menu_save_item)).setVisible(true);
            menu.findItem((R.id.travel_deal_menu_delete_item)).setVisible(true);
        } else {
            menu.findItem((R.id.travel_deal_menu_save_item)).setVisible(false);
            menu.findItem((R.id.travel_deal_menu_delete_item)).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.travel_deal_menu_save_item:
                saveTravelDeal();
                return true;
            case R.id.travel_deal_menu_delete_item:
                deleteTravelDeal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final StorageReference ref = mTravelDealsPicturesStorageRef.child(imageUri.getLastPathSegment());
            ref.putFile(imageUri)
                    .addOnSuccessListener(this,
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    mTravelDeal.setImageName(taskSnapshot.getStorage().getPath());

                                    ref.getDownloadUrl().addOnSuccessListener(TravelDealActivity.this,
                                            new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    mTravelDeal.setImageUrl(uri.toString());

                                                    Picasso.get()
                                                            .load(mTravelDeal.getImageUrl())
                                                            .resize(300, 300)
                                                            .centerCrop()
                                                            .into(mImageView);

                                                    Toast.makeText(TravelDealActivity.this,
                                                            getString(R.string.imageUploaded), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            });
        }
    }

    private void saveTravelDeal() {
        if (mTravelDeal == null) {
            mTravelDeal = new TravelDeal();
        }
        mTravelDeal.setTitle(mTitleEditText.getText().toString());
        mTravelDeal.setPrice(mPriceEditText.getText().toString());
        mTravelDeal.setDescription(mDescriptionEditText.getText().toString());

        if (mTravelDeal.getId() == null || mTravelDeal.getId().isEmpty()) {
            mTravelDealsDbRef.push().setValue(mTravelDeal);
            Toast.makeText(this, getString(R.string.travelDealSaved), Toast.LENGTH_LONG).show();
        } else {
            mTravelDealsDbRef.child(mTravelDeal.getId()).setValue(mTravelDeal);
            Toast.makeText(this, getString(R.string.travelDealUpdated), Toast.LENGTH_LONG).show();
        }

        finish();
    }

    private void deleteTravelDeal() {
        if (mTravelDeal.getId() == null || mTravelDeal.getId().isEmpty()) {
            Toast.makeText(this, getString(R.string.errorDeletingTravelDeal), Toast.LENGTH_LONG).show();
        } else {
            mTravelDealsDbRef.child(mTravelDeal.getId()).removeValue();

            if (mTravelDeal.getImageName() != null && !mTravelDeal.getImageName().isEmpty()) {
                FirebaseStorage.getInstance().getReference().child(mTravelDeal.getImageName()).delete();
            }
            Toast.makeText(this, getString(R.string.travelDealDeleted), Toast.LENGTH_LONG).show();

            finish();
        }
    }
}
