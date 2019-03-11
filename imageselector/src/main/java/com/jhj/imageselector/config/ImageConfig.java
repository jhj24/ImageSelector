package com.jhj.imageselector.config;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;

import com.jhj.imageselector.R;
import com.yalantis.ucrop.UCrop;

public class ImageConfig {

    private static volatile ImageConfig singleton;

    private ImageConfig() {
    }

    public static ImageConfig getInstance() {
        if (singleton == null) {
            synchronized (ImageConfig.class) {
                if (singleton == null) {
                    singleton = new ImageConfig();
                }
            }
        }
        return singleton;
    }

    @ColorRes
    public int statusBarDark = R.color.colorPrimaryDark1; //状态栏背景色
    @ColorRes
    public int topbarPrimary = R.color.colorPrimary1; //标题栏背景色
    @DrawableRes
    public int titleLeftBackImage = R.drawable.arrow_back; //返回键图标
    @DrawableRes
    public int titleArrowUpImage = R.drawable.arrow_up; //标题上拉箭头
    @DrawableRes
    public int titleArrowDownImage = R.drawable.arrow_down; //标题下拉箭头
    @ColorRes
    public int titleTextColor = R.color.white; //标题文字颜色
    @ColorRes
    public int rightTextColor = R.color.white; //右边文字颜色
    @DrawableRes
    public int selectedStateImage = R.drawable.image_checked; //图片选中时图标
    @DrawableRes
    public int unSelectedStateImage = R.drawable.image_unchecked; // 图片未选中时图标
    @ColorRes
    public int bottomBackgroundColor = R.color.white; //底图预览背景色
    @ColorRes
    public int previewTextColor = R.color.orange; //预览字体背景色
    @DrawableRes
    public int previewNumBackground = R.drawable.orange_oval;//图片已选数量圆点背景色


    public UCrop.Options uCropOptions;
    public int uCropPressQuality = 90;
    public float uCropScaleX = 1;
    public float uCropScaleY = 1;
    public int uCropWidth = 0;
    public int uCropHeight = 0;
    public boolean isHideUCropBottomControl = true;  // 是否隐藏底部容器
    public boolean isFreeStyleCropEnabled = true; //是否能调整裁剪框

    public boolean isImageAnim = true;
    public boolean isOnlyCamera = false;
    public boolean isAllowTakePhoto = true;
    public int maxSelectNum = 9;
    public int minSelectNum = 1;
    public boolean isCompress = true;
    public boolean isCrop = false;
    public int selectMode = ImageExtra.MULTI;
    public int compressSize = 100;


}