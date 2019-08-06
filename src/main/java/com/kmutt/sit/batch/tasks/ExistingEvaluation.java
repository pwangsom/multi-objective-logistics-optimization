package com.kmutt.sit.batch.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.kmutt.sit.existing.ExistingSolutionManager;

@Component
public class ExistingEvaluation implements Tasklet {
	
	private static Logger logger = LoggerFactory.getLogger(ExistingEvaluation.class);
	
    @Value("${existing.evaluator.enabled}")
    private boolean isEnabled;
    
    @Autowired
    private ExistingSolutionManager evaluationSolutionManager;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
        logger.info("ExistingSolutionEvaluator: start..");
        logger.info("");
        
        String jobId = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("JobID");

        logger.info("Enable: " + isEnabled);
        if(isEnabled) {
        	evaluationSolutionManager.setJobId(jobId);
        	evaluationSolutionManager.evaluate();
        }
        
        logger.info("");
        logger.info("ExistingSolutionEvaluator: finished..");
		
		return RepeatStatus.FINISHED;
	}

}
