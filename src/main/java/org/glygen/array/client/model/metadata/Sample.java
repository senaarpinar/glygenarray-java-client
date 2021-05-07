package org.glygen.array.client.model.metadata;

public class Sample extends MetadataCategory {
    
    String internalId;
    
    public Sample() {
    }
    
    public Sample(MetadataCategory metadata) {
        super (metadata);
        if (metadata instanceof Sample)
            this.internalId = ((Sample) metadata).internalId;
    }
    
    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }
    
    public String getInternalId() {
        return internalId;
    }
    
}
