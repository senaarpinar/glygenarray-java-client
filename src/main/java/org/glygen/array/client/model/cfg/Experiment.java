package org.glygen.array.client.model.cfg;

import java.util.Date;
import java.util.List;

public class Experiment {
    Long id;
    String primScreen;
    String sampleName;
    String species;
    String proteinFamily;
    String investigator;
    String request;
    Date date;
    SampleData sampleData;
    Long experimentDataId;
    Long requestDataId;
    String filename;
    
    List<Link> links;
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @return the primScreen
     */
    public String getPrimScreen() {
        return primScreen;
    }
    /**
     * @param primScreen the primScreen to set
     */
    public void setPrimScreen(String primScreen) {
        this.primScreen = primScreen;
    }
    /**
     * @return the sampleName
     */
    public String getSampleName() {
        return sampleName;
    }
    /**
     * @param sampleName the sampleName to set
     */
    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }
    /**
     * @return the species
     */
    public String getSpecies() {
        return species;
    }
    /**
     * @param species the species to set
     */
    public void setSpecies(String species) {
        this.species = species;
    }
    /**
     * @return the proteinFamily
     */
    public String getProteinFamily() {
        return proteinFamily;
    }
    /**
     * @param proteinFamily the proteinFamily to set
     */
    public void setProteinFamily(String proteinFamily) {
        this.proteinFamily = proteinFamily;
    }
    /**
     * @return the investigator
     */
    public String getInvestigator() {
        return investigator;
    }
    /**
     * @param investigator the investigator to set
     */
    public void setInvestigator(String investigator) {
        this.investigator = investigator;
    }
    /**
     * @return the request
     */
    public String getRequest() {
        return request;
    }
    /**
     * @param request the request to set
     */
    public void setRequest(String request) {
        this.request = request;
    }
    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }
    /**
     * @return the sampleData
     */
    public SampleData getSampleData() {
        return sampleData;
    }
    /**
     * @param sampleData the sampleData to set
     */
    public void setSampleData(SampleData sampleData) {
        this.sampleData = sampleData;
    }
    /**
     * @return the experimentDataId
     */
    public Long getExperimentDataId() {
        return experimentDataId;
    }
    /**
     * @param experimentDataId the experimentDataId to set
     */
    public void setExperimentDataId(Long experimentDataId) {
        this.experimentDataId = experimentDataId;
    }
    /**
     * @return the requestDataId
     */
    public Long getRequestDataId() {
        return requestDataId;
    }
    /**
     * @param requestDataId the requestDataId to set
     */
    public void setRequestDataId(Long requestDataId) {
        this.requestDataId = requestDataId;
    }
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }
    /**
     * @return the links
     */
    public List<Link> getLinks() {
        return links;
    }
    /**
     * @param links the links to set
     */
    public void setLinks(List<Link> links) {
        this.links = links;
    }
    

}
