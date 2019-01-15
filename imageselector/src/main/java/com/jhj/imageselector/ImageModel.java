package com.jhj.imageselector;

import java.io.Serializable;

/**
 * Created by jhj on 19-1-15.
 */

public class ImageModel implements Serializable {

    private String imagePath;

    public ImageModel(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
