package com.kmutt.sit.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kmutt.sit.batch.tasks.EnvironmentCreation;
import com.kmutt.sit.batch.tasks.ExistingEvaluation;
import com.kmutt.sit.batch.tasks.LogisticsOptimization;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
	
	private static Logger logger = LoggerFactory.getLogger(BatchConfiguration.class);
	
    @Autowired
    private JobBuilderFactory jobs;
 
    @Autowired
    private StepBuilderFactory steps;
    
    @Autowired
    private EnvironmentCreation environmentCreation;
    
    @Autowired
    private LogisticsOptimization logisticsOptimization;
    
    @Autowired
    private ExistingEvaluation existingEvaluation;
     
    @Bean
    public Job processJob(){
    	logger.info("processJob(): ...");
    	
        return jobs.get("processJob")
                .incrementer(new RunIdIncrementer())
                .start(createApplicationEnvironment())
                .next(evaluateExistingSolution())
                .next(optimizeShipmentLogistics())
                .build();
    }
    
    @Bean
    public Step createApplicationEnvironment(){    	
        return steps.get("createApplicationEnvironment")
                .tasklet(environmentCreation)
                .build();
    }    
    
    @Bean
    public Step evaluateExistingSolution(){    	
        return steps.get("evaluateExistingSolution")
                .tasklet(existingEvaluation)
                .build();
    }    

    @Bean
    public Step optimizeShipmentLogistics(){    	
        return steps.get("optimizeShipmentLogistics")
                .tasklet(logisticsOptimization)
                .build();
    }
}
