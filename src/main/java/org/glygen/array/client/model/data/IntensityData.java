package org.glygen.array.client.model.data;

import org.glygen.array.client.model.Feature;

public class IntensityData {
    Feature feature;
    Intensity intensity;
    /**
     * @return the feature
     */
    public Feature getFeature() {
        return feature;
    }
    /**
     * @param feature the feature to set
     */
    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    /**
     * @return the intensity
     */
    public Intensity getIntensity() {
        return intensity;
    }
    /**
     * @param intensity the intensity to set
     */
    public void setIntensity(Intensity intensity) {
        this.intensity = intensity;
    }
}
