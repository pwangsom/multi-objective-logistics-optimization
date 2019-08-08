package com.kmutt.sit.optimization;

import com.kmutt.sit.jmetal.runner.NsgaIIIHelper;

public class OptimizationHelper {
	
	public static String getFileOutputName(NsgaIIIHelper nsgaIIIHelper, String vehicleType, String fileType) {
		return nsgaIIIHelper.getLogisticsHelper().getOutputPath() + "/" + nsgaIIIHelper.getJobId() 
				+ "-" + nsgaIIIHelper.getShipmentDate() + "-" + vehicleType + "-" + fileType + ".csv";
	}

}
