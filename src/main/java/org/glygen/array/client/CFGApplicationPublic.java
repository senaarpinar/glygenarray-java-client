package org.glygen.array.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.glygen.array.client.CFGApplicationPublic.GlygenSettings;
import org.glygen.array.client.exception.CustomClientException;
import org.glygen.array.client.model.ArrayDatasetListView;
import org.glygen.array.client.model.User;
import org.glygen.array.client.model.data.ArrayDataset;
import org.glygen.array.client.model.data.FutureTaskStatus;
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
public class CFGApplicationPublic implements CommandLineRunner {

    private static final Logger log = (Logger) LoggerFactory.getLogger(Application.class);
    
    SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    
    @Bean
    @ConfigurationProperties("glygen")
    public GlygenSettings glygen2() {
        return new GlygenSettings();
    }

    public static void main(String args[]) {
        new SpringApplicationBuilder(CFGApplicationPublic.class).run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        GlygenSettings settings = glygen2();
        String url = settings.scheme + settings.host + settings.basePath;
        UserRestClient userClient = new UserRestClientImpl();
        userClient.setURL(url);
        
        if (args == null || args.length < 2) {
            log.error("need to pass username and password");
            return;
        }
        userClient.login(args[0], args[1]);
        User user = userClient.getUser(args[0]);
        log.info("got user information:" + user.getEmail());
        
        DatasetRestClient datasetClient = new DatasetRestClientImpl();
        datasetClient.setURL(url);
        datasetClient.setUsername(args[0]);
        datasetClient.setPassword(args[1]);
        
        
        ArrayDatasetListView datasets = datasetClient.getDatasets(0, 10);
        int total = datasets.getTotal();
        int i = 0;
        while (i < total) {
            for (ArrayDataset dataset: datasets.getRows()) {
                System.out.println("Making " + dataset.getName() + " public");
                try {
                    if (dataset.getIsPublic()) {
                        System.out.println("Already public. skipping!");
                        continue;
                    }
                    datasetClient.makeDatasetPublic(dataset.getId());
                    
                    boolean done = false;
                    long timePassed = 0;
                    while (!done) {
                        TimeUnit.MINUTES.sleep(1);
                        timePassed += 1;
                        try {
                            ArrayDataset d = datasetClient.getDatasetByLabel(dataset.getName());
                            if (d.getStatus() == FutureTaskStatus.DONE || d.getStatus() == FutureTaskStatus.ERROR)
                                done = true;
                            else 
                                System.out.println("making public is not done yet! Checking in 1 minutes!");
                            if (timePassed > 30) {
                                //login again
                                datasetClient.clearToken();
                                userClient.login(args[0], args[1]);
                                timePassed = 0;
                            }
                        } catch (Exception e) {
                            System.out.println("Failed checking if making public is finished: " + dataset.getName());
                            System.out.println("Reason" + e.getMessage());
                            if (e instanceof CustomClientException && ((CustomClientException) e).getBody() != null && ((CustomClientException) e).getBody().contains("EXPIRED")) {
                                // force login
                                System.out.println ("making public check: token expired, login again!");
                                datasetClient.clearToken();
                                userClient.login(args[0], args[1]);
                                timePassed = 0;
                            } else {
                            	// error! skip it
                            	System.out.println("Skipping!");
                            	done = true;
                            }
                        }
                        
                    }
                    System.out.println ("finished making " + dataset.getName() + " public!");
                } catch (CustomClientException e) { 
                    if (e.getBody() != null && e.getBody().contains("EXPIRED")) {
                        System.out.println ("token expired, login again!");
                        datasetClient.clearToken();
                        userClient.login(args[0], args[1]);
                    } else {
                        System.out.println (e.getBody());
                        System.out.println("Failed: " + dataset.getName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed: " + dataset.getName());
                }
            }
            System.out.println ("Getting next bunch");
            i += 10;
            datasets = datasetClient.getDatasets(i, 10);
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