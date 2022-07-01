package org.glygen.array.client;

import java.util.Date;

import org.glygen.array.client.model.data.ArrayDataset;
import org.glygen.array.client.model.data.Image;
import org.glygen.array.client.model.data.PrintedSlide;
import org.glygen.array.client.model.data.ProcessedData;
import org.glygen.array.client.model.data.RawData;
import org.glygen.array.client.model.data.Slide;
import org.glygen.array.client.model.metadata.AssayMetadata;
import org.glygen.array.client.model.metadata.DataProcessingSoftware;
import org.glygen.array.client.model.metadata.ImageAnalysisSoftware;
import org.glygen.array.client.model.metadata.Printer;
import org.glygen.array.client.model.metadata.Sample;
import org.glygen.array.client.model.metadata.ScannerMetadata;
import org.glygen.array.client.model.metadata.SlideMetadata;
import org.glygen.array.client.model.metadata.SpotMetadata;

public interface DatasetRestClient {
    /**
     * sets the user name of the user
     * @param username name of the user
     */
    void setUsername(String username);
    
    /**
     * sets the password to be used for the user
     * @param password password of the user
     */
    void setPassword(String password);
    
    /**
     * sets the url for the web services, e.g https://glygen.ccrc.uga.edu/ggarray/api/
     * @param url url to be used to access the web services (with the trailing /)
     */
    void setURL (String url);
    
    /**
     * adds the given array dataset to the repository
     * 
     * @param datasetName name of the experiment/dataset
     * @param description description of the experiment/dataset
     * @param sample Sample used in the experiment, Sample should already be in the repository
     * @param date date when experiment is created
     * Either id or name should be specified for the sample, the other fields might be left empty/null
     * @return the id of the newly added array dataset
     */
    String addDataset(String experimentName, String description, Sample sample, Date date);
    
    /**
     * add the given slide to the dataset
     * 
     * @param slide slide to be added. The slide should contain one or more images. An image should be associated with a raw data
     * and the rawdata should have a list of processed data. All the metadata should already be in the repository. In addition, all the files
     * refered by the image/rawdata/processeddata should already be uploaded to the server
     * @param datasetId id of the dataset to add to (dataset should already be in the repository)
     * @return the id of the newly added slide
     */
    String addSlideToDataset (Slide slide, String datasetId);
    
    /**
     * add the given sample to the repository
     * @param sample Sample with all the required metadata
     * @return the id of the newly added sample
     */
    String addSample(Sample sample);
    
    /**
     * add the given printer to the repository
     * @param printer Printer with all the required metadata
     * @return the id of the newly added printer
     */
    String addPrinterMetadata (Printer printer);
    
    /**
     * add the given printer to the repository
     * @param printer Printer with all the required metadata
     * @param validate true if printer metadata should be validated or false to bypass validation (should only be done for legacy data)
     * @return the id of the newly added printer
     */
    String addPrinterMetadata (Printer printer, boolean validate);
    
    /**
     * 
     * @param scanner
     * @return
     */
    String addScannerMetadata (ScannerMetadata scanner);
    
    String addSlideMetadata (SlideMetadata slideMetadata);
    
    String addImageAnalysisMetadata (ImageAnalysisSoftware metadata);
    
    String addDataProcessingMetadata (DataProcessingSoftware metadata); 
    
    String addAssayMetadata (AssayMetadata metadata); 
    
    String addSpotMetadata (SpotMetadata metadata); 

    /**
     * add the given printed slide to the repository
     * @param printedSlide printed slide with name, description, 
     * name/id/uri for an already created slide metadata, name/id/uri for an already created printer metadata, 
     * name/id/uri for an already created slide layout should all be specified (only one of name, id or uri is sufficient)
     * @return the id of the newly added printed slide
     */
    String addPrintedSlide (PrintedSlide printedSlide);
    
    /**
     * return the id of the data processing software metadata with the given name, if exists
     * @param name name of the metadata
     * @return the id of the metadata or null if it does not exists
     */
    String getDataProcessingMetadataByLabel (String name);
    String getAssayMetadataByLabel (String name);
    String getSpotMetadataByLabel (String name);
    String getImageAnalysisMetadataByLabel (String name);
    String getSlideMetadataByLabel (String name);
    String getScannerByLabel (String name);
    String getPrinterByLabel (String name);
    String getSampleByLabel (String name);
    
    /**
     * retrieve the array dataset given its name
     * @param name name of the dataset to be retrieved
     * @return the array dataset of null if not found
     */
    ArrayDataset getDatasetByLabel (String name);

    String addImageToSlide(Image image, String slideId, String datasetId);
    String addRawDataToImage(RawData rawData, String imageId, String datasetId);
    String addProcessedDataToRawData(ProcessedData processedData, String rawDataId, String datasetId);

    void clearToken();
    
    /**
     * return slide given its id
     * 
     * @param id id of the slide to be retrieved
     * @return the slide with the given id, or null if it does not exists
     */
    //Slide getSlideById (String id);
    
}
