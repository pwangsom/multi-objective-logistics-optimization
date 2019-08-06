package com.kmutt.sit.environment;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Setter;

@Component
public class ApplicationEnvironmentCreator {

	private static Logger logger = LoggerFactory.getLogger(ApplicationEnvironmentCreator.class);
	
    @Value("${app.output.path}")
    private String outputPath;

	@Setter
	private String jobId;
	
	public void create() {
        logger.info("ApplicationEnvironmentCreator: Job ID: " + jobId + "\t start.....");
        
        createOutputFolder();

        logger.info("ApplicationEnvironmentCreator: Job ID: " + jobId + "\t finished.."); 		
	}
	
	private void createOutputFolder() {
        logger.debug("preparing output folder: " + outputPath);
        
		File folder = new File(outputPath);
		
		if (!folder.isDirectory()) {	
			folder.mkdir();			
	        logger.debug("Output path was not exist, but it is just crated");
		}
	}

}
