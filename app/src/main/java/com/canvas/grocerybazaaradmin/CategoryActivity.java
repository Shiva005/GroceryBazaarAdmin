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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class CategoryActivity extends AppCompatActivity {

    private ImageView categoryImage;
    private Spinner selectCategory;

    private Uri imagePath;
    private static final int PIKE_IMAGE_REQUEST = 1;
    String positionText="Choose from the Options";
    ProgressDialog progressDialog;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    private int imageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_item);

        selectCategory = findViewById(R.id.chooseCategory_spinner);
        categoryImage = findViewById(R.id.category_imageView);
        progressDialog = new ProgressDialog(CategoryActivity.this);

        storageReference = FirebaseStorage.getInstance().getReference("All Images");
        firebaseFirestore = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.CategoryName, android.R.layout.simple_list_item_1);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectCategory.setAdapter(adapter);
        selectCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                positionText = parent.getItemAtPosition(position).toString();
                imageIndex=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void uploadCategoryButton(View view) {
        UploadCategoryFile();
    }

    private void UploadCategoryFile() {

        if (imagePath != null) {

            if(!positionText.equals("Choose from the Options")) {
                progressDialog.setTitle("Uploading Please wait..");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

                final StorageReference imageReference = storageReference.child("Category Icons").child(positionText);
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
                            CategoryModel categoryModel = new CategoryModel(
                                    imageIndex,
                                    positionText,
                                    imageUrl
                            );
                            firebaseFirestore.collection("Category").document(positionText).set(categoryModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(CategoryActivity.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CategoryActivity.this, "ERROR while Uploading !!", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        } else {
                            Toast.makeText(CategoryActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(CategoryActivity.this, "Failed to Upload with ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                Toast.makeText(CategoryActivity.this, "Please select from the DropDown list", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(CategoryActivity.this, "Please Choose the Image first.", Toast.LENGTH_SHORT).show();
        }
    }

    public void categoryChooseImage(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), PIKE_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (requestCode == PIKE_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                    imagePath = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                        categoryImage.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this)
                            .load(imagePath)
                            .into(categoryImage);
                    categoryImage.setImageURI(imagePath);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
