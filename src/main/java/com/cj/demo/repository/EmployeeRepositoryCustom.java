package com.cj.demo.repository;

public interface EmployeeRepositoryCustom<T,ID> {
	
	 public boolean update(T entity,String...properties);
	 
	 public int delete(T entity,String...properties);
	 
}
