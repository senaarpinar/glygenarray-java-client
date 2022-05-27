package org.glygen.array.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.glygen.array.client.exception.CustomClientException;
import org.glygen.array.client.model.ErrorCodes;
import org.glygen.array.client.model.ErrorMessage;
import org.glygen.array.client.model.User;
import org.glygen.array.client.model.cfg.Experiment;
import org.glygen.array.client.model.cfg.SampleData;
import org.glygen.array.client.model.data.ArrayDataset;
import org.glygen.array.client.model.data.FileWrapper;
import org.glygen.array.client.model.data.FutureTaskStatus;
import org.glygen.array.client.model.data.Image;
import org.glygen.array.client.model.data.PrintedSlide;
import org.glygen.array.client.model.data.ProcessedData;
import org.glygen.array.client.model.data.RawData;
import org.glygen.array.client.model.data.Slide;
import org.glygen.array.client.model.data.StatisticalMethod;
import org.glygen.array.client.model.metadata.Description;
import org.glygen.array.client.model.metadata.Descriptor;
import org.glygen.array.client.model.metadata.DescriptorGroup;
import org.glygen.array.client.model.metadata.Sample;
import org.glygen.array.client.model.template.DescriptionTemplate;
import org.glygen.array.client.model.template.DescriptorGroupTemplate;
import org.glygen.array.client.model.template.MetadataTemplate;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.classic.Logger;

