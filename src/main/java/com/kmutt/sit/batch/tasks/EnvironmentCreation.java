package com.kmutt.sit.batch.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kmutt.sit.environment.ApplicationEnvironmentCreator;

@Component
public class EnvironmentCreation implements Tasklet  {
	
	private static Logger logger = LoggerFactory.getLogger(EnvironmentCreation.class);
	
	@Autowired
	private ApplicationEnvironmentCreator environmentCreator;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
        logger.info("EnvironmentCreation: start..");
        logger.info("");
        
        String jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("JobID");
        
        environmentCreator.setJobId(jobId);
        environmentCreator.create();
        
        logger.info("");
        logger.info("EnvironmentCreation: finished..");
		
		return RepeatStatus.FINISHED;
	}
}
