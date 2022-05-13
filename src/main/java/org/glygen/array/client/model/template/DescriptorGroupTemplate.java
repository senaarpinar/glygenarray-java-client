package org.glygen.array.client.model.template;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("descriptorgrouptemplate")
public class DescriptorGroupTemplate extends DescriptionTemplate {

    List<DescriptionTemplate> descriptors;
    
    //@JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore
    @Override
    public boolean isGroup() {
        return true;
    }

    /**
     * @return the descriptors
     */
    public List<DescriptionTemplate> getDescriptors() {
        return descriptors;
    }

    /**
     * @param descriptors the descriptors to set
     */
    public void setDescriptors(List<DescriptionTemplate> descriptors) {
        this.descriptors = descriptors;
    }

}
