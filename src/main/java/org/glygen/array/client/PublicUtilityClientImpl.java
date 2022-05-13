package org.glygen.array.client;

import java.util.List;

import org.glygen.array.client.model.data.StatisticalMethod;
import org.glygen.array.client.model.template.MetadataTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PublicUtilityClientImpl implements PublicUtilitiyClient {
    
    private RestTemplate restTemplate = new RestTemplate();
    String url;

    @Override
    public List<StatisticalMethod> getAllStatisticalMethods() {
        
        String url = this.url + "util/statisticalmethods";
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            ResponseEntity<List> response = this.restTemplate.exchange(url, HttpMethod.GET, null, List.class);
            List list = response.getBody();
            List<StatisticalMethod> methods = mapper.convertValue(list, new TypeReference<List<StatisticalMethod>>() { });
            return methods;
        } catch (HttpClientErrorException e) {
            System.out.println ("Error gettting classification list: " + e.getMessage());
        }
        return null;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public MetadataTemplate getTemplate(String id) {
        //http://localhost:8080/util/getTemplate/Metadata6
        String url = this.url + "util/getTemplate/" + id;
        try {
            ResponseEntity<MetadataTemplate> response = this.restTemplate.exchange(url, HttpMethod.GET, null, MetadataTemplate.class);
            MetadataTemplate template = response.getBody();
            return template;
        } catch (HttpClientErrorException e) {
            System.out.println ("Error gettting metadata template: " + e.getMessage());
        }
        return null;
    }

}
