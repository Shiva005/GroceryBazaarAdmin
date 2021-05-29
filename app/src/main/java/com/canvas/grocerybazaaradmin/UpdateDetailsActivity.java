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
import android.widget.TextView;
import android.widget.Toast;

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

import com.bumptech.glide.Glide;

import java.io.IOException;


public class UpdateDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextName;
    private EditText editTextBrand;
    private EditText editTextDesc;
    private EditText editTextPrice;
    private EditText editTextQty;

    private TextView imageChoosenName;
    private ImageView imageView;
    private Uri imagePath;
    private ProgressDialog progressDialog;
    private static final int PIKE_IMAGE_REQUEST = 1;

    String name, brand, desc, price, qty;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_details);

        editTextName = findViewById(R.id.edittext_name);
        editTextBrand = findViewById(R.id.edittext_brand);
        editTextDesc = findViewById(R.id.edittext_desc);
        editTextPrice = findViewById(R.id.edittext_price);
        editTextQty = findViewById(R.id.edittext_qty);
        imageChoosenName = findViewById(R.id.image_choose_nameId);
        imageView = findViewById(R.id.category_imageView);

        storageReference = FirebaseStorage.getInstance().getReference("All Images");
        firebaseFirestore = FirebaseFirestore.getInstance();

        findViewById(R.id.button_save).setOnClickListener(this);
        findViewById(R.id.textview_view_products).setOnClickListener(this);

        progressDialog = new ProgressDialog(UpdateDetailsActivity.this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_save:
                saveProduct();
                break;
            case R.id.textview_view_products:
                startActivity(new Intent(this, CategoryActivity.class));
                break;
        }
    }

    private boolean hasValidationErrors(String name, String brand, String desc, String price, String qty) {
        if (name.isEmpty()) {
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return true;
        }

        if (brand.isEmpty()) {
            editTextBrand.setError("Brand required");
            editTextBrand.requestFocus();
            return true;
        }

        if (desc.isEmpty()) {
            editTextDesc.setError("Description required");
            editTextDesc.requestFocus();
            return true;
        }

        if (price.isEmpty()) {
            editTextPrice.setError("Price required");
            editTextPrice.requestFocus();
            return true;
        }

        if (qty.isEmpty()) {
            editTextQty.setError("Quantity required");
            editTextQty.requestFocus();
            return true;
        }
        return false;
    }


    // Code for storing data in firebase firestore and storage
    public void saveProduct() {

        name = editTextName.getText().toString().trim();
        brand = editTextBrand.getText().toString().trim();
        desc = editTextDesc.getText().toString().trim();
        price = editTextPrice.getText().toString().trim();
        qty = editTextQty.getText().toString().trim();

        if (!hasValidationErrors(name, brand, desc, price, qty)) {

            progressDialog.setTitle("Uploading Please wait..");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);

            if (imagePath != null) {
                progressDialog.show();

                final StorageReference imageReference = storageReference.child("Category Icons").child(name);
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
                            Product product = new Product(
                                    name,
                                    brand,
                                    desc,
                                    Double.parseDouble(price),
                                    Integer.parseInt(qty),
                                    imageUrl
                            );
                            firebaseFirestore.collection("Category").document("Beverages").set(product)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(UpdateDetailsActivity.this, "Upload Successfull", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UpdateDetailsActivity.this, "ERROR while Uploading !!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            progressDialog.dismiss();

                        } else {
                            Toast.makeText(UpdateDetailsActivity.this, "" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(UpdateDetailsActivity.this, "Failed to Upload with ERROR : " + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            } else {
                Toast.makeText(UpdateDetailsActivity.this, "No file Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }


    //OnClick of Choose Image Button
    public void ChooseImage(View view) {

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
                        imageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Glide.with(this)
                            .load(imagePath)
                            .into(imageView);
                    imageView.setImageURI(imagePath);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
