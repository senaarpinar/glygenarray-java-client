package org.glygen.array.client.model.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.glygen.array.client.model.metadata.ScannerMetadata;

@XmlRootElement
public class Image {
    
    String id;
    String uri;
    FileWrapper file;
    ScannerMetadata scanner; 
    String description;
    List<RawData> rawDataList;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
    /**
     * @return the file
     */
    public FileWrapper getFile() {
        return file;
    }
    /**
     * @param file the file to set
     */
    public void setFile(FileWrapper file) {
        this.file = file;
    }
    /**
     * @return the scanner
     */
    public ScannerMetadata getScanner() {
        return scanner;
    }
    /**
     * @param scanner the scanner to set
     */
    public void setScanner(ScannerMetadata scanner) {
        this.scanner = scanner;
    }
    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @param rawData the rawData to set
     */
    public void addRawData(RawData rawData) {
        if (this.rawDataList == null) {
            this.rawDataList = new ArrayList<RawData>();
        }
        this.rawDataList.add(rawData);
    }
    /**
     * @return the rawDataList
     */
    public List<RawData> getRawDataList() {
        return rawDataList;
    }
    /**
     * @param rawDataList the rawDataList to set
     */
    public void setRawDataList(List<RawData> rawDataList) {
        this.rawDataList = rawDataList;
    }

}
