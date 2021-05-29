package com.canvas.grocerybazaaradmin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void UpdateAddCategory(View view) {
        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
    }

    /*public void UpdateDetails(View view) {
        startActivity(new Intent(MainActivity.this,UpdateDetailsActivity.class));
    }*/

    public void SpecialOffer(View view) {
        startActivity(new Intent(MainActivity.this,SpecialOfferActivity.class));
    }

    public void TopDealsImageSlider(View view) {
        startActivity(new Intent(MainActivity.this,TopDealsImageSliderActivity.class));
    }
}
