package org.glygen.array.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.glygen.array.client.exception.CustomClientException;
import org.glygen.array.client.model.ArrayDatasetListView;
import org.glygen.array.client.model.LoginRequest;
import org.glygen.array.client.model.MetadataListResultView;
import org.glygen.array.client.model.data.ArrayDataset;
import org.glygen.array.client.model.data.FileWrapper;
import org.glygen.array.client.model.data.Image;
import org.glygen.array.client.model.data.PrintedSlide;
import org.glygen.array.client.model.data.ProcessedData;
import org.glygen.array.client.model.data.RawData;
import org.glygen.array.client.model.data.Slide;
import org.glygen.array.client.model.metadata.AssayMetadata;
import org.glygen.array.client.model.metadata.DataProcessingSoftware;
import org.glygen.array.client.model.metadata.ImageAnalysisSoftware;
import org.glygen.array.client.model.metadata.MetadataCategory;
import org.glygen.array.client.model.metadata.Printer;
import org.glygen.array.client.model.metadata.Sample;
import org.glygen.array.client.model.metadata.ScannerMetadata;
import org.glygen.array.client.model.metadata.SlideMetadata;
import org.glygen.array.client.model.metadata.SpotMetadata;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DatasetRestClientImpl implements DatasetRestClient {
    
    private RestTemplate restTemplate = new RestTemplate();

    String token=null;
    String username;
    String password;
    String url = "http://localhost:8080/";

    @Override
    public void setUsername(String username) {
        this.username = username;

    }

    @Override
    public void setPassword(String password) {
       this.password = password;
    }

    @Override
    public void setURL(String url) {
       this.url = url;
    }
    
    @Override
    public void clearToken () {
        this.token = null;
    }

    @Override
    public String addDataset(String experimentName, String description, Sample sample, Date date) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        
        // first add the dataset
        ArrayDataset dataset = new ArrayDataset();
        dataset.setName(experimentName);
        dataset.setSample(sample);
        dataset.setDescription(description);
        dataset.setDateCreated(date);
        
       /* try {
            String result = new ObjectMapper()
                    .writeValueAsString(dataset);
            System.out.println(result);
            ArrayDataset test = new ObjectMapper().readValue(result, ArrayDataset.class);
            System.out.println ("converted back to object");
        } catch (JsonProcessingException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

        HttpEntity<ArrayDataset> requestEntity = new HttpEntity<ArrayDataset>(dataset, headers);
        String url = this.url + "array/addDataset";
        
        String datasetId = null;
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            datasetId = response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
        return datasetId;
    }
    
    @Override
    public String addSlideToDataset(Slide slide, String datasetId) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        
        HttpEntity<Slide> requestEntity = new HttpEntity<Slide> (slide, headers);
        String url = this.url + "array/addSlide?arraydatasetId=" + datasetId;
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
                return null;
            }
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
        
    }
    
    @Override
    public String addImageToSlide(Image image, String slideId, String datasetId) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        
        HttpEntity<Image> requestEntity = new HttpEntity<Image> (image, headers);
        String url = this.url + "array/addImage?arraydatasetId=" + datasetId + "&slideId=" + slideId;
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
                return null;
            }
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
        
    }

    @Override
    public String addSample(Sample sample) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Sample> requestEntity = new HttpEntity<Sample>(sample, headers);
        String url = this.url + "array/addSample";
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }
    
    @Override
    public String addPrinterMetadata(Printer printer) {
        return addPrinterMetadata(printer, true);
    }
    
    @Override
    public String addSpotMetadata(SpotMetadata metadata) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<SpotMetadata> requestEntity = new HttpEntity<SpotMetadata>(metadata, headers);
        String url = this.url + "array/addSpotMetadata"; 
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }
    
    public void login(String username, String password) throws CustomClientException {
        // login to the system and set the token
        this.username = username;
        this.password = password;
        String url = this.url + "login";
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);
        HttpEntity<LoginRequest> requestEntity = new HttpEntity<LoginRequest>(loginRequest);
        HttpEntity<Void> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
        HttpHeaders header = response.getHeaders();
        this.token = header.getFirst("Authorization");
    }

    @Override
    public String addPrinterMetadata(Printer printer, boolean validate) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Printer> requestEntity = new HttpEntity<Printer>(printer, headers);
        String url = this.url + "array/addPrinter?validate=" + validate; 
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addScannerMetadata(ScannerMetadata scanner) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<ScannerMetadata> requestEntity = new HttpEntity<ScannerMetadata>(scanner, headers);
        String url = this.url + "array/addScanner";  
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addSlideMetadata(SlideMetadata slideMetadata) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<SlideMetadata> requestEntity = new HttpEntity<SlideMetadata>(slideMetadata, headers);
        String url = this.url + "array/addSlideMetadata";  
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addImageAnalysisMetadata(ImageAnalysisSoftware metadata) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<ImageAnalysisSoftware> requestEntity = new HttpEntity<ImageAnalysisSoftware>(metadata, headers);
        String url = this.url + "array/addImageAnalysis";  
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addDataProcessingMetadata(DataProcessingSoftware metadata) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<DataProcessingSoftware> requestEntity = new HttpEntity<DataProcessingSoftware>(metadata, headers);
        String url = this.url + "array/addDataProcessingSoftware"; 
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addAssayMetadata(AssayMetadata metadata) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<AssayMetadata> requestEntity = new HttpEntity<AssayMetadata>(metadata, headers);
        String url = this.url + "array/addAssayMetadata"; 
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }
    
    @Override
    public String addPrintedSlide(PrintedSlide printedSlide) {
        if (token == null) login (this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<PrintedSlide> requestEntity = new HttpEntity<PrintedSlide>(printedSlide, headers);
        String url = this.url + "array/addPrintedSlide";
        
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String getDataProcessingMetadataByLabel(String name) {
        return getMetadataByLabel(name, "listDataProcessingSoftware");
    }

    @Override
    public String getAssayMetadataByLabel(String name) {
        return getMetadataByLabel(name, "listAssayMetadata");
    }
    
    @Override
    public String getSpotMetadataByLabel(String name) {
        return getMetadataByLabel(name, "listSpotMetadata");
    }
    
    String getMetadataByLabel (String name, String webservice) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        String url = this.url + "array/" + webservice +"?offset=0&filter=" + name;
        try {
            
            ResponseEntity<MetadataListResultView> response = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, MetadataListResultView.class);
            MetadataListResultView result = response.getBody();
            for (MetadataCategory metadata: result.getRows()) {
                if (metadata.getName().equalsIgnoreCase(name)) {
                    return metadata.getId();
                }
            }
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), "Error gettting metadata list: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String getImageAnalysisMetadataByLabel(String name) {
        return getMetadataByLabel(name, "listImageAnalysisSoftware");
    }

    @Override
    public String getSlideMetadataByLabel(String name) {
        return getMetadataByLabel(name, "listSlideMetadata");
    }

    @Override
    public String getScannerByLabel(String name) {
        return getMetadataByLabel(name, "listScanners");
    }

    @Override
    public String getPrinterByLabel(String name) {
        return getMetadataByLabel(name, "listPrinters");
    }

    @Override
    public String getSampleByLabel(String name) {
        return getMetadataByLabel(name, "listSamples");
    }

    @Override
    public ArrayDataset getDatasetByLabel(String name) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        String url = this.url + "array/listArrayDataset?offset=0&loadAll=false&filter=" + name;
        try {
            
            ResponseEntity<ArrayDatasetListView> response = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity, ArrayDatasetListView.class);
            ArrayDatasetListView result = response.getBody();
            for (ArrayDataset dataset: result.getRows()) {
                if (dataset.getName().equalsIgnoreCase(name)) {
                    return dataset;
                }
            }
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), "Error gettting array dataset list: " + e.getMessage());
        }
        return null;
    }

    @Override
    public String addRawDataToImage(RawData rawData, String imageId, String datasetId) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        
        HttpEntity<RawData> requestEntity = new HttpEntity<RawData> (rawData, headers);
        String url = this.url + "array/addRawdata?arraydatasetId=" + datasetId + "&imageId=" + imageId;
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
                return null;
            }
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

    @Override
    public String addProcessedDataToRawData(ProcessedData processedData, String rawDataId, String datasetId) {
        if (token == null) login (this.username, this.password);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        
        HttpEntity<FileWrapper> requestEntity = new HttpEntity<FileWrapper> (processedData.getFile(), headers);
        String url = this.url + "array/addProcessedDataFromExcel?arraydatasetId=" + datasetId + "&rawdataId=" + rawDataId +"&methodName=" + processedData.getMethod().getName()
                + (processedData.getMetadata() == null ? "" : "&metadataId=" + processedData.getMetadata().getId()) ;
        try {
            ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.REQUEST_TIMEOUT) {
                return null;
            }
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        } catch (HttpServerErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), e.getMessage());
        }
    }

}
