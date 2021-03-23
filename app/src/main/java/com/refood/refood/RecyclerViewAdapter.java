package com.refood.refood;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<Zem> zemList;
    private long numCoins;
    private TextView textView;
    private DocumentReference documentReference;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseFirestore mDb = FirebaseFirestore.getInstance();

    private static final String LOG_TAG = RecyclerViewAdapter.class.getSimpleName();


    RecyclerViewAdapter(List<Zem> zemList, long numCoins, TextView textView, DocumentReference documentReference){
        this.zemList = zemList;
        this.numCoins = numCoins;
        this.textView = textView;
        this.documentReference = documentReference;
    }
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Zem zem = zemList.get(position);
        holder.price.setText("$"+zem.getPrice());
        holder.image.setBackgroundResource(zem.getImage());
        holder.button.setText(zem.getStatus());
        holder.invisText.setText(zem.getUrl());
        holder.invisText.setVisibility(View.INVISIBLE);
    }
    @Override
    public int getItemCount() {
        return zemList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView price;
        private TextView invisText;
        private ImageView image;
        private Button button;
        private CardView cardView;
        public MyViewHolder(View itemView) {
            super(itemView);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);
            button = itemView.findViewById(R.id.zemButton);
            cardView = itemView.findViewById(R.id.cardView);
            invisText = itemView.findViewById(R.id.invisText);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    String text = (String) button.getText();

                    CharSequence priceString = price.getText();
                    priceString = priceString.subSequence(1, priceString.length());
                    int priceInt = Integer.parseInt((String) priceString);

                    if (numCoins>=priceInt) {
                        numCoins = numCoins - priceInt;
                        textView.setText(textView.getContext().getString(R.string.coins_template, numCoins));
                        button.setVisibility(View.GONE);
                        cardView.setCardBackgroundColor(cardView.getContext().getResources().getColor(R.color.light_grey));

                        String zemUrl = (String) invisText.getText();
                        Map<String, Object> data = new HashMap<>();
                        documentReference.update("ownedZems", FieldValue.arrayUnion(zemUrl));

                        updateCoinsStorage(numCoins);
                    } else {
                        Toast.makeText(cardView.getContext(), "Not enough coins", Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }

    private void updateCoinsStorage(Long mNumCoins) {
        Map<String, Object> data = new HashMap<>();
        data.put("numCoins", mNumCoins);

        if (user != null) {
            String uid = user.getUid();
            mDb.collection("users").document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(LOG_TAG, "Coins successfully updated in FireStore!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(LOG_TAG, "Error writing document", e);
                        }
                    });
        }
    }
}