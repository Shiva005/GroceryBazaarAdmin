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
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SpecialOfferActivity extends AppCompatActivity {

    private static final int PIKE_IMAGE_REQUEST = 1;
    private Uri imagePath;
    private ImageView showImage;
    private ProgressDialog progressDialog;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_offer);
        showImage = findViewById(R.id.show_imageView);
        progressDialog = new ProgressDialog(this);

        storageReference = FirebaseStorage.getInstance().getReference("All Images");
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public void categoryChooseImage(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PIKE_IMAGE_REQUEST);
    }

    public void uploadCategoryButton(View view) {
        UploadCategoryFile();
    }

    private void UploadCategoryFile() {

        if (imagePath != null) {
            progressDialog.setTitle("Uploading Please wait..");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

            final StorageReference imageReference = storageReference.child("Special Offer").child("Special Offer Image");
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
                        Map<String, Object> data =new HashMap<>();
                        data.put("imageUrl",imageUrl);
                        data.put("view_type",1);
/*                        SpecialOfferModel specialOfferModel = new SpecialOfferModel(
                                imageUrl,
                                view_type
                        );*/
                        firebaseFirestore.collection("Category")
                                .document("Home")
                                .collection("Top Deals ImageSlider")
                                .document("Special Offer Image")
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SpecialOfferActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SpecialOfferActivity.this, "ERROR while Uploading !!", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    } else {
                        Toast.makeText(SpecialOfferActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SpecialOfferActivity.this, "Failed to Upload with ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(SpecialOfferActivity.this, "Please select from the DropDown list", Toast.LENGTH_SHORT).show();
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
                        showImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this)
                            .load(imagePath)
                            .into(showImage);
                    showImage.setImageURI(imagePath);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}