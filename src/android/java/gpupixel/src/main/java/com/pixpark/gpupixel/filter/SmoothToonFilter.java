package com.pixpark.gpupixel.filter;

//
public final class SmoothToonFilter extends GPUPixelFilter {
    private static final String name = "SmoothToonFilter";
    private static final String propBlurRadiusLevel = "blurRadius";
    private static final String propThresholdLevel = "toonThreshold";
    private static final String propQuantizationLevel = "toonQuantizationLevels";

    private float blurRadiusLevel = 1.0f;
    private float thresholdLevel = 0.2f;

    private float quantizationLevel = 10.0f;

    public SmoothToonFilter() {
        super(name);
    }

    public float getBlurRadiusLevel() {
        return blurRadiusLevel;
    }

    public void setBlurRadiusLevel(float smoothLevel) {
        this.blurRadiusLevel = smoothLevel;
        setProperty(propBlurRadiusLevel, smoothLevel);
    }

    public float getThresholdLevel() {
        return thresholdLevel;
    }

    public void setThresholdLevel(float whiteLevel) {
        this.thresholdLevel = whiteLevel;
        setProperty(propThresholdLevel, whiteLevel);
    }

    public void setPropQuantizationLevel(float quantizationLevel) {
        this.quantizationLevel = quantizationLevel;
        setProperty(propQuantizationLevel, quantizationLevel);
    }

    public float getQuantizationLevel() {
        return quantizationLevel;
    }
}