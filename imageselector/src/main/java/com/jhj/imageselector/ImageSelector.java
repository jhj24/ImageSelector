package com.jhj.imageselector;

import android.content.Context;
import android.content.Intent;

import com.jhj.imageselector.ui.ImageViewPagerActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImageSelector {

    private static volatile ImageSelector singleton;

    private WeakReference<Context> weakReference;

    private ImageSelector(Context context) {
        weakReference = new WeakReference<Context>(context);
    }

    public static ImageSelector getInstance(Context context) {
        if (singleton == null) {
            synchronized (ImageSelector.class) {
                if (singleton == null) {
                    singleton = new ImageSelector(context);
                }
            }
        }
        return singleton;
    }

    public void imagePreview(List<ImageModel> imageList) {
        imagePreview(imageList, 0);
    }

    public void imagePreview(List<ImageModel> imageList, int currentIndex) {
        imagePreview(imageList, currentIndex, false);
    }

    public void imagePreview(List<ImageModel> imageList, int currentIndex, boolean isEditable) {
        if (isContextNull()) return;
        Context context = weakReference.get();
        Intent intent = new Intent(context, ImageViewPagerActivity.class);
        intent.putExtra(ImageConfig.IMAGE_LIST, toArrayList(imageList));
        intent.putExtra(ImageConfig.IMAGE_INDEX, currentIndex);
        intent.putExtra(ImageConfig.IMAGE_IS_EDITABLE, isEditable);
        context.startActivity(intent);

    }

    private boolean isContextNull() {
        return weakReference.get() == null;
    }

    private ArrayList<ImageModel> toArrayList(List<ImageModel> list) {
        return new ArrayList<>(list);
    }
}