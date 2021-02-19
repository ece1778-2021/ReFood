package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.google.firebase.storage.UploadTask;
import com.refood.refood.R;
import com.refood.refood.RegisterActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private final LinkedList<String> mTimestampList = new LinkedList<>();

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private RecyclerView mRecyclerView;
    private com.refood.refood.ProfileImageAdapter mAdapter;

    private String mCurrentPhotoPath;
    private String mCurrentTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(uid);
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userStore = document.getData();
                            Log.d(LOG_TAG, "DocumentSnapshot data: " + userStore);

                            ((TextView) findViewById(R.id.textProfileUsername)).setText((String)userStore.get("username"));
                            ((TextView) findViewById(R.id.textProfileBio)).setText((String)userStore.get("bio"));

                            File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), uid);
                            if (!storageDir.exists())
                            {
                                storageDir.mkdirs();
                            }
                            File displayPicFile = new File(storageDir, "displayPic.jpg");
                            if (!displayPicFile.exists())
                            {
                                try {
                                    displayPicFile.createNewFile();
                                }
                                catch(IOException ex){
                                    Log.e(LOG_TAG, "createNewFile throws exception in ProfileActivity OnCreate");
                                }
                                StorageReference avatarRef = mStorage.getReference().child(uid+"/displayPic.jpg");
                                avatarRef.getFile(displayPicFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        // Local temp file has been created
                                        Log.d(LOG_TAG, "Display Picture successfully downloaded!");
                                        Bitmap avatar = BitmapFactory.decodeFile(displayPicFile.getAbsolutePath());
                                        ((ImageView) findViewById(R.id.profile_avatar)).setImageBitmap(avatar);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                        Log.w(LOG_TAG, "Display Picture failed the download!");
                                    }
                                });
                            }
                            else
                            {
                                Bitmap avatar = BitmapFactory.decodeFile(displayPicFile.getAbsolutePath());
                                ((ImageView) findViewById(R.id.profile_avatar)).setImageBitmap(avatar);
                            }

                        } else {
                            Log.d(LOG_TAG, "No such document");
                        }
                    } else {
                        Log.d(LOG_TAG, "get failed with ", task.getException());
                    }
                }
            });

            // Get a handle to the RecyclerView.
            mRecyclerView = findViewById(R.id.imageGrid);
            // Create an adapter and supply the data to be displayed.
            mAdapter = new com.refood.refood.ProfileImageAdapter(this, mTimestampList);
            // Connect the adapter with the RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
            // Give the RecyclerView a default layout manager.
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

            mDb.collection("photos").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getData().get("uid").toString().equals(uid)) {
                                mTimestampList.addFirst(document.getData().get("timestamp").toString());
                            }
                        }
                        Collections.sort(mTimestampList, Collections.reverseOrder());
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }
                    else {
                        Log.d(LOG_TAG, "get failed with ", task.getException());
                    }
                }
            });

            FloatingActionButton fab = findViewById(R.id.cameraFAB);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dispatchTakePictureIntent(view);
                }
            });

        }
    }

    public void dispatchTakePictureIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.refood.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        String uid = mAuth.getCurrentUser().getUid();
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), uid);

        File image = new File(storageDir, imageFileName);
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        mCurrentTimestamp = timeStamp;
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Record its timestamp to firestore and upload the image to storage
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user.getUid();
            Map<String, Object> userStore = new HashMap<>();
            userStore.put("uid", uid);
            userStore.put("timestamp", mCurrentTimestamp);
            mDb.collection("photos").add(userStore);

            Bitmap image = RegisterActivity.downscaleBitmapFromFile(mCurrentPhotoPath, RegisterActivity.DOWNSCALE_SIZE);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] uploadData = baos.toByteArray();

            StorageReference storageRef = mStorage.getReference();
            StorageReference avatarRef = storageRef.child(uid + "/" + mCurrentTimestamp + ".jpg");

            UploadTask uploadTask = avatarRef.putBytes(uploadData);

            mTimestampList.addFirst(mCurrentTimestamp);
            // Notify the adapter, that the data has changed.
            mRecyclerView.getAdapter().notifyItemInserted(0);
            // Scroll to the top.
            mRecyclerView.smoothScrollToPosition(0);
        }
    }

    public void exitFullscreen(View view) {
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.cameraFAB).setVisibility(View.VISIBLE);
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.menu is a reference to an xml file named menu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logout_action) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}