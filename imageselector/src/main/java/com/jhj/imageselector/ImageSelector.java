package com.jhj.imageselector;

import android.app.Activity;
import android.content.Intent;

import com.jhj.imageselector.activityresult.ActivityResult;
import com.jhj.imageselector.bean.LocalMedia;
import com.jhj.imageselector.ui.ImagePreviewActivity;
import com.jhj.imageselector.ui.ImageSelectorActivity;
import com.jhj.imageselector.config.ImageExtra;
import com.jhj.imageselector.utils.ExtensionKt;

import java.util.List;

public class ImageSelector {

    private final Activity mActivity;

    private ImageSelector(Activity activity) {
        this.mActivity = activity;
    }

    public static ImageSelector with(Activity activity) {
        return new ImageSelector(activity);
    }

    public void imageSelected(final OnImageSelectedListener listener) {
        ActivityResult.with(mActivity)
                .targetActivity(ImageSelectorActivity.class)
                .putInt(ImageExtra.EXTRA_SELECTED_MODE, ImageExtra.MULTI)
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
        mActivity.overridePendingTransition(R.anim.activity_fade_out,0);
    }


    public void imageSelected(int selectedMode, int selectedMaxNum, int selectedMinNum, final OnImageSelectedListener listener) {
        ActivityResult.with(mActivity)
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


    public void imagePreview(List<LocalMedia> imageList) {
        imagePreview(imageList, 0);
    }

    public void imagePreview(List<LocalMedia> imageList, int currentIndex) {
        imagePreview(imageList, currentIndex, false);
    }

    public void imagePreview(List<LocalMedia> imageList, int currentIndex, boolean isDelete) {

        Intent intent = new Intent(mActivity, ImagePreviewActivity.class);
        intent.putExtra(ImageExtra.IMAGE_LIST, ExtensionKt.toArrayList(imageList));
        intent.putExtra(ImageExtra.IMAGE_INDEX, currentIndex);
        intent.putExtra(ImageExtra.IMAGE_IS_DELETE, isDelete);
        mActivity.startActivity(intent);
        mActivity.overridePendingTransition(R.anim.activity_fade_out,0);
    }

    public interface OnImageSelectedListener {
        void onSelected(List<LocalMedia> list);
    }
}