package org.glygen.array.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glygen.array.client.exception.CustomClientException;
import org.glygen.array.client.model.BlockLayout;
import org.glygen.array.client.model.Confirmation;
import org.glygen.array.client.model.Feature;
import org.glygen.array.client.model.FeatureType;
import org.glygen.array.client.model.Glycan;
import org.glygen.array.client.model.GlycanSequenceFormat;
import org.glygen.array.client.model.GlycanType;
import org.glygen.array.client.model.ImportGRITSLibraryResult;
import org.glygen.array.client.model.LibraryImportInput;
import org.glygen.array.client.model.Linker;
import org.glygen.array.client.model.LinkerClassification;
import org.glygen.array.client.model.LoginRequest;
import org.glygen.array.client.model.SequenceDefinedGlycan;
import org.glygen.array.client.model.SlideLayout;
import org.glygen.array.client.model.SmallMoleculeLinker;
import org.glygen.array.client.model.UnknownGlycan;
import org.glygen.array.client.model.data.FileWrapper;
import org.grits.toolbox.glycanarray.library.om.ArrayDesignLibrary;
import org.grits.toolbox.glycanarray.library.om.LibraryInterface;
import org.grits.toolbox.glycanarray.library.om.feature.GlycanProbe;
import org.grits.toolbox.glycanarray.library.om.feature.Ratio;
import org.grits.toolbox.glycanarray.library.om.layout.Block;
import org.grits.toolbox.glycanarray.library.om.layout.Spot;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class GlycanRestClientImpl implements GlycanRestClient {
	
	private RestTemplate restTemplate = new RestTemplate();
	
	String token=null;
	String username;
	String password;
	String url = "http://localhost:8080/";
	
	List<String> duplicates = new ArrayList<String>();
	List<String> empty = new ArrayList<String>();
	
	Map <String, Feature> featureCache = new HashMap<String, Feature>();
	
	@Override
    public void clearToken () {
        this.token = null;
    }
	
	/**
     * {@inheritDoc}
     */
	@Override
	public String addGlycan(Glycan glycan) {
		if (token == null) login(this.username, this.password);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
	    HttpEntity<Glycan> requestEntity = new HttpEntity<Glycan>(glycan, headers);
	    String url = this.url + "array/addglycan";
	    url += "?noGlytoucanRegistration=true";
		System.out.println("URL: " + url);
		
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			System.out.println("Exception adding glycan " + ((HttpClientErrorException) e).getResponseBodyAsString());
			if (e.getResponseBodyAsString().contains("Duplicate") && e.getResponseBodyAsString().contains("sequence")) {
				duplicates.add(glycan.getName());
				if (glycan.getType() == GlycanType.SEQUENCE_DEFINED) {
					try {
						url = this.url + "array/getGlycanBySequence?sequence=" 
								+ URLEncoder.encode(((SequenceDefinedGlycan)glycan).getSequence(), StandardCharsets.UTF_8.name());
						HttpEntity<Map<String, String>> requestEntity1 = new HttpEntity<>(headers);
						ResponseEntity<String> glycanId = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity1, String.class);
						if (glycanId.getBody() != null) {
						   // headers.setAccept(Arrays.asList(MediaType.TEXT_PLAIN));
							HttpEntity<String> requestEntity2 = new HttpEntity<String>(glycan.getName(), headers);
							// add as alias to the existing one
							url = this.url + "array/addAlias/" + glycanId.getBody();
							try {
								ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity2, String.class);
								System.out.println ("added alias " + glycan.getName());
								return response.getBody();
							} catch (HttpClientErrorException ex) {
								System.out.println ("Could not add alias for " + glycan.getName());
							}
						}
					} catch (UnsupportedEncodingException e1) {
						System.out.println ("Error encoding the sequence: " + e1.getMessage());
					}
				}
			}
			if (e.getResponseBodyAsString().contains("NoEmpty") && e.getResponseBodyAsString().contains("sequence")) {
				empty.add(glycan.getName());
			}
		}
		return null;
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
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public String addBlockLayout(BlockLayout layout) {
		if (token == null) login(this.username, this.password);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
	    HttpEntity<BlockLayout> requestEntity = new HttpEntity<BlockLayout>(layout, headers);
		String url = this.url + "array/addblocklayout";
		System.out.println("URL: " + url);
		ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
		return response.getBody();
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public String addLinker(Linker linker) {
		if (token == null) login(this.username, this.password);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
	    HttpEntity<Linker> requestEntity = new HttpEntity<Linker>(linker, headers);
		String url = this.url + "array/addlinker";
		System.out.println("URL: " + url);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			return response.getBody();
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			if (errorMessage.contains("Duplicate") && !errorMessage.contains("pubChemId") 
					&& errorMessage.contains("name")) {
				linker.setName(linker.getName()+"B");
				requestEntity = new HttpEntity<Linker>(linker, headers);
				ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
				return response.getBody();
			}
		}
		return null;	
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public List<LinkerClassification> getLinkerClassifications() {
		String url = this.url + "util/getLinkerClassifications";
		System.out.println("URL: " + url);
		try {
			ObjectMapper mapper = new ObjectMapper();
			ResponseEntity<List> response = this.restTemplate.exchange(url, HttpMethod.GET, null, List.class);
			List list = response.getBody();
			List<LinkerClassification> classifications = mapper.convertValue(list, new TypeReference<List<LinkerClassification>>() { });
			return classifications;
		} catch (HttpClientErrorException e) {
			System.out.println ("Error gettting classification list: " + e.getMessage());
		}
		return null;	
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public String addSlideLayout(SlideLayout layout) {
		if (token == null) login(this.username, this.password);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
	    HttpEntity<SlideLayout> requestEntity = new HttpEntity<SlideLayout>(layout, headers);
		String url = this.url + "array/addslidelayout";
		System.out.println("URL: " + url);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			return response.getBody();
		} catch (HttpServerErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			if (errorMessage != null) {
				System.out.println("server error: " + errorMessage);
			}
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			if (errorMessage != null) {
				System.out.println("client error: " + errorMessage);
			}
		}
		
		return null;
	}
	
	public List<String> getDuplicates() {
		return duplicates;
	}
	
	public List<String> getEmpty() {
		return empty;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public void setURL(String url) {
		this.url = url;
	}

	/**
     * {@inheritDoc}
     */
	@Override
	public String addFeature(Feature feature) {
	    System.out.println ("Trying to add " + feature.getName());
	    if (featureCache.get(feature.getName()) != null) {
	        System.out.println("Feature " + feature.getName() + " has already been added");
	        return featureCache.get(feature.getName()).getId();
	    }
		if (token == null) login(this.username, this.password);
		//set the header with token
		HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	    headers.add("Authorization", token);
	    HttpEntity<Feature> requestEntity = new HttpEntity<Feature>(feature, headers);
		String url = this.url + "array/addfeature";
		System.out.println("URL: " + url);
		try {
			ResponseEntity<String> response = this.restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
			feature.setId(response.getBody());
			featureCache.put(feature.getName(), feature);
			return response.getBody();
		} catch (HttpServerErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			if (errorMessage != null) {
				System.out.println("server error: " + errorMessage);
				System.out.println("Cannot add feature:" + feature.getName());
			} 
		} catch (HttpClientErrorException e) {
			String errorMessage = e.getResponseBodyAsString();
			if (errorMessage != null) {
			    if (errorMessage.contains("Duplicate")) {
			        featureCache.put(feature.getName(), feature);
			    }
				System.out.println("client error: " + errorMessage);
				System.out.println("Cannot add feature:" + feature.getName());
			}
		}
		
		return null;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public ImportGRITSLibraryResult addFromLibrary(FileWrapper file) {
        ImportGRITSLibraryResult result = new ImportGRITSLibraryResult();
        if (token == null) login(this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        String url = this.url + "array/getSlideLayoutFromLibrary?file=" + file.getIdentifier();
        String addUrl = this.url + "array/addSlideLayoutFromLibrary";
        try {
            // get the list of slide layouts in the file
            HttpEntity<Map<String, String>> requestEntity1 = new HttpEntity<>(headers);
            ResponseEntity<SlideLayout[]> response = this.restTemplate.exchange(url, HttpMethod.GET, requestEntity1, SlideLayout[].class);
            //List layoutList = response.getBody();
            List<SlideLayout> returnedLayouts = Arrays.asList(response.getBody());
            for (SlideLayout layout: returnedLayouts) {
               // SlideLayout layout = (SlideLayout)item;
                // add the slide layout
                LibraryImportInput input = new LibraryImportInput();
                input.setFile(file);
                input.setSlideLayout(layout);
                HttpEntity<LibraryImportInput> requestEntity = new HttpEntity<LibraryImportInput>(input, headers);
                try {
                    ResponseEntity<String> response2 = this.restTemplate.exchange(addUrl, HttpMethod.POST, requestEntity, String.class);
                    String id = response2.getBody();
                    layout.setId(id);
                    result.getAddedLayouts().add(layout);
                } catch (HttpServerErrorException e) {
                    //TODO catch the ErrorMessage body and check for duplicate error
                    String errorMessage = e.getResponseBodyAsString();
                    if (errorMessage != null) {
                        System.out.println("server error: " + errorMessage);
                    } 
                    result.getErrors().add(layout);
                } catch (HttpClientErrorException e) {
                    String errorMessage = e.getResponseBodyAsString();
                    if (errorMessage != null) {
                        System.out.println("client error: " + errorMessage);
                    }
                    result.getErrors().add(layout);
                }
            }            
        } catch (HttpServerErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            if (errorMessage != null) {
                System.out.println("server error: " + errorMessage);
            } 
        } catch (HttpClientErrorException e) {
            String errorMessage = e.getResponseBodyAsString();
            if (errorMessage != null) {
                System.out.println("client error: " + errorMessage);
            }
        }
        
        return result;  
    }

    @Override
    public Confirmation deleteFeature(Feature feature) {
        if (token == null) login(this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        String url = this.url + "/deletefeature/" + feature.getId();
        ResponseEntity<Confirmation> response = this.restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Confirmation.class);
        return response.getBody(); 
    }

    @Override
    public Confirmation deleteLinker(Linker linker) {
        if (token == null) login(this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        String url = this.url + "/deletelinker/" + linker.getId();
        ResponseEntity<Confirmation> response = this.restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Confirmation.class);
        return response.getBody(); 
        
    }

    @Override
    public Confirmation deleteGlycan(Glycan glycan) {
        if (token == null) login(this.username, this.password);
        //set the header with token
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", token);
        HttpEntity<Void> requestEntity = new HttpEntity<Void>(null, headers);
        String url = this.url + "/delete/" + glycan.getId();
        ResponseEntity<Confirmation> response = this.restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, Confirmation.class);
        return response.getBody(); 
        
    }
}