@SpringBootApplication
@Configuration
public class CFGDatasetApplication implements CommandLineRunner {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Application.class);
    
    SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    
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
        String token = userClient.getToken();
        
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
            try {
                File experimentFolder = new File (dataFolder + File.separator + experimentName);
                if (experimentFolder.isDirectory()) {
                    System.out.println("Procesing " + experimentName);
                    Experiment experiment = getExperimentDetails(experimentName, url, token);
                    String processedDataFile = null;
                    String rawDataFile = null;
                    for (String filename: experimentFolder.list()) {
                        if (filename.endsWith(".xls")) {
                            processedDataFile = filename;
                        } else if (filename.endsWith(".txt"))
                            rawDataFile = filename;
                    }
                    
                    if (processedDataFile == null || processedDataFile.startsWith("unknown")) {
                        System.out.println ("Skipping " + experimentName + " No processed data is found!");
                        continue;
                    }
                    
                    Sample sample = createSample (experiment, utilClient);
                    
                    // add the sample
                    String sampleId = datasetClient.getSampleByLabel(sample.getName());
                    if (sampleId == null) {
                        try {
                            sampleId = datasetClient.addSample(sample);
                        } catch (CustomClientException e1) {
                            System.out.println ("Error adding the sample: " + e1.getBody());
                        }
                    }
                    sample.setId(sampleId);
                    // clear the descriptors, we only need the name and id for the rest 
                    // (while adding a dataset, json conversion fails for descriptortemplates for some unknown reason)
                    sample.setDescriptorGroups(null);
                    sample.setDescriptors(null);
                    
                    String datasetName = sample.getName();
                    
                    if (datasetName == null || datasetName.trim().isEmpty()) {
                        datasetName = experiment.getSampleName().substring(experiment.getSampleName().lastIndexOf(":"), experiment.getSampleName().length()-1);
                    }
                    datasetName += ":" + dt1.format(experiment.getDate()) + ":" + experiment.getPrimScreen();
                    String datasetID = null;
                    ArrayDataset existing = datasetClient.getDatasetByLabel (datasetName);
                    if (existing == null) {
                        try { 
                            // create the array dataset
                            datasetID = datasetClient.addDataset(datasetName, sample);
                        } catch (CustomClientException e1) {
                            System.out.println ("Error adding the dataset: " + datasetName + "Reason: " + e1.getBody());
                            continue;
                        }
                    } else {
                        datasetID = existing.getId();
                    }
                    
                    FileWrapper file = null;
                    FileWrapper rawFile = null;
                    if (existing != null && existing.getSlides() != null) {
                        for (Slide slide: existing.getSlides()) {
                            if (slide.getImages() == null) continue;
                            for (Image image: slide.getImages()) {
                                if (image.getRawDataList() == null) continue;
                                for (RawData data: image.getRawDataList()) {
                                    if (data.getFile() != null) {
                                        if (data.getFile().getOriginalName().equals(rawDataFile)) {
                                            // skip uploading
                                            rawFile = data.getFile();
                                        }
                                    }
                                    if (processedDataFile != null) {
                                        if (data.getProcessedDataList() == null) continue;
                                        for (ProcessedData processedData: data.getProcessedDataList()) {
                                            if (processedData.getFile() != null) {
                                                if (processedData.getFile().getOriginalName() != null &&
                                                        processedData.getFile().getOriginalName().equals(processedDataFile)) {
                                                    if (processedData.getStatus() == FutureTaskStatus.DONE) {
                                                        // skip uploading
                                                        file = processedData.getFile();
                                                    }
                                                }
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
                    image.addRawData(rawData);
                    slide.getImages().add(image);
                    //rawData.setSlide(slide);
                    
                    if (rawDataFile != null && rawFile == null) {
                        // upload the file
                        file = fileClient.uploadFile(dataFolder.getPath() + File.separator + experimentName + File.separator + rawDataFile);
                        file.setFileFormat("GenePix Results 3");
                        
                        rawData.setFile(file);
                        rawData.setPowerLevel(10.0);
                    }
                    
                    if (file == null) {
                        // upload the file
                        file = fileClient.uploadFile(dataFolder.getPath() + File.separator + experimentName + File.separator + processedDataFile);
                        file.setFileFormat("CFG_V5.2");
                        
                        ProcessedData processedData = new ProcessedData();
                        processedData.setFile(file);
                        processedData.setMethod(method);
                        
                        rawData.getProcessedDataList().add(processedData);
                        
                        // add slide
                        String slideId = datasetClient.addSlideToDataset(slide, datasetID);
                        slide.setId(slideId);
                        // add image
                        if (slideId != null) {
                            String imageId = datasetClient.addImageToSlide(image, slideId, datasetID);
                            image.setId(imageId);
                            if (imageId != null) {
                                String rawDataId = datasetClient.addRawDataToImage(rawData, imageId, datasetID);
                                rawData.setId(rawDataId);
                                boolean done = false;
                                long timePassed = 0;
                                while (!done) {
                                    if (rawData.getFile() == null) {
                                        TimeUnit.SECONDS.sleep(1);
                                    } else {
                                        TimeUnit.MINUTES.sleep(5);
                                        timePassed += 5;
                                    }
                                    try {
                                        ArrayDataset dataset = datasetClient.getDatasetByLabel(datasetName);
                                        if (dataset.getSlides().get(0).getImages().get(0).getRawDataList().get(0).getStatus() == FutureTaskStatus.DONE ||
                                                dataset.getSlides().get(0).getImages().get(0).getRawDataList().get(0).getStatus() == FutureTaskStatus.ERROR)
                                            done = true;
                                        else 
                                            System.out.println("Raw data is not done yet! Checking in 5 minutes!");
                                        if (timePassed > 30) {
                                            //login again
                                            datasetClient.clearToken();
                                            fileClient.clearToken();
                                            userClient.login(args[0], args[1]);
                                            token = userClient.getToken();
                                            timePassed = 0;
                                        }
                                    } catch (Exception e) {
                                        System.out.println("Failed checking if rawdata is finished: " + dataFolder.getName() + File.separator + experimentName);
                                        System.out.println("Reason" + e.getMessage());
                                        if (e instanceof CustomClientException && ((CustomClientException) e).getBody() != null && ((CustomClientException) e).getBody().contains("EXPIRED")) {
                                            // force login
                                            System.out.println ("rawdata check: token expired, login again!");
                                            datasetClient.clearToken();
                                            fileClient.clearToken();
                                            userClient.login(args[0], args[1]);
                                            token = userClient.getToken();
                                            timePassed = 0;
                                        }
                                    }
                                    
                                }
                                if (rawDataId != null) {
                                    String processedDataId = datasetClient.addProcessedDataToRawData(processedData, rawDataId, datasetID);
                                    processedData.setId(processedDataId);
                                    done = false;
                                    timePassed = 0;
                                    while (!done) {
                                        TimeUnit.MINUTES.sleep(5);
                                        try {
                                            ArrayDataset dataset = datasetClient.getDatasetByLabel(datasetName);
                                            if (dataset.getSlides().get(0).
                                                    getImages().get(0).getRawDataList().get(0).getProcessedDataList().get(0).getStatus() == FutureTaskStatus.DONE ||
                                                    dataset.getSlides().get(0).
                                                    getImages().get(0).getRawDataList().get(0).getProcessedDataList().get(0).getStatus() == FutureTaskStatus.ERROR)
                                                done = true;
                                            else
                                                System.out.println("Processed data is not done yet! Checking in 5 minutes!");
                                            timePassed += 5;
                                            if (timePassed > 30) {
                                                //login again
                                                datasetClient.clearToken();
                                                fileClient.clearToken();
                                                userClient.login(args[0], args[1]);
                                                token = userClient.getToken();
                                                timePassed = 0;
                                            }
                                        } catch (Exception e) {
                                            System.out.println("Failed checking if processeddata is finished: " + dataFolder.getName() + File.separator + experimentName);
                                            System.out.println("Reason" + e.getMessage());
                                            if (e instanceof CustomClientException && ((CustomClientException) e).getBody() != null && ((CustomClientException) e).getBody().contains("EXPIRED")) {
                                                // force login
                                                System.out.println ("processed data check: token expired, login again!");
                                                datasetClient.clearToken();
                                                fileClient.clearToken();
                                                userClient.login(args[0], args[1]);
                                                token = userClient.getToken();
                                                timePassed = 0;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        log.info("Added slide " + slideId + " for " + datasetID + " folder: " + experimentFolder);
                    }
                }
            } catch (CustomClientException e) { 
                if (e.getBody() != null && e.getBody().contains("EXPIRED")) {
                    System.out.println ("token expired, login again!");
                    datasetClient.clearToken();
                    fileClient.clearToken();
                    userClient.login(args[0], args[1]);
                    token = userClient.getToken();
                } else {
                    System.out.println (e.getBody());
                    System.out.println("Failed: " + dataFolder.getName() + File.separator + experimentName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Failed: " + dataFolder.getName() + File.separator + experimentName);
            }
        }
    }
    
    private Experiment getExperimentDetails (String experimentName, String url, String token) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        url = url + "cfg/getSampleData?experimentId=" + experimentName;
        try {
            ResponseEntity<Experiment> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Experiment.class);
            Experiment experiment = response.getBody();
            return experiment;
        } catch (HttpClientErrorException e) {
            throw new CustomClientException(e.getStatusCode(), e.getResponseBodyAsString(), "Error gettting metadata: " + e.getMessage());
        }
    }
    
    private Sample createSample(Experiment experiment, PublicUtilitiyClient utilClient) {   
        SampleData result = experiment.getSampleData();
        Sample sample = new Sample();
        String name = result.getName();
        if (name == null || name.trim().isEmpty()) {
            name = experiment.getSampleName().substring(experiment.getSampleName().lastIndexOf(":")+1, experiment.getSampleName().length());
        }
        sample.setName(name);
        sample.setDescriptors(new ArrayList<Descriptor>());
        sample.setDescriptorGroups(new ArrayList<DescriptorGroup>());
        String template = determineSampleTemplate (result);
        if (template == null) {
            throw new IllegalArgumentException("Sample template could not be determined. Conflicting information");
        }
        sample.setTemplate(template);
        
        MetadataTemplate mt = null;
        if (template.startsWith("Protein")) {
            mt = utilClient.getTemplate ("Metadata6");
            fillInProteinInfo (sample, result, experiment.getSpecies(), mt);
        } else if (template.startsWith("Micro")) {
            mt = utilClient.getTemplate ("Metadata4");
            fillInMicroOrganismInfo (sample, result, mt);
        } else if (template.startsWith("Antibody")) {
            mt = utilClient.getTemplate ("Metadata5");
            fillInAntibodyInfo (sample, result, mt);
        } else if (template.startsWith("Biospecimen")) {
            mt = utilClient.getTemplate ("Metadata2");
            fillInBiospecimenInfo (sample, result, mt);
        } 
        fillInCommonInfo (sample, result, mt);
        return sample;
    }

    private void fillInBiospecimenInfo(Sample sample, SampleData result, MetadataTemplate template) {
        if (result.getSerumSpeciesInfo() != null && !result.getSerumSpeciesInfo().isEmpty()) {
            addDescriptor("Species", result.getSerumCollectionInfo(), sample.getDescriptors(), template);
            addDescriptor("Biospecimen type", "serum", sample.getDescriptors(), template);
        }
    }

    private void fillInAntibodyInfo(Sample sample, SampleData result, MetadataTemplate template) {
        if (result.getSpecies() != null && !result.getSpecies().isEmpty()) {
            addDescriptor ("Host species", result.getSpecies(), sample.getDescriptors(), template);
        }
        if (result.getImmunogen() != null && !result.getImmunogen().isEmpty()) {
            addDescriptor ("Immunogen", result.getImmunogen(), sample.getDescriptors(), template);
        }
        if (result.getEpitopeInfo() != null && !result.getEpitopeInfo().isEmpty()) {
            addDescriptor ("Epitope", result.getEpitopeInfo(), sample.getDescriptors(), template);
        }
        if (result.getAntibodyType() != null && !result.getAntibodyType().isEmpty()) {
            addDescriptor ("Clonality", result.getAntibodyType(), sample.getDescriptors(), template);
        }
        if (result.getAntibodyIsotope() != null && !result.getAntibodyIsotope().isEmpty()) {
            addDescriptor ("Isotype", result.getAntibodyIsotope(), sample.getDescriptors(), template);
        }
        if (result.getSubtype() != null && !result.getSubtype().isEmpty()) {
            addDescriptor ("Subtype", result.getSubtype(), sample.getDescriptors(), template);
        }
        if (result.getAntibodyName() != null && !result.getAntibodyName().isEmpty()) {
            addDescriptor ("Antibody name", result.getAntibodyName(), sample.getDescriptors(), template);
        }
        if (result.getImmunizationSex() != null || result.getImmunizationDoseAndRoute() != null || result.getAdjuvant() != null ||
                result.getAgeOfSubject() != null || result.getImmunizationSchedule() != null) {
            DescriptorGroup immunization = new DescriptorGroup();
            immunization.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Immunization", template);
            immunization.setKey(key2);
            immunization.setName(key2.getName());
            if (result.getAdjuvant() != null) {
                addDescriptor("Adjuvant", result.getAdjuvant(), immunization.getDescriptors(), template);
            }
            if (result.getAgeOfSubject() != null) {
                addDescriptor("Age of subject", result.getAgeOfSubject(), immunization.getDescriptors(), template);
            }
            if (result.getImmunizationSex() != null) {
                addDescriptor("Sex of subject", result.getImmunizationSex(), immunization.getDescriptors(), template);
            }
            if (result.getImmunizationDoseAndRoute() != null) {
                addDescriptor("Dose", result.getImmunizationDoseAndRoute(), immunization.getDescriptors(), template);
            }
            if (result.getImmunizationSchedule() != null) {
                addDescriptor("Immunization schedule", result.getImmunizationSchedule(), immunization.getDescriptors(), template);
            }
            sample.getDescriptorGroups().add(immunization);
        } else {
            // not recorded
            DescriptorGroup immunization = new DescriptorGroup();
            immunization.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Immunization", template);
            immunization.setKey(key2);
            immunization.setName(key2.getName());
            immunization.setNotRecorded(true);
            DescriptorGroup natural = new DescriptorGroup();
            natural.setDescriptors(new ArrayList<Description>());
            key2 = getKeyFromTemplate("Natural immunity", template);
            natural.setKey(key2);
            natural.setName(key2.getName());
            natural.setNotRecorded(true);
            DescriptorGroup expression = new DescriptorGroup();
            expression.setDescriptors(new ArrayList<Description>());
            key2 = getKeyFromTemplate("Expression", template);
            expression.setKey(key2);
            expression.setName(key2.getName());
            expression.setNotRecorded(true);
            sample.getDescriptorGroups().add(immunization);
            sample.getDescriptorGroups().add(natural);
            sample.getDescriptorGroups().add(expression);
        }
    }

    private void fillInMicroOrganismInfo(Sample sample, SampleData result, MetadataTemplate template) {
        if (result.getOrganismCells() != null && !result.getOrganismCells().isEmpty()) {
            DescriptorGroup species = new DescriptorGroup();
            species.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Species", template);
            species.setKey(key2);
            species.setName(key2.getName());
            addDescriptor ("Species name", result.getOrganismCells(), species.getDescriptors(), template);
            sample.getDescriptorGroups().add(species);
        } else {
            System.out.println("No species information is found for " + sample.getName());
        }
    }
    
    private void addDescriptor (String name, String value, List descriptors, MetadataTemplate template) {
        addDescriptor(name, value, false, descriptors, template);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addDescriptor (String name, String value, Boolean notRecorded, List descriptors, MetadataTemplate template) {
        Descriptor desc = new Descriptor();
        desc.setName(name);
        DescriptionTemplate key = getKeyFromTemplate (desc.getName(), template);
        desc.setKey(key);
        desc.setValue(value);
        desc.setNotRecorded(notRecorded);
        descriptors.add(desc);
    }

    private void fillInProteinInfo(Sample sample, SampleData result, String species, MetadataTemplate template) {
        if (result.getSpecSciName() != null && !result.getSpecSciName().trim().isEmpty()) {
            DescriptorGroup spec = new DescriptorGroup();
            spec.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Species", template);
            spec.setKey(key2);
            spec.setName(key2.getName());
            addDescriptor ("Species name", result.getSpecSciName(), spec.getDescriptors(), template);
            sample.getDescriptorGroups().add(spec);
        } else if (species != null && !species.trim().isEmpty()) {
            DescriptorGroup spec = new DescriptorGroup();
            spec.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Species", template);
            spec.setKey(key2);
            spec.setName(key2.getName());
            addDescriptor ("Species name", species, spec.getDescriptors(), template);
            sample.getDescriptorGroups().add(spec);
        } else {
            System.out.println("No species information is found for " + sample.getName());
        }
        if (result.getPrimarySequence() != null && !result.getPrimarySequence().trim().isEmpty()) {
            DescriptorGroup natural = new DescriptorGroup();
            natural.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Natural source", template);
            natural.setKey(key2);
            natural.setName(key2.getName());
            addDescriptor ("AA sequence", result.getPrimarySequence(), natural.getDescriptors(), template);
            sample.getDescriptorGroups().add(natural);
        } else {
            DescriptorGroup natural = new DescriptorGroup();
            natural.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Natural source", template);
            natural.setKey(key2);
            natural.setName(key2.getName());
            natural.setNotRecorded(true);
            DescriptorGroup expression = new DescriptorGroup();
            expression.setDescriptors(new ArrayList<Description>());
            key2 = getKeyFromTemplate("Expression  system", template);
            expression.setKey(key2);
            expression.setName(key2.getName());
            expression.setNotRecorded(true);
            sample.getDescriptorGroups().add(natural);
            sample.getDescriptorGroups().add(expression);
        }
        
        if (result.getCompleteName() != null && !result.getCompleteName().trim().isEmpty()) {
            addDescriptor ("Protein name", result.getCompleteName(), sample.getDescriptors(), template);
        } else {
            // not recorded
            addDescriptor ("Protein name", null, true, sample.getDescriptors(), template);
        }
    }

    private void fillInCommonInfo(Sample sample, SampleData result, MetadataTemplate template) {
        // comment
        StringBuffer commentBuffer = new StringBuffer("");
        if (result.getStorageCondition() != null && !result.getStorageCondition().trim().isEmpty()) {
            commentBuffer.append("Condition of storage:" + result.getStorageCondition() + "\n");
        } 
        if (result.getDateStored() != null && !result.getDateStored().trim().isEmpty()) {
            commentBuffer.append("When was sample placed in storage:" + result.getDateStored() + "\n");
        }
        if (result.getDiluent() != null && !result.getDiluent().trim().isEmpty()) {
            commentBuffer.append("Dilution in what diluent:" + result.getDiluent() + "\n");
        }
        if (result.getOrganismDescription() != null && !result.getOrganismDescription().trim().isEmpty()) {
            commentBuffer.append("Describe the organism by entering information that a person skilled "
                    + "in this field would expect to see to accurately identify the organism:" + result.getOrganismDescription() + "\n");
        }
        if (result.getSubFamily() != null && !result.getSubFamily().isEmpty()) {
            commentBuffer.append("Sub family:" + result.getSubFamily() + "\n");
        }
        if (result.getSerumCollectionInfoType1() != null && !result.getSerumCollectionInfoType1().isEmpty()) {
            commentBuffer.append("Serum collection and processing information type "
                    + "1.Anti-coagulant 2.Dilution in what diluent 3.Condition of storage 4.Time in storage "
                    + "5.Post collection processing (IgG depletion, ammonium sulfate precipitated, etc):" + result.getSerumCollectionInfoType1() + "\n");
        }
        if (result.getVariantOrganismInfo() != null && !result.getVariantOrganismInfo().isEmpty()) {
            commentBuffer.append("Describe variant or mutant organism by entering information "
                    + "that a person skilled in this field would expect to see to accurately identify the organism:" + result.getVariantOrganismInfo() + "\n");
        }
        if (result.getOrganismSpeciesInfo() != null && !result.getOrganismSpeciesInfo().isEmpty()) {
            commentBuffer.append("Species in which your Organism originated (human, mouse, etc) and Scientific Name:" + result.getOrganismSpeciesInfo() + "\n");
        }
        if (result.getSource() != null && !result.getSource().isEmpty()) {
            commentBuffer.append("Source of your material; ie, natural, what expression system, or commercial source:" + result.getSource() + "\n");
        }
        if (result.getContaminants() != null && !result.getContaminants().isEmpty()) {
            commentBuffer.append("Potential contaminants in the sample that could influence binding:" + result.getContaminants() + "\n");
        }
        if (result.getComments() != null && !result.getComments().isEmpty()) {
            commentBuffer.append("Comments:" + result.getComments() + "\n");
        }
        if (result.getMethods() != null && !result.getMethods().isEmpty()) {
            commentBuffer.append("Method(s) by which the sample was tested for activity:" + result.getMethods() + "\n");
        }
        if (result.getExpressionInfo() != null && !result.getExpressionInfo().isEmpty()) {
            commentBuffer.append("If recombinant antibody, describe details of expression; ie construct, expression system, etc:" + result.getExpressionInfo() + "\n");
        }
        if (result.getTestingMethod() != null && !result.getTestingMethod().isEmpty()) {
            commentBuffer.append("Method(s) for testing the activity of the sample:" + result.getTestingMethod() + "\n");
        }
        if (result.getFamily() != null && !result.getFamily().isEmpty()) {
            commentBuffer.append("Family:" + result.getFamily() + "\n");
        }
        if (result.getImmunizationProtocol() != null && !result.getImmunizationProtocol().isEmpty()) {
            commentBuffer.append("Describe immunization protocol used type 1.Immunogen (structure, concentration, etc) "
                    + "2.If conjugated immunogen, what was the carrier protein "
                    + "3.Dose (immunogen and adjuvant) and route of immunization 4.Schedule of immunization (weekly, biweekly, etc)" + 
                    ":" + result.getImmunizationProtocol() + "\n");
        }
        
        addDescriptor ("Comment", commentBuffer.toString().trim(), sample.getDescriptors(), template);
        
        // sample reference
        /*if (result.getReference() != null && !result.getReference().isEmpty()) {
            DescriptorGroup reference = new DescriptorGroup();
            reference.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Sample reference", template);
            reference.setKey(key2);
            reference.setName(key2.getName());
            Descriptor value = new Descriptor();
            value.setName("Value");
            DescriptionTemplate key6 = getKeyFromTemplate ("Value", template);
            value.setKey(key6);
            String referenceValue = result.getReference();
            //TODO how to parse
            value.setValue(referenceValue);
            if (value.getValue() != null) {
                Descriptor sub1Desc4 = new Descriptor();
                sub1Desc4.setName("Type");
                sub1Desc4.setValue("PMID");   // TODO what should be the type??? 
                reference.getDescriptors().add(value);
                reference.getDescriptors().add(sub1Desc4);
                sample.getDescriptorGroups().add(reference);
            }
        }*/
        
        // database entry - Genbank
        if (result.getGenBank() != null && !result.getGenBank().trim().isEmpty()) {
            DescriptorGroup genbankEntry = new DescriptorGroup();
            genbankEntry.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Database entry", template);
            genbankEntry.setKey(key2);
            genbankEntry.setName(key2.getName());
            addDescriptor ("Database", "GenBank", genbankEntry.getDescriptors(), template);
            addDescriptor ("Database URL", "https://www.ncbi.nlm.nih.gov/genbank/", genbankEntry.getDescriptors(), template);
            addDescriptor ("Id", result.getGenBank(), genbankEntry.getDescriptors(), template);
            sample.getDescriptorGroups().add(genbankEntry);
        }
        
        // database entry - Swis_prot
        if (result.getSwissProt() != null && !result.getSwissProt().trim().isEmpty()) {
            DescriptorGroup genbankEntry = new DescriptorGroup();
            genbankEntry.setDescriptors(new ArrayList<Description>());
            DescriptionTemplate key2 = getKeyFromTemplate("Database entry", template);
            genbankEntry.setKey(key2);
            genbankEntry.setName(key2.getName());
            
            addDescriptor ("Database", "UniProt", genbankEntry.getDescriptors(), template);
            addDescriptor ("Database URL", "https://www.uniprot.org/uniprot/", genbankEntry.getDescriptors(), template);
            String val = result.getSwissProt();
            if (val.contains(":"))
                val = val.substring(val.lastIndexOf(":")+1);
            addDescriptor ("Id", val, genbankEntry.getDescriptors(), template);
            sample.getDescriptorGroups().add(genbankEntry);
        }
        
        DescriptionTemplate descT = getKeyFromTemplate("Commercial source", template);
        DescriptorGroup group = new DescriptorGroup();
        group.setKey(descT);
        group.setNotRecorded(true);
        DescriptionTemplate descT2 = getKeyFromTemplate("Non-commercial", template);
        DescriptorGroup group2 = new DescriptorGroup();
        group2.setKey(descT2);
        group2.setNotRecorded(true);
        sample.getDescriptorGroups().add(group);
        sample.getDescriptorGroups().add(group2);
    }
    
    public static DescriptionTemplate getKeyFromTemplate(String descriptorName, MetadataTemplate metadataTemplate) {
        DescriptionTemplate template = null;
        if (metadataTemplate != null) {
            for (DescriptionTemplate desc: metadataTemplate.getDescriptors()) {
                if (desc.getName().equalsIgnoreCase(descriptorName)) 
                    template = desc;
                if (template == null && desc instanceof DescriptorGroupTemplate) {
                    template =  getKeyFromTemplate ((DescriptorGroupTemplate)desc, descriptorName);
                }
            }
        }
        return template;
    }
    
    public static DescriptionTemplate getKeyFromTemplate(DescriptorGroupTemplate group, String descriptorName) {
        DescriptionTemplate template = null;
        for (DescriptionTemplate desc: group.getDescriptors()) {
            if (desc.getName().equalsIgnoreCase(descriptorName)) 
                template = desc;
            if (template == null && desc instanceof DescriptorGroupTemplate) {
                template = getKeyFromTemplate ((DescriptorGroupTemplate)desc, descriptorName);
            }
        }   
        return template;
    }

    private String determineSampleTemplate(SampleData result) {
        String template = null;
        if (result.getOrganismCells() != null && !result.getOrganismCells().isEmpty())
            template = "Micro-Organism Sample";
        else if (result.getSerumSpeciesInfo() != null && !result.getSerumSpeciesInfo().isEmpty())
            template = "Biospecimen Sample";
        if ((result.getAntibodyName() != null && !result.getAntibodyName().isEmpty()) ||
                (result.getAntibodyType() != null && !result.getAntibodyType().isEmpty()) ||
                (result.getSubtype() != null && !result.getSubtype().isEmpty())) {
            if (template == null) 
                template = "Antibody Sample";
            else {
                return null;
            }
        }
        
        if ((result.getPrimarySequence() != null && !result.getPrimarySequence().isEmpty()) ||
                (result.getSpecSciName() != null && !result.getSpecSciName().isEmpty()) ||
                (result.getCompleteName() != null && !result.getCompleteName().isEmpty())) {
            if (template == null) 
                template = "Protein Sample";
        }
        
        return template;
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
