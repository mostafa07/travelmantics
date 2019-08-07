package com.example.travelmantics.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.travelmantics.R;
import com.example.travelmantics.models.TravelDeal;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TravelDealAdapter extends RecyclerView.Adapter<TravelDealAdapter.TravelDealViewHolder> {

    private List<TravelDeal> mTravelDeals;

    private TravelDealAdapterOnClickHandler mClickHandler;

    private static final String TRAVEL_DEALS_DB_REF = "travelDeals";

    public TravelDealAdapter(TravelDealAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
        mTravelDeals = new ArrayList<>();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mTravelDealsDbRef = mFirebaseDatabase.getReference().child(TRAVEL_DEALS_DB_REF);
        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal newTravelDeal = dataSnapshot.getValue(TravelDeal.class);
                newTravelDeal.setId(dataSnapshot.getKey());
                mTravelDeals.add(newTravelDeal);
                notifyItemInserted(mTravelDeals.size() - 1);
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
        mTravelDealsDbRef.addChildEventListener(mChildEventListener);
    }

    public interface TravelDealAdapterOnClickHandler {
        public void onClick(TravelDeal travelDeal);
    }

    @NonNull
    @Override
    public TravelDealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_travel_deal_item, parent, false);
        return new TravelDealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull TravelDealViewHolder holder, int position) {
        holder.bind(mTravelDeals.get(position));
    }

    @Override
    public int getItemCount() {
        return mTravelDeals.size();
    }


    public class TravelDealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mImageView;
        private TextView mTitleTextView;
        private TextView mPriceTextView;
        private TextView mDescriptionTextView;

        public TravelDealViewHolder(@NonNull View itemView) {
            super(itemView);

            mImageView = itemView.findViewById(R.id.travelDealImageView);
            mTitleTextView = itemView.findViewById(R.id.travelDealTitleTextView);
            mPriceTextView = itemView.findViewById(R.id.travelDealPriceTextView);
            mDescriptionTextView = itemView.findViewById(R.id.travelDealDescriptionTextView);

            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal travelDeal) {
            if (travelDeal.getImageUrl() != null && !travelDeal.getImageUrl().isEmpty()) {
                Picasso.get()
                        .load(travelDeal.getImageUrl())
                        .resize(100, 100)
                        .centerCrop()
                        .into(mImageView);
            }

            mTitleTextView.setText(travelDeal.getTitle());
            mPriceTextView.setText(travelDeal.getPrice());
            mDescriptionTextView.setText(travelDeal.getDescription());
        }

        @Override
        public void onClick(View view) {
            mClickHandler.onClick(mTravelDeals.get(getAdapterPosition()));
        }
    }
}
