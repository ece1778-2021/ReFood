package com.refood.refood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.refood.refood.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private static final String LOG_TAG = RegisterActivity.class.getSimpleName();
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int DOWNSCALE_SIZE = 1024;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseFirestore mDb;

    private ImageView mAvatarView;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mUsernameEditText;
    private EditText mBioEditText;

    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDb = FirebaseFirestore.getInstance();

        mAvatarView = findViewById(R.id.register_avatar);
        mEmailEditText = findViewById(R.id.editTextRegisterEmailAddress);
        mPasswordEditText = findViewById(R.id.editTextRegisterPassword);
        mConfirmPasswordEditText = findViewById(R.id.editTextRegisterConfirmPassword);
        mUsernameEditText = findViewById(R.id.editTextRegisterUsername);
        mBioEditText = findViewById(R.id.editTextRegisterShortBio);

        if (savedInstanceState != null)
        {
            mCurrentPhotoPath = savedInstanceState.getString("currentPhotoPath");
            Bitmap avatar = BitmapFactory.decodeFile(mCurrentPhotoPath);
            mAvatarView.setImageBitmap(avatar);
        }
        else
        {
            mCurrentPhotoPath = null;
        }

    }

    public void DoRegister(View view) {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        String confirmPassword = mConfirmPasswordEditText.getText().toString();
        String username = mUsernameEditText.getText().toString();
        String bio = mBioEditText.getText().toString();
        if (!password.equals(confirmPassword))
        {
            Toast.makeText(RegisterActivity.this, "Confirm password mismatches",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        else if (com.refood.refood.MainActivity.isEmptyString(email) || com.refood.refood.MainActivity.isEmptyString(password)
                || com.refood.refood.MainActivity.isEmptyString(username) || com.refood.refood.MainActivity.isEmptyString(bio)
                || com.refood.refood.MainActivity.isEmptyString(mCurrentPhotoPath))
        {
            Toast.makeText(RegisterActivity.this, "Fields and/or the picture cannot be empty",
                    Toast.LENGTH_SHORT).show();
            updateUI(null);
        }
        else
        {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(LOG_TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                // Create a new user with a first and last name
                                Map<String, Object> userStore = new HashMap<>();
                                userStore.put("username", username);
                                userStore.put("bio", bio);
                                //userStore.put("displayPicPath", mCurrentPhotoPath);
                                String uid = user.getUid();
                                // Add a new document with a generated ID
                                mDb.collection("users").document(uid)
                                        .set(userStore)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(LOG_TAG, "DocumentSnapshot successfully written!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(LOG_TAG, "Error writing document", e);
                                            }
                                        });
                                // Upload image to storage
                                Bitmap image = downscaleBitmapFromFile(mCurrentPhotoPath, DOWNSCALE_SIZE);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                StorageReference storageRef = mStorage.getReference();
                                StorageReference avatarRef = storageRef.child(uid+"/displayPic.jpg");

                                UploadTask uploadTask = avatarRef.putBytes(data);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        Log.w(LOG_TAG, "Display Picture failed the upload!");
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                        // ...
                                        Log.d(LOG_TAG, "Display Picture successfully Uploaded!");
                                        updateUI(user);
                                    }
                                });
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(LOG_TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this,
                                        "Sign up failed: password is not strong enough or email already exists",
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }

                            // ...
                        }
                    });


        }
    }

    private void updateUI(FirebaseUser user) {
        if (user!=null){
            Intent intent = new Intent(this, com.refood.refood.ProfileActivity.class);
            startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*if (data != null)
            {
                Log.d(LOG_TAG, "received non-null data from the camera");
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                mAvatarView.setImageBitmap(imageBitmap);
            }
            else
            {*/
                Log.w(LOG_TAG, "received null data from the camera, try to use current photo path");

                Bitmap avatar = BitmapFactory.decodeFile(mCurrentPhotoPath);
                mAvatarView.setImageBitmap(avatar);
            //}
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "displayPic.jpg";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        //File image = File.createTempFile(
        //        imageFileName,  /* prefix */
        //        ".jpg",         /* suffix */
        //        storageDir      /* directory */
        //);
        File image = new File(storageDir, imageFileName);
        image.createNewFile();

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentPhotoPath!=null)
        {
            outState.putString("currentPhotoPath", mCurrentPhotoPath);
        }
    }

    public static int calculateInSampleSize( BitmapFactory.Options options, int downscaleSize) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > downscaleSize || width > downscaleSize) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= downscaleSize
                    && (halfWidth / inSampleSize) >= downscaleSize) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap downscaleBitmapFromFile(String photoPath, int downscaleSize) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, downscaleSize);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(photoPath, options);
    }



}