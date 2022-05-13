package org.glygen.array.client;

import java.util.List;

import org.glygen.array.client.model.data.StatisticalMethod;
import org.glygen.array.client.model.template.MetadataTemplate;

public interface PublicUtilitiyClient {
    
    List<StatisticalMethod> getAllStatisticalMethods();
    void setUrl(String url);
    MetadataTemplate getTemplate (String id);
}
