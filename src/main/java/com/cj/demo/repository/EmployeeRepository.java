package com.cj.demo.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cj.demo.entity.Department;
import com.cj.demo.entity.Employee;

public interface EmployeeRepository extends 
	JpaRepository<Employee, Integer>,EmployeeRepositoryCustom<Employee, Integer>
	,JpaSpecificationExecutor<Employee>{
	
	/* Named Query*/
	public List<Employee> findByName(String name);
	public List<Employee> findByDepartmentId(Integer id);
	public List<Employee> findByDepartment(Department department);
	
	/* JPQL Query*/
	@Query("from Employee where name like ?1")
	public Employee queryByName(String name);
	
	@Query("from Employee e where e.department.id=?1")
	public List<Employee> queryByDepartmentId(Integer departmentId);
	
	@Query("from Employee e left join e.department where e.id=?1") // will return e & e.department,store to Employee
	public Employee queryByIdWithDepartment(Integer id);
	
	/* Update */
	@Query("update Employee set name=?2 where id=?1")
	@Modifying
	public int updateName(Integer id,String newName);
	
	/* Delete */
	@Query("delete from Employee where id=?1")
	@Modifying
	public int deleteOneById(Integer id);		// can't return removed entities
	
	/* Advance - Pagable */
	@Query("from Employee e where e.department.id=?1")
	public Page<Employee> listByPage(Integer departmentId,Pageable pageable);
	
	@Query("from Employee e left join e.department where e.department.id=?1")	// will return e & e.department,two objects
	public Page<Object[]> listWithDepartmentByPage(Integer departmentId,Pageable pageable);
	
	@Query( value="from Employee e left join fetch e.department where e.department.id=?1",
			countQuery="select count(e) from Employee e left join e.department where e.department.id=?1"
//			countQuery="from Employee e where e.department.id=?1"
			) // use fetch, will return e & e.department,store to Employee,countQuery can't use fetch !!
	public Page<Employee> listFetchWithDepartmentByPage(Integer departmentId,Pageable pageable);
	
	/* Advance - EntityGraph*/
	@EntityGraph(attributePaths="department"/*,type = EntityGraph.EntityGraphType.FETCH*/)
	@Query("from Employee e")
	public Page<Employee> listByGraphAndPage(Pageable pageable);	
	
	//override findAll,add @EntityGraph
	//@EntityGraph(attributePaths="department"/*,type = EntityGraph.EntityGraphType.FETCH*/)
	//public Page<Employee> findAll(Specification<Employee> spec,Pageable pageable);
	
	
	//----------------------------------------------
	
	public boolean existsByName(String name);
	
	public List<Employee> deleteByDepartmentId(Integer departmentId);
	
	@Query("delete from Employee where department.id=?1")
	@Modifying
	public int deleteDirectlyByDepartmentId(Integer departmentId);
		
}
