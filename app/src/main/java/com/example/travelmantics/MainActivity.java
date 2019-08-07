package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.travelmantics.adapters.TravelDealAdapter;
import com.example.travelmantics.models.TravelDeal;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TravelDealAdapter.TravelDealAdapterOnClickHandler {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private RecyclerView mTravelDealsRecyclerView;
    private TravelDealAdapter mTravelDealAdapter;

    private boolean mIsAdmin;

    private static final String INTENT_EXTRA_IS_ADMIN = "isAdmin";
    private static final String INTENT_EXTRA_TRAVEL_DEAL_OBJECT = "travelDealObject";
    private static final String ADMINISTRATORS_DB_REF = "administrators";
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mIsAdmin = false;

        mTravelDealsRecyclerView = findViewById(R.id.travelDealsRecyclerView);
        mTravelDealAdapter = new TravelDealAdapter(this);
        mTravelDealsRecyclerView.setAdapter(mTravelDealAdapter);
        mTravelDealsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mFirebaseAuth.getCurrentUser() == null) {
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                } else {
                    String uid = mFirebaseAuth.getUid();
                    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference administratorsDbRef = firebaseDatabase.getReference()
                            .child(ADMINISTRATORS_DB_REF)
                            .child(uid);

                    ChildEventListener childEventListener = new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            mIsAdmin = true;
                            invalidateOptionsMenu();
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    };
                    administratorsDbRef.addChildEventListener(childEventListener);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTravelDealAdapter = new TravelDealAdapter(this);
        mTravelDealsRecyclerView.setAdapter(mTravelDealAdapter);

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if (mIsAdmin) {
            menu.findItem(R.id.main_menu_add_item).setVisible(true);
        } else {
            menu.findItem(R.id.main_menu_add_item).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_add_item:
                Intent intent = new Intent(MainActivity.this, TravelDealActivity.class);
                intent.putExtra(INTENT_EXTRA_IS_ADMIN, mIsAdmin);
                startActivity(intent);
                return true;
            case R.id.main_menu_logout_item:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
                            }
                        });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(TravelDeal travelDeal) {
        Intent intent = new Intent(this, TravelDealActivity.class);
        intent.putExtra(INTENT_EXTRA_IS_ADMIN, mIsAdmin);
        intent.putExtra(INTENT_EXTRA_TRAVEL_DEAL_OBJECT, travelDeal);
        startActivity(intent);
    }
}
