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