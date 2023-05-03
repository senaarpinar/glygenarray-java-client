package org.glygen.array.client;

import java.util.List;

import org.glygen.array.client.model.BlockLayout;
import org.glygen.array.client.model.Confirmation;
import org.glygen.array.client.model.Feature;
import org.glygen.array.client.model.Glycan;
import org.glygen.array.client.model.ImportGRITSLibraryResult;
import org.glygen.array.client.model.Linker;
import org.glygen.array.client.model.LinkerClassification;
import org.glygen.array.client.model.SlideLayout;
import org.glygen.array.client.model.data.FileWrapper;

public interface GlycanRestClient {
	
	public static final String uriPrefix = "http://glygen.org/glygenarray/";
	
	/**
	 * adds the given glycan to the repository for the given user
	 * @param glycan glycan to add
	 * @return the identifier of the newly added glycan in the repository
	 */
	String addGlycan (Glycan glycan);
	/**
	 * adds the given linker to the repository for the given user
	 * @param linker linker to add
     * @return the identifier of the newly added linker in the repository
     */
	String addLinker (Linker linker);
	
	/**
	 * adds the given feature to the repository for the given user
	 * @param feature feature to add
	 * @return the identifier of the newly added feature in the repository
	 */
	String addFeature (Feature feature);
	
	Confirmation deleteFeature (Feature feature);
	Confirmation deleteLinker (Linker linker);
	Confirmation deleteGlycan (Glycan glycan);
	
	/**
	 * adds the given block layout to the repository for the given user
	 * @param layout BlockLayout to add
	 * @return the identifier of the newly added block layout in the repository
	 */
	String addBlockLayout (BlockLayout layout);
	
	/**
	 * adds the given slide layout to the repository for the given user
	 * @param layout SlideLayout to add
	 * @return the identifier of the newly added slide layout in the repository
	 */
	String addSlideLayout (SlideLayout layout);
	
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
	 * retrieve all the linker classifications from the repository
	 * @return a list of linker classifications
	 */
	List<LinkerClassification> getLinkerClassifications();
	
	/** 
	 * 
	 * @param file library file to get slide layouts
	 * @return a result object with the added slide layouts, errors and duplicates
	 */
    ImportGRITSLibraryResult addFromLibrary(FileWrapper file);
    
    void clearToken();
}
