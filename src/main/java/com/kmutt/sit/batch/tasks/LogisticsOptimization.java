package com.kmutt.sit.batch.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kmutt.sit.optimization.OptimizationManager;

@Service
public class LogisticsOptimization implements Tasklet {

	private static Logger logger = LoggerFactory.getLogger(LogisticsOptimization.class);
	
	@Autowired
	private OptimizationManager optimizationManager;
	
    @Value("${generated.evaluator.enabled}")
    private boolean isEnabled;
	
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub

        logger.info("LogisticsOptimizer: start....."); 
        
        String jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("JobID");        

        if(isEnabled) {            
            optimizationManager.setJobId(jobId);
            optimizationManager.opitmize();
        }
        
        logger.info("LogisticsOptimizer: finished..");  
		
		return RepeatStatus.FINISHED;
	}

}
