package com.canvas.grocerybazaaradmin;

public class SpecialOfferModel {
    private String imageUrl;



    public SpecialOfferModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
