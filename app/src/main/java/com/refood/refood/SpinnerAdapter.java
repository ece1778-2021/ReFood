package com.refood.refood;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<String> {
    StorageReference storageReference;

    public SpinnerAdapter(Context context, ArrayList<String> countryList, StorageReference storageReference) {
        super(context, 0, countryList);
        this.storageReference = storageReference;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.zem_spinner_row, parent, false
            );
        }
        ImageView imageViewFlag = convertView.findViewById(R.id.spinner_image);
        String zemPath = getItem(position);
        if (zemPath != null) {
            StorageReference childRef = storageReference.child(zemPath);
            childRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Log.d("Spinner adapter", "Successfully loaded uri");
                    Glide.with(imageViewFlag.getContext() /* context */)
                            .load(uri)
                            .into(imageViewFlag);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.d("Spinner adapter", "Failed to load uri");
                }
            });
        }
        return convertView;
    }
}
