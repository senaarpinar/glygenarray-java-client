package org.glygen.array.client.model.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.glygen.array.client.model.metadata.AssayMetadata;

@XmlRootElement
public class Slide {
    
    String id;
    String uri;
    PrintedSlide printedSlide;
    List<Image> images;
    AssayMetadata metadata;
    List<String> blocksUsed;  // list of block ids used from this slide in the experiment
    
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
     * @return the images
     */
    public List<Image> getImages() {
        return images;
    }
    /**
     * @param image the images to set
     */
    public void setImages(List<Image> image) {
        this.images = image;
    }
    /**
     * @return the printedSlide
     */
    public PrintedSlide getPrintedSlide() {
        return printedSlide;
    }
    /**
     * @param printedSlide the printedSlide to set
     */
    public void setPrintedSlide(PrintedSlide printedSlide) {
        this.printedSlide = printedSlide;
    }
    /**
     * @return the metadata
     */
    public AssayMetadata getMetadata() {
        return metadata;
    }
    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(AssayMetadata metadata) {
        this.metadata = metadata;
    }
    /**
     * @return the blocksUsed
     */
    public List<String> getBlocksUsed() {
        return blocksUsed;
    }
    /**
     * @param blocksUsed the blocksUsed to set
     */
    public void setBlocksUsed(List<String> blocksUsed) {
        this.blocksUsed = blocksUsed;
    }
}
