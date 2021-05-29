package com.canvas.grocerybazaaradmin;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private int imageIndex;
    private String productName, imageUrl;

    public CategoryModel(int imageIndex, String productName, String imageUrl) {
        this.imageIndex = imageIndex;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
