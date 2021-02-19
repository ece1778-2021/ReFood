package com.refood.refood;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.refood.refood.R;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class ProfileImageAdapter extends
        RecyclerView.Adapter<ProfileImageAdapter.ProfileImageViewHolder> {

    private static final String LOG_TAG = ProfileImageAdapter.class.getSimpleName();

    private final LinkedList<String> mTimestamp;
    private LayoutInflater mInflater;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;

    private Context mContext;

    public ProfileImageAdapter(Context context, LinkedList<String> timestampList) {
        mInflater = LayoutInflater.from(context);
        mContext = context;

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();

        mTimestamp = timestampList;
    }


    @NonNull
    @Override
    public ProfileImageAdapter.ProfileImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.profile_image_item,
                parent, false);
        return new ProfileImageViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileImageAdapter.ProfileImageViewHolder holder, int position) {
        String timestamp = mTimestamp.get(position);
        String uid = mAuth.getCurrentUser().getUid();
        File storageDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), uid);
        if (!storageDir.exists())
        {
            storageDir.mkdirs();
        }
        File timestampImage = new File(storageDir, timestamp + ".jpg");
        if (!timestampImage.exists())
        {
            try {
                timestampImage.createNewFile();
            }
            catch(IOException ex){
                Log.e(LOG_TAG, "createNewFile throws exception in ProfileActivity OnCreate");
            }
            StorageReference timestampRef = mStorage.getReference().child(uid + "/" + timestamp + ".jpg");
            timestampRef.getFile(timestampImage).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap imageItem = BitmapFactory.decodeFile(timestampImage.getAbsolutePath());
                    holder.profileImageView.setImageBitmap(imageItem);
                }
            });
        }
        else
        {
            Bitmap imageItem = BitmapFactory.decodeFile(timestampImage.getAbsolutePath());
            holder.profileImageView.setImageBitmap(imageItem);
        }
    }

    @Override
    public int getItemCount() {
        return mTimestamp.size();
    }

    class ProfileImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final ImageView profileImageView;
        final ProfileImageAdapter mAdapter;

        public ProfileImageViewHolder(View itemView, ProfileImageAdapter adapter)  {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.imageItem);
            this.mAdapter = adapter;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            // Get the position of the item that was clicked.
            String timestamp = mTimestamp.get(getLayoutPosition());
            String uid = mAuth.getCurrentUser().getUid();
            File storageDir = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), uid);
            File timestampImage = new File(storageDir, timestamp + ".jpg");

            ImageView fullscreenView = itemView.getRootView().findViewById(R.id.fullscreen_view);
            Bitmap imageItem = BitmapFactory.decodeFile(timestampImage.getAbsolutePath());
            fullscreenView.setImageBitmap(imageItem);
            fullscreenView.setVisibility(View.VISIBLE);
            itemView.getRootView().findViewById(R.id.cameraFAB).setVisibility(View.GONE);
        }
    }

}
