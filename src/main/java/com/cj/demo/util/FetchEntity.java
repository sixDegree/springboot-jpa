package com.cj.demo.util;

import javax.persistence.criteria.JoinType;

public class FetchEntity {
	private String attribute;
	private JoinType joinType;
	
	public FetchEntity(String attribute, JoinType joinType) {
		super();
		this.attribute = attribute;
		this.joinType = joinType;
	}
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public JoinType getJoinType() {
		return joinType;
	}
	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}
	
}
