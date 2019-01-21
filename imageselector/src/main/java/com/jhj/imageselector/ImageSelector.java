package com.jhj.imageselector;

import android.app.Activity;
import android.content.Intent;

import com.jhj.imageselector.activityresult.ActivityResult;
import com.jhj.imageselector.ui.ImagePreviewActivity;
import com.jhj.imageselector.ui.ImageSelectorActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImageSelector {

    private static volatile ImageSelector singleton;

    private WeakReference<Activity> weakReference;

    private ImageSelector(Activity context) {
        weakReference = new WeakReference<Activity>(context);
    }

    public static ImageSelector getInstance(Activity context) {
        if (singleton == null) {
            synchronized (ImageSelector.class) {
                if (singleton == null) {
                    singleton = new ImageSelector(context);
                }
            }
        }
        return singleton;
    }

    public void imageSelected(final OnImageSelectedListener listener) {
        if (isContextNull()) return;
        Activity context = weakReference.get();
        ActivityResult.getInstance(context)
                .targetActivity(ImageSelectorActivity.class)
                .putInt(ImageExtra.EXTRA_SELECTED_MODE, 1)
                .putInt(ImageExtra.EXTRA_SELECTED_MAX_NUM, 9)
                .putInt(ImageExtra.EXTRA_SELECTED_MIN_NUM, 1)
                .onResult(new ActivityResult.OnActivityResultListener() {
                    @Override
                    public void onResult(Intent data) {
                        if (data!= null){
                            Object ob = data.getSerializableExtra(ImageExtra.EXTRA_SELECTED_RESULT);
                            if (ob != null) {
                                listener.onSelected((List<LocalMedia>) ob);
                            }
                        }


                    }
                });
    }


    public void imageSelected(int selectedMode, int selectedMaxNum, int selectedMinNum, final OnImageSelectedListener listener) {
        if (isContextNull()) return;
        Activity context = weakReference.get();
        ActivityResult.getInstance(context)
                .targetActivity(ImageSelectorActivity.class)
                .putInt(ImageExtra.EXTRA_SELECTED_MODE, selectedMode)
                .putInt(ImageExtra.EXTRA_SELECTED_MAX_NUM, selectedMaxNum)
                .putInt(ImageExtra.EXTRA_SELECTED_MIN_NUM, selectedMinNum)
                .onResult(new ActivityResult.OnActivityResultListener() {
                    @Override
                    public void onResult(Intent data) {
                        List<LocalMedia> localMediaList = (List<LocalMedia>) data.getSerializableExtra(ImageExtra.EXTRA_SELECTED_RESULT);
                        listener.onSelected(localMediaList);
                    }
                });
    }


    public void imagePreview(List<ImageModel> imageList) {
        imagePreview(imageList, 0);
    }

    public void imagePreview(List<ImageModel> imageList, int currentIndex) {
        imagePreview(imageList, currentIndex, false);
    }

    public void imagePreview(List<ImageModel> imageList, int currentIndex, boolean isEditable) {
        if (isContextNull()) return;
        Activity context = weakReference.get();
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(ImageExtra.IMAGE_LIST, toArrayList(imageList));
        intent.putExtra(ImageExtra.IMAGE_INDEX, currentIndex);
        intent.putExtra(ImageExtra.IMAGE_IS_EDITABLE, isEditable);
        context.startActivity(intent);

    }

    private boolean isContextNull() {
        return weakReference.get() == null;
    }

    private ArrayList<ImageModel> toArrayList(List<ImageModel> list) {
        return new ArrayList<>(list);
    }

    public interface OnImageSelectedListener {
        void onSelected(List<LocalMedia> list);
    }
}