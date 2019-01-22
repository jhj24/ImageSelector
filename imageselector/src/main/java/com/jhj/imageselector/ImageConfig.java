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

    private int colorPrimaryDark; //状态栏背景色
    private int colorPrimary; //标题栏背景色
    private int icLeftBack; //返回键图标
    private int titleArrowUp; //标题上拉箭头
    private int titleArrowDown; //标题下拉箭头
    private int titleTextColor; //标题文字颜色
    private int titleTextSize; //标题文字大小
    private int rightTextColor; //右边文字颜色
    private int rightTextSize; //右边文字大小
    private int icSelected; //图片选中时图标
    private int icUnSelected; // 图片未选中时图标
    private int bottomBackground; //底图预览背景色
    private int previewTextColor; //预览字体背景色
    private int selectedNumBackground;//图片已选数量圆点背景色


    private int minSelectNum;
    private boolean isCompress = false;
    private boolean isCrop;
    private boolean isAnim;
    private SelectedMode selectMode;





    public int getMinSelectNum() {
        return minSelectNum;
    }

    public void setMinSelectNum(int minSelectNum) {
        this.minSelectNum = minSelectNum;
    }

    public boolean isCompress() {
        return isCompress;
    }

    public void setCompress(boolean compress) {
        isCompress = compress;
    }

    public boolean isCrop() {
        return isCrop;
    }

    public void setCrop(boolean crop) {
        isCrop = crop;
    }

    public boolean isAnim() {
        return isAnim;
    }

    public void setAnim(boolean anim) {
        isAnim = anim;
    }

    public SelectedMode getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(SelectedMode selectMode) {
        this.selectMode = selectMode;
    }

    public enum SelectedMode {
        SINGLE,
        MULTI
    }

}