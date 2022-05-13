package org.glygen.array.client.model.template;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MetadataTemplateType {
    
    SAMPLE("sample_template"),
    PRINTER("printer_template"),
    SCANNER("scanner_template"),
    SLIDE("slide_template"),
    DATAPROCESSINGSOFTWARE("data_processing_software_template"),
    IMAGEANALYSISSOFTWARE("image_analysis_software_template"),
    ASSAY("assay_template"),
    FEATURE("feature_template"),
    SPOT("spot_template"),
    PRINTRUN("printrun_template");
    
    String label;
    
    private MetadataTemplateType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
    
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    @JsonCreator
    public static MetadataTemplateType forValue(String value) {
        if (value.equals("sample_template"))
            return SAMPLE;
        else if (value.equals("printer_template"))
            return PRINTER;
        else if (value.equals("scanner_template"))
            return SCANNER;
        else if (value.equals("slide_template"))
            return SLIDE;
        else if (value.equals("data_processing_software_template"))
            return DATAPROCESSINGSOFTWARE;
        else if (value.equals("image_analysis_software_template"))
            return IMAGEANALYSISSOFTWARE;
        else if (value.equals("assay_template"))
            return ASSAY;
        else if (value.equals("spot_template"))
            return SPOT;
        else if (value.equals("feature_template"))
            return FEATURE;
        else if (value.equals("printrun_template"))
            return PRINTRUN;
        return SAMPLE;
    }
}
