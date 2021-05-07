package org.glygen.array.client.model.data;

public class Grant {
    
    String id;
    String uri;
    String title;
    String identifier;
    String URL;
    String fundingOrganization;
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    /**
     * @return the uRL
     */
    public String getURL() {
        return URL;
    }
    /**
     * @param uRL the uRL to set
     */
    public void setURL(String uRL) {
        URL = uRL;
    }
    /**
     * @return the fundingOrganization
     */
    public String getFundingOrganization() {
        return fundingOrganization;
    }
    /**
     * @param fundingOrganization the fundingOrganization to set
     */
    public void setFundingOrganization(String fundingOrganization) {
        this.fundingOrganization = fundingOrganization;
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

}
