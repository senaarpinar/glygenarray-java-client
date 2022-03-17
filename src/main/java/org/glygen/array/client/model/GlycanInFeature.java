package org.glygen.array.client.model;

import java.util.List;

public class GlycanInFeature  {
    Glycan glycan;
    Source source;
    ReducingEndConfiguration reducingEndConfiguration;
    List<Publication> publications;
    List<String> urls;
    
    /**
     * @return the source
     */
    public Source getSource() {
        return source;
    }
    /**
     * @param source the source to set
     */
    public void setSource(Source source) {
        this.source = source;
    }
    /**
     * @return the reducingEndConfiguration
     */
    public ReducingEndConfiguration getReducingEndConfiguration() {
        return reducingEndConfiguration;
    }
    /**
     * @param reducingEndConfiguration the reducingEndConfiguration to set
     */
    public void setReducingEndConfiguration(ReducingEndConfiguration ringInfo) {
        this.reducingEndConfiguration = ringInfo;
    }

    /**
     * @return the glycan
     */
    public Glycan getGlycan() {
        return glycan;
    }
    /**
     * @param glycan the glycan to set
     */
    public void setGlycan(Glycan glycan) {
        this.glycan = glycan;
    }
    /**
     * @return the publications
     */
    public List<Publication> getPublications() {
        return publications;
    }
    /**
     * @param publications the publications to set
     */
    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }
    /**
     * @return the urls
     */
    public List<String> getUrls() {
        return urls;
    }
    /**
     * @param urls the urls to set
     */
    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

}
