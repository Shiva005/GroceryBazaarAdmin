package com.canvas.grocerybazaaradmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TopDealsImageSliderActivity extends AppCompatActivity {

    private EditText inputImageName;
    private ImageView topDealsImages;
    private Uri imagePath;
    private static final int PIKE_IMAGE_REQUEST = 1;
    ProgressDialog progressDialog;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_deals_image_slider);

        inputImageName = findViewById(R.id.inputImageName);
        topDealsImages = findViewById(R.id.category_imageView);

        progressDialog = new ProgressDialog(this);

        storageReference = FirebaseStorage.getInstance().getReference("All Images");
        firebaseFirestore = FirebaseFirestore.getInstance();


    }

    public void uploadTopDeals(View view) {
        UploadCategoryFile();
    }

    public void ImageSliderChooseImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PIKE_IMAGE_REQUEST);
    }

    private void UploadCategoryFile() {

        final String inputText = inputImageName.getText().toString();
        if (imagePath != null && !inputText.equals("")) {
            progressDialog.setTitle("Uploading Please wait..");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

            final StorageReference imageReference = storageReference.child("Top Deals ImageSlider");
            final UploadTask uploadTask = (UploadTask) imageReference.putFile(imagePath)
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setProgress((int) progress);
                        }
                    });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        task.getException();
                    }
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String imageUrl = task.getResult().toString();
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("productName", inputText);
                        data.put("imageUrl", imageUrl);
                        data.put("view_type", 0);
                        /*CategoryModel categoryModel = new CategoryModel(
                                index,
                                inputText,
                                imageUrl
                        );*/
                        firebaseFirestore.collection("Category")
                                .document("Home")
                                .collection("Top Deals ImageSlider")
                                .document("TopDeals")
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(TopDealsImageSliderActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        index++;
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(TopDealsImageSliderActivity.this, "ERROR while Uploading !!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(TopDealsImageSliderActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(TopDealsImageSliderActivity.this, "Failed to Upload with ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(TopDealsImageSliderActivity.this, "Please enter Image Name", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (requestCode == PIKE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                    imagePath = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                        topDealsImages.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this)
                            .load(imagePath)
                            .into(topDealsImages);
                    topDealsImages.setImageURI(imagePath);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
