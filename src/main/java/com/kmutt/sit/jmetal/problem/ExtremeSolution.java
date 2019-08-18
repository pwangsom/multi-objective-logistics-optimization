package com.kmutt.sit.jmetal.problem;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtremeSolution {
	private Integer extremeId = Integer.MAX_VALUE;
	
	public ExtremeSolution() {
		
	}
	
	public ExtremeSolution(Integer id) {
		this.extremeId = id;
	}
}
