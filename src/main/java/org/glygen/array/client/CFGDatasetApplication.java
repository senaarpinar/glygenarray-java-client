package org.glygen.array.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.glygen.array.client.exception.CustomClientException;
import org.glygen.array.client.model.User;
import org.glygen.array.client.model.data.ArrayDataset;
import org.glygen.array.client.model.data.FileWrapper;
import org.glygen.array.client.model.data.Image;
import org.glygen.array.client.model.data.PrintedSlide;
import org.glygen.array.client.model.data.ProcessedData;
import org.glygen.array.client.model.data.RawData;
import org.glygen.array.client.model.data.Slide;
import org.glygen.array.client.model.data.StatisticalMethod;
import org.glygen.array.client.model.metadata.Sample;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.Logger;

@SpringBootApplication
@Configuration
public class CFGDatasetApplication implements CommandLineRunner {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Application.class);
    
    
    @Bean
    @ConfigurationProperties("glygen")
    public GlygenSettings glygen() {
        return new GlygenSettings();
    }

    public static void main(String args[]) {
        new SpringApplicationBuilder(CFGDatasetApplication.class).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        GlygenSettings settings = glygen();
        
        String url = settings.scheme + settings.host + settings.basePath;
        UserRestClient userClient = new UserRestClientImpl();
        userClient.setURL(url);
        
        if (args == null || args.length < 3) {
            log.error("need to pass username and password and processed file folder as arguments");
            return;
        }
        userClient.login(args[0], args[1]);
        User user = userClient.getUser(args[0]);
        log.info("got user information:" + user.getEmail());
        
        // read all folders in the given folder
        File dataFolder = new File (args[2]);
        if (!dataFolder.isDirectory()) {
            log.error(args[2] + " is not a folder");
            return;
        }
        
        
        FileUploadClient fileClient = new FileUploadClientImpl();
        fileClient.setURL(url);
        fileClient.setUsername(args[0]);
        fileClient.setPassword(args[1]);
        
        DatasetRestClient datasetClient = new DatasetRestClientImpl();
        datasetClient.setURL(url);
        datasetClient.setUsername(args[0]);
        datasetClient.setPassword(args[1]);
        
        PublicUtilitiyClient utilClient = new PublicUtilityClientImpl();
        utilClient.setUrl(url);
        
        StatisticalMethod method = null;
        List<StatisticalMethod> methods = utilClient.getAllStatisticalMethods();
        for (StatisticalMethod m: methods) {
            if (m.getName().equalsIgnoreCase("eliminate"))
                method = m;
        }
        
        String[] datasetFolders = dataFolder.list();
        for (String experimentName: datasetFolders) {
            File experimentFolder = new File (dataFolder + File.separator + experimentName);
            if (experimentFolder.isDirectory()) {
                String processedDataFile = null;
                String rawDataFile = null;
                for (String filename: experimentFolder.list()) {
                    if (filename.endsWith(".xls")) {
                        processedDataFile = filename;
                    } else if (filename.endsWith(".txt"))
                        rawDataFile = filename;
                }
                
                if (processedDataFile == null) //||  !processedDataFile.startsWith("unknown"))
                    continue;
                
                String sampleName = processedDataFile.substring(0, processedDataFile.lastIndexOf(".xls"));
                Sample sample = new Sample();
                sample.setName(sampleName);
                sample.setTemplate("Protein Sample");
                
                try {
                    // create the sample
                    String sampleId = datasetClient.getSampleByLabel(sampleName);
                    if (sampleId == null) {
                        try {
                            sampleId = datasetClient.addSample(sample);
                        } catch (CustomClientException e1) {
                            System.out.println ("Error adding the sample: " + e1.getBody());
                        }
                    }
                    sample.setId(sampleId);
                    
                    String datasetID = null;
                    ArrayDataset existing = datasetClient.getDatasetByLabel (experimentName);
                    if (existing == null) {
                        try { 
                            // create the array dataset
                            datasetID = datasetClient.addDataset(experimentName, sample);
                        } catch (CustomClientException e1) {
                            System.out.println ("Error adding the dataset: " + experimentName + "Reason: " + e1.getBody());
                        }
                    } else {
                        datasetID = existing.getId();
                    }
                    
                    FileWrapper file = null;
                    FileWrapper rawFile = null;
                    if (existing != null) {
                        if (rawDataFile != null) {
                            for (RawData data: existing.getRawDataList()) {
                                if (data.getFile() != null) {
                                    if (data.getFile().getOriginalName().equals(rawDataFile)) {
                                        // skip uploading
                                        rawFile = data.getFile();
                                    }
                                }
                                if (processedDataFile != null) {
                                    for (ProcessedData processedData: data.getProcessedDataList()) {
                                        if (processedData.getFile() != null) {
                                            if (processedData.getFile().getOriginalName().equals(processedDataFile)) {
                                                // skip uploading
                                                file = processedData.getFile();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Slide slide = new Slide();
                    PrintedSlide printedSlide = new PrintedSlide();
                    printedSlide.setName("CFG5.2PrintedSlide");
                    slide.setPrintedSlide(printedSlide);
                    slide.setImages(new ArrayList<Image>());
                    
                    Image image = new Image();
                    RawData rawData = new RawData();
                    rawData.setProcessedDataList(new ArrayList<ProcessedData>());
                    image.setRawData(rawData);
                    slide.getImages().add(image);
                    
                    if (rawDataFile != null && rawFile == null) {
                        // upload the file
                        file = fileClient.uploadFile(dataFolder.getPath() + File.separator + experimentName + File.separator + rawDataFile);
                        file.setFileFormat("GenePix Results 3");
                        
                        rawData.setFile(file);
                        rawData.setPowerLevel(10.0);
                    }
                    
                    //if (file == null) {
                        // upload the file
                        file = fileClient.uploadFile(dataFolder.getPath() + File.separator + experimentName + File.separator + processedDataFile);
                        file.setFileFormat("CFG_V5.2");
                        
                        ProcessedData processedData = new ProcessedData();
                        processedData.setFile(file);
                        processedData.setMethod(method);
                        
                        rawData.getProcessedDataList().add(processedData);
                        
                        // add slide
                        String slideId = datasetClient.addSlideToDataset(slide, datasetID);
                        log.info("Added slide " + slideId + " for " + datasetID);
                     
                    //}
                } catch (CustomClientException e) { 
                    System.out.println (e.getBody());
                    System.out.println("Failed: " + dataFolder.getName() + File.separator + experimentName + File.separator + processedDataFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed: " + dataFolder.getName() + File.separator + experimentName + File.separator + processedDataFile);
                }
            }
        }
    }
    
    public class GlygenSettings {
        String host;
        String scheme;
        String basePath;
        /**
         * @return the host
         */
        public String getHost() {
            return host;
        }
        /**
         * @param host the host to set
         */
        public void setHost(String host) {
            this.host = host;
        }
        /**
         * @return the scheme
         */
        public String getScheme() {
            return scheme;
        }
        /**
         * @param scheme the scheme to set
         */
        public void setScheme(String scheme) {
            this.scheme = scheme;
        }
        /**
         * @return the basePath
         */
        public String getBasePath() {
            return basePath;
        }
        /**
         * @param basePath the basePath to set
         */
        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }
}
