package com.jhj.imageselector;

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

    public int colorPrimaryDark; //状态栏背景色
    public int colorPrimary; //标题栏背景色
    public int icLeftBack; //返回键图标
    public int titleArrowUp; //标题上拉箭头
    public int titleArrowDown; //标题下拉箭头
    public int titleTextColor; //标题文字颜色
    public int titleTextSize; //标题文字大小
    public int rightTextColor; //右边文字颜色
    public int rightTextSize; //右边文字大小
    public int icSelected; //图片选中时图标
    public int icUnSelected; // 图片未选中时图标
    public int bottomBackground; //底图预览背景色
    public int previewTextColor = R.color.orange; //预览字体背景色
    public int selectedNumBackground;//图片已选数量圆点背景色

    public boolean isImageAnim = true;
    public boolean isOnlyCamera = false;
    public boolean isAllowTakePhoto = true;
    public int maxSelectNum = 9;
    public int minSelectNum = 1;
    public boolean isCompress = false;
    public boolean isCrop = false;
    public int selectMode = ImageExtra.MULTI;
    public int compressSize = 100;


}