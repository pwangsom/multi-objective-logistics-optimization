package com.kmutt.sit.batch.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kmutt.sit.batch.LogisticsHelper;

@Service
public class LogisticsHelperBuilder implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(LogisticsHelperBuilder.class);
	
	@Autowired
	private LogisticsHelper logisticsHelper;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
        logger.info("LogisticsHelperBuilder: start..");  
        logger.info(""); 
        
        logisticsHelper.initial();
        
        logger.info("");
        logger.info("LogisticsHelperBuilder: finished..");
		
		return RepeatStatus.FINISHED;
	}
	
}
