package com.cj.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cj.demo.entity.Department;
import com.cj.demo.entity.Employee;
import com.cj.demo.repository.DepartmentRepository;
import com.cj.demo.repository.EmployeeRepository;


@Service
public class DepartmentService {

	@Autowired
	private DepartmentRepository departmentRepository;
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@Autowired
	private EmployeeService employeeService;
	
	public List<Department> listWithEmployees(){
		return this.departmentRepository.queryAll();
	}
	
	public Page<Department> listWithEmployees(Specification<Department> spec,Pageable pageable){
		return this.departmentRepository.findAll(spec,pageable);
	}
	
	public Page<Department> listByGraphAndPage(String empName,Pageable pageable){
		return this.departmentRepository.listByGraphAndPage(empName,pageable);
	}
	
	public Department findById(Integer id){
		Optional<Department> department=this.departmentRepository.findById(id);
		if(department.isPresent())
			return department.get();
		else
			return null;
	}
	
	public Optional<Department> findByExample(Department department){
		Example<Department> example=Example.of(department);
		return this.departmentRepository.findOne(example);
	}
	
	@Transactional
	public Department saveOrUpdate(Department department){
		return this.departmentRepository.save(department);
	}
	
	@Transactional
	public Employee saveEmployee(Employee employee){
		return this.employeeRepository.save(employee);
	}
	
	@Transactional
	public Department create(Department department){
//		Optional<Department> pre=this.departmentRepository.findByName(department.getName());
//		if(pre.isPresent())
//			return null;
		boolean isExist=this.departmentRepository.existsByName(department.getName());
		if(isExist)
			return null;
		
		department.setId(null);
		department.setEmployees(null);
		return this.departmentRepository.save(department);
	}
	
	@Transactional
	public Optional<Department> update(Department department){
		Optional<Department> pre=this.departmentRepository.findById(department.getId());
		if(pre.isPresent()){
			Department entity=pre.get();
			entity.setName(department.getName());
			entity.setRemark(department.getRemark());
			Department saved=this.departmentRepository.save(entity);
			System.out.println("saved vs preEntity:"+saved.equals(entity));
			System.out.println("saved vs department:"+saved.equals(department));
		}
		return pre;
	}
	
	@Transactional(isolation=Isolation.REPEATABLE_READ,propagation=Propagation.REQUIRES_NEW)
	public boolean updateName(Integer id,String newName){
		Optional<Department> pre=this.departmentRepository.findById(id);
		if(!pre.isPresent()){
			System.out.println("Not exist");
			return false;
		}
		Department preDept=pre.get();
		if(preDept.getName().equals(newName)){
			System.out.println("Same name");
			return true;
		}
		
		int count=this.departmentRepository.getCountByName(newName);
		System.out.println("Query same named department count:"+count);
		
		System.out.println("Do update...");
		
//		int result=this.departmentRepository.updateName(id, newName);
//		System.out.println("Update "+result+" rows");
		
//		preDept.setName(newName);	// will trigger auto update
		
		int result=this.changeName(id, newName);
		System.out.println("Update "+result+" rows");
		
		System.out.println("Done Update");
		
		count=this.departmentRepository.getCountByName(newName);
		System.out.println("Re-Query same named department count:"+count);
		
		return true;
	}
	
	@Transactional
	public int changeName(Integer id,String newName){
		int updated= this.departmentRepository.updateName(id, newName);
		return updated;
	}
	
	@Transactional
	public List<Department> deleteGraph(Integer id){
//		boolean isExist = this.departmentRepository.existsById(id);
//		if(!isExist)
//			return null;
		
//		this.employeeRepository.deleteByDepartmentId(id);
		this.employeeRepository.deleteDirectlyByDepartmentId(id);
		
		return this.departmentRepository.deleteGraphById(id);
	}
	
	@Transactional
	public boolean deleteDirectly(Integer id){
		this.employeeRepository.deleteDirectlyByDepartmentId(id);
		return this.departmentRepository.deleteDirectlyById(id)>0;
	}
	
	@Transactional
	public Employee addEmployee(Employee employee){
		if(employee.getDepartment()==null || employee.getDepartment().getId()==null)
			return null;
		
		boolean isExistDepartment=this.departmentRepository.existsById(employee.getDepartment().getId());
		if(!isExistDepartment)
			return null;
		
		boolean isExistEmployee=this.employeeRepository.existsByName(employee.getName());
		if(isExistEmployee)
			return null;
		
		employee.setId(null);
		return this.employeeRepository.save(employee);
	}
	
	@Transactional
	public Employee moveEmployee(Employee employee){
		boolean isExistDepartment=this.departmentRepository.existsById(employee.getDepartment().getId());
		if(!isExistDepartment)
			return null;
		
		Optional<Employee> pre=this.employeeRepository.findById(employee.getId());
		if(!pre.isPresent())
			return null;
		
		pre.get().setDepartment(employee.getDepartment());
		return this.employeeRepository.save(pre.get());
	}
	
	@Transactional
	public boolean deleteEmployee(Integer employeeId){
		return this.employeeRepository.deleteOneById(employeeId)>0;
	}
	
	@Transactional(isolation=Isolation.REPEATABLE_READ)
	public void deleteEmployee(Integer departmentId,Integer employeeId){
		int count=this.departmentRepository.getEmployeeCountById(departmentId);
		System.out.println("Query employee count:"+count);
		
		this.employeeService.deleteDirectly(employeeId);
		
		count=this.departmentRepository.getEmployeeCountById(departmentId);
		System.out.println("Re-Query employee count:"+count);
	}
	
}
