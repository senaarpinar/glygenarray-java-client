package org.glygen.array.client.model.data;

import java.util.List;

import org.glygen.array.client.model.Feature;

public class TechnicalExclusionInfo { 
    List<Feature> features;
    TechnicalExclusionReasonType reason;
    String otherReason;
    
    /**
     * @return the reason
     */
    public TechnicalExclusionReasonType getReason() {
        return reason;
    }
    /**
     * @param reason the reason to set
     */
    public void setReason(TechnicalExclusionReasonType reason) {
        this.reason = reason;
    }
    /**
     * @return the otherReason
     */
    public String getOtherReason() {
        return otherReason;
    }
    /**
     * @param otherReason the otherReason to set
     */
    public void setOtherReason(String otherReason) {
        this.otherReason = otherReason;
    }
    /**
     * @return the features
     */
    public List<Feature> getFeatures() {
        return features;
    }
    /**
     * @param features the features to set
     */
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}
