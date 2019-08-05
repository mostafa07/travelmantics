package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.travelmantics.models.TravelDeal;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InsertActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTravelDealsDbRef;
    private static final String TRAVEL_DEALS_DB_REF = "travelDeals";

    private EditText mTitleEditText;
    private EditText mPriceEditText;
    private EditText mDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mTravelDealsDbRef = mFirebaseDatabase.getReference().child(TRAVEL_DEALS_DB_REF);

        mTitleEditText = findViewById(R.id.titleEditText);
        mPriceEditText = findViewById(R.id.priceEditText);
        mDescriptionEditText = findViewById(R.id.descriptionEditText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saveMenuItem :
                saveTravelDeal();
                Toast.makeText(this, getString(R.string.travelDealSaved), Toast.LENGTH_LONG).show();
                resetEditTexts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void saveTravelDeal() {
        TravelDeal travelDeal = new TravelDeal(mTitleEditText.getText().toString(),
                mPriceEditText.getText().toString(),
                mDescriptionEditText.getText().toString(),
                "");
        mTravelDealsDbRef.push().setValue(travelDeal);
    }

    private void resetEditTexts() {
        mTitleEditText.setText("");
        mPriceEditText.setText("");
        mDescriptionEditText.setText("");
        mTitleEditText.requestFocus();
    }
}
