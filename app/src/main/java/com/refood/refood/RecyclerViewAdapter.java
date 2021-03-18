package com.refood.refood;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Zem {
    private int price;
    private int image;
    private String status;
    public Zem(int price, int image) {
        this.price = price;
        this.image = image;
        this.status = "Buy";
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(String title) {
        this.price = price;
    }
    public int getImage() {
        return image;
    }
    public void setImage(int image) {
        this.image = image;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus(){
        return status;
    }
}

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private List<Zem> zemList;
    private long numCoins;
    private TextView textView;
    private DocumentReference documentReference;

    private static final String LOG_TAG = RecyclerViewAdapter.class.getSimpleName();


    RecyclerViewAdapter(List<Zem> zemList, long numCoins, TextView textView, DocumentReference documentReference){
        this.zemList = zemList;
        this.numCoins = numCoins;
        this.textView = textView;
        this.documentReference = documentReference;
    }
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view,parent,false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, final int position) {
        final Zem zem = zemList.get(position);
        holder.price.setText("$"+zem.getPrice());
        holder.image.setBackgroundResource(zem.getImage());
        holder.button.setText(zem.getStatus());
    }
    @Override
    public int getItemCount() {
        return zemList.size();
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView price;
        private ImageView image;
        private Button button;
        public MyViewHolder(View itemView) {
            super(itemView);
            price = itemView.findViewById(R.id.price);
            image = itemView.findViewById(R.id.image);
            button = itemView.findViewById(R.id.zemButton);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button button = (Button) v;
                    String text = (String) button.getText();

                    switch (text){
                        case "Buy":
                            CharSequence priceString = price.getText();
                            priceString = priceString.subSequence(1, priceString.length());
                            int priceInt = Integer.parseInt((String) priceString);
                            numCoins = numCoins - priceInt;
                            textView.setText(textView.getContext().getString(R.string.coins_template, numCoins));
                            button.setText("Use");
                            break;
                        case "Use":

//                            Map<String, Object> data = new HashMap<>();
//                            data.put("zem", image.getId());

                            Toast.makeText(textView.getContext(), "Use called", Toast.LENGTH_SHORT).show();
                            button.setText("In use");
//                            documentReference.set(data, SetOptions.merge())
//                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void aVoid) {
//                                        Log.d(LOG_TAG, "Zem successfully updated in FireStore!");
//                                    }
//                                })
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.w(LOG_TAG, "Error writing document", e);
//                                    }
//                                });
                            break;
                    }
                }
                });
        }
    }
}