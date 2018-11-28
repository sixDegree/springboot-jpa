package com.cj.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cj.demo.entity.Department;
import com.cj.demo.entity.Employee;
import com.cj.demo.repository.EmployeeRepository;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;
	
	/* Search by Default Function */
	
	public Employee getOne(Integer id) {
		return this.employeeRepository.getOne(id);
	}
	
	public Employee findById(Integer id) {
		Optional<Employee> e= this.employeeRepository.findById(id);
		if(e.isPresent())
			return e.get();
		return null;
	}
	
	public List<Employee> findAll(){
		return this.employeeRepository.findAll();
	}
	
	/* Search by Named Function */
	
	public List<Employee> findByName(String name) {
		return this.employeeRepository.findByName(name);
	}
	
	public List<Employee> findByDepartment(Department department){
		return this.employeeRepository.findByDepartment(department);
	}
	
	public List<Employee> findByDepartmentId(Integer departmentId){
		return this.employeeRepository.findByDepartmentId(departmentId);
	}
	
	/* Search by @Query */
	
	public Employee queryByName(String name){
		return this.employeeRepository.queryByName(name);
	}
	
	public List<Employee> queryByDepartmentId(Integer departmentId){
		return this.employeeRepository.queryByDepartmentId(departmentId);
	}
	
	public Employee queryByIdWithDepartment(Integer id) {
		return this.employeeRepository.queryByIdWithDepartment(id);
	}
	
	
	/* Create */
	@Transactional
	public Employee save(Employee e) {
		return this.employeeRepository.save(e);
	}
	
	/* Update: Default Function */
	@Transactional
	public Employee update(Employee e) {
		return this.employeeRepository.save(e);
	}
	
	/* Update: Select first in Txn */
	@Transactional
	public Employee updateSelectedInTxn(Employee e) {
		Optional<Employee> obj=this.employeeRepository.findById(e.getId());
		if(obj.isPresent()) {
			Employee pre=obj.get();
			BeanUtils.copyProperties(e, pre);
			return this.employeeRepository.save(pre);	// won't execute select,will execute update if has changes.
		}else {
			return null;
		}
	}
	
	/* Update: @Query + @Modify */
	@Transactional
	public int updateName(Employee e) {
		return this.employeeRepository.updateName(e.getId(),e.getName());
	}
	
	/* Update: Dynamic: em + HQL */
	@Transactional
	public boolean updateDynamic(Employee e,String...properties) {
		return this.employeeRepository.update(e, properties);
	}
	
	/* Delete */
	
	@Transactional
	public void delete(Employee e) {
		this.employeeRepository.delete(e);
	}
	
	@Transactional
	public void deleteById(Integer id) {
		this.employeeRepository.deleteById(id);
	}
	
	@Transactional
	public Employee deleteSelectedInTxn(Integer id) {
		Optional<Employee> e=this.employeeRepository.findById(id);
		if(e.isPresent()) {
			this.employeeRepository.delete(e.get());
			return e.get();
		}
		return null;
	}
	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public int deleteDirectly(Integer id) {
		return this.employeeRepository.deleteOneById(id);
	}
	
	@Transactional
	public int deleteDynamic(Employee e,String...properties) {
		return this.employeeRepository.delete(e, properties);
	}
	
	@Transactional
	public List<Employee> deleteByDepartmentId(Integer departmentId){
		return this.employeeRepository.deleteByDepartmentId(departmentId);
	}
	
	
	/* Advance - Pageable */
	
	public Page<Employee> listByPage(Integer departmentId,Pageable pageable){
		return this.employeeRepository.listByPage(departmentId, pageable);
	}
	
	public Page<Object[]> listWithDepartmentByPage(Integer departmentId,Pageable pageable){
		return this.employeeRepository.listWithDepartmentByPage(departmentId,pageable);
	}
	
	public Page<Employee> listFetchWithDepartmentByPage(Integer departmentId,Pageable pageable){
		return this.employeeRepository.listFetchWithDepartmentByPage(departmentId, pageable);
	}
	
	/* Advance - Specification */
	public List<Employee> listBySpecification(Specification<Employee> spec) {
		return this.employeeRepository.findAll(spec);
	}
	
	/* Advance - Specification + Pageable */
	public Page<Employee> listBySpecificationAndPage(Specification<Employee> spec,Pageable pageable) {
		return this.employeeRepository.findAll(spec,pageable);
	}
	
	/* Advance - EntityGraph */
	public Page<Employee> listByGraphAndPage(Pageable pageable){
		return this.employeeRepository.listByGraphAndPage(pageable);
	}
	
	public Page<Employee> listByGraphAndSpecificationAndPage(Specification<Employee> spec,Pageable pageable){
		return this.employeeRepository.findAll(spec,pageable);
	}
	

}
