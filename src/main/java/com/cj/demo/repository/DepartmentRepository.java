package com.cj.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.cj.demo.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer>,
	JpaSpecificationExecutor<Department> {

	@Query("select distinct p from Department p left join fetch p.employees")
	public List<Department> queryAll();
	
	@EntityGraph(attributePaths="employees")
	public Page<Department> findAll(Specification<Department> spec,Pageable pageable);
	
	@EntityGraph(attributePaths="employees")
	@Query("from Department p left join p.employees e where e.name like ?1")
	public Page<Department> listByGraphAndPage(String empName,Pageable pageable);

	public Optional<Department> findByName(String name);
	
	@Query("select count(1) from Department where name=?1")
	public int getCountByName(String name);
	
	@Query("select count(1) from Employee where department.id=?1")
	public int getEmployeeCountById(Integer id);
	
	@Query("update Department set name=?2 where id=?1")
	@Modifying
	public int updateName(Integer id,String newName);
	
	public boolean existsByName(String name);
	
	@EntityGraph(attributePaths="employees")
	public List<Department> deleteGraphById(Integer id);
	
	@Query("delete from Department where id=?1")
	@Modifying
	public int deleteDirectlyById(Integer id);
	
}
