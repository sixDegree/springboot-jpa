package com.cj.demo.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import com.cj.demo.entity.Department;
import com.cj.demo.entity.Employee;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DepartmentServiceTest {

	@Autowired
	private DepartmentService departmentService;
	
	@Test
	public void setupData(){
		for(int i=0;i<5;i++){
			Department d = new Department();
			d.setName("Dep "+i);
			d.setRemark("Test Department "+i);
			Department saved=this.departmentService.saveOrUpdate(d);
			System.out.println("Department vs savedDepartment:"+d.equals(saved)); // true
			
			for(int j=0;j<3;j++){
				Employee e = new Employee();
				e.setName("Emp "+j);
				e.setRemark("Test Employee "+j);
				e.setDepartment(saved); // same as e.setDepartment(d);
				this.departmentService.saveEmployee(e);
			}
		}
	}
	
	/*
	 * @Query("select distinct p from Department p left join fetch p.employees")
	 * 
	 * select * from department left outer join employee;
	 * => Department1
	 * 		Employee1
	 * 		Employee2
	 * 
	 * Note: use " select distinct p " for root
	 * 
	 * */
	
	@Test
	public void queryWithEmployeesTest(){
		List<Department> list=this.departmentService.listWithEmployees();
		for(Department d:list) {
			System.out.println(d.getId()+":"+d.getName());
			for(Employee e:d.getEmployees())
				System.out.println("\t"+e.getId()+":"+e.getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * 
	 * Summary:
	 *  For oneToMany,need to use 'join' to set Many's conditions.
	 *  If want to fetch Many:
	 *  	ref:
	 * 			https://stackoverflow.com/questions/17306655/using-the-jpa-criteria-api-can-you-do-a-fetch-join-that-results-in-only-one-joi
	 *  	Method1: root.fetch,then cast to join
	 *  	Method2: use @EntityGraph
	 *  Note: won't limit records,and the total records are not correct.
	 * 
	 * */
	
	@Test
	public void listWithEmployeesTest(){
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Specification<Department> spec=new Specification<Department>() {
			@Override
			public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				Predicate p1=null;
				// to check whether it's a count query -- no need if used @EntityGraph
				if(!Long.class.equals(query.getResultType()) && !long.class.equals(query.getResultType())){
					Fetch<Department,Employee> ep=root.fetch("employees",JoinType.LEFT);
					Join<Department,Employee> join=(Join<Department,Employee>)ep;
					p1=criteriaBuilder.like(join.get("name"), "Emp%");
				}else{
					p1=criteriaBuilder.like(root.join("employees",JoinType.LEFT).get("name"), "Test%");
				}
				
//				Predicate p1=criteriaBuilder.like(root.get("employees").get("name"), "Test%");	//error
//				Predicate p1=criteriaBuilder.like(root.join("employees",JoinType.LEFT).get("name"), "Test%");
				Predicate p2=criteriaBuilder.like(root.get("name"), "Dep%");
				query.where(criteriaBuilder.and(p1,p2))
					.orderBy(criteriaBuilder.desc(root.get("id")));
				return query.getRestriction();
//				return p2;
			}
		};
		
		Page<Department> page=this.departmentService.listWithEmployees(spec, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Department> list=page.getContent();
		for(Department d:list) {
			System.out.println(d.getId()+":"+d.getName());
			for(Employee e:d.getEmployees())
				System.out.println("\t"+e.getId()+":"+e.getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * 
	 * @EntityGraph(attributePaths="employees")
	 * public Page<Department> findAll(Specification<Department> spec,Pageable pageable);
	 * 
	 * Note: still no 'limit' in select,and total records are not correct.
	 * 
	 * */
	@Test
	public void listWithEmployeesTest2(){
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Specification<Department> spec=new Specification<Department>() {
			@Override
			public Predicate toPredicate(Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				Join<Department,Employee> join=root.join("employees",JoinType.LEFT);
				Predicate p = criteriaBuilder.isNull(join);
				Predicate p1=criteriaBuilder.like(join.get("name"), "Test%");
				Predicate p2=criteriaBuilder.like(root.get("name"), "QA%");
				
				query.where(criteriaBuilder.and(criteriaBuilder.or(p,p1),p2))
//				query.where(criteriaBuilder.and(p1,p2))
					.orderBy(criteriaBuilder.desc(root.get("id")));
				
				return query.getRestriction();
//				return p2;
			}
		};
		
		Page<Department> page=this.departmentService.listWithEmployees(spec, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Department> list=page.getContent();
		for(Department d:list) {
			System.out.println(d.getId()+":"+d.getName());
			for(Employee e:d.getEmployees())
				System.out.println("\t"+e.getId()+":"+e.getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	@Test
	public void listByGraphAndPageTest(){
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Page<Department> page=this.departmentService.listByGraphAndPage("Emp%",pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Department> list=page.getContent();
		for(Department d:list) {
			System.out.println(d.getId()+":"+d.getName());
			for(Employee e:d.getEmployees())
				System.out.println("\t"+e.getId()+":"+e.getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * first check if department existsByName
	 * 		not exist => save -> insert
	 * 		exist => null
	 * 
	 * */
	@Test
	public void createTest(){
		Department department = new Department();
		department.setName("Test1");
		department.setRemark("Remark");
		
		Department saved=this.departmentService.create(department);
		
		System.out.println(department.equals(saved));	// if not exist by name,then insert and true
		if(saved==null)
			System.out.println("Create Fail");
		else
			System.out.println(saved.getId()+":"+saved.getName());
	}
	
	
	@Test
	public void findByExampleTest(){
		Department department=new Department();
		department.setName("Test1-2");
		department.setRemark(null);
		Optional<Department> optional=this.departmentService.findByExample(department);
		if(optional.isPresent()){
			Department d=optional.get();
			System.out.println(d.getId()+":"+d.getName()+","+d.getRemark());
		}else{
			System.out.println("Not found");
		}
		
	}
	
	/*
	 * first get exist department findById -> select
	 * 		not exist => return Optional.Empty()
	 * 		exist => save -> update
	 * */
	@Test
	public void updateTest(){
		Department department = new Department();
		department.setId(29);
		department.setName("Test1-2");
		department.setRemark("Remark");
		
		Optional<Department> saved=this.departmentService.update(department);
		if(saved.isPresent()){
			System.out.println(saved.get().getId()+":"+saved.get().getName());
		}
		else
			System.out.println("Update Fail");
	}
	
	/*
	 * Test @Transanctional
	 *  
	 *  count:1
	 *  update
	 *  count:2
	 *  => 两次读取不一致 （在同一个Service中，设置isolation,progagation无效）
	 * 
	 * */
	@Test
	public void updateNameTxnTest(){
		boolean result=this.departmentService.updateName(28, "DepTest-New1");
		System.out.println(result);
	}
	
	/*
	 * Test @Transactional
	 * 
	 * departmentService getEmployeeCountById @Transactional(isolation=Isolation.REPEATABLE_READ)
	 * employeeService	deleteDirectly @Transactional(propagation=Propagation.REQUIRES_NEW)
	 * 
	 * departmentService call employeeService
	 * 
	 * count:2
	 * delete
	 * count:2
	 * => 两次读取一致
	 * 
	 * */
	@Test
	public void deleteEmployeeTxnTest(){
		this.departmentService.deleteEmployee(28, 41);
	}
	
	
	@Test
	public void deleteDirectlyTest(){
		Integer id=8;
		boolean result=this.departmentService.deleteDirectly(id);
		System.out.println(result);
	}
	
	/*
	 * @EntityGraph on delete
	 * 
	 * before delete, fetch all
	 * 
	 * */
	@Test
	public void deleteGraphTest(){
		Integer id=5;
		List<Department> deleted=this.departmentService.deleteGraph(id);
		if(deleted!=null){
			for(Department d:deleted){
				System.out.println(d.getId()+":"+d.getName());
				for(Employee e:d.getEmployees())
					System.out.println("\t"+e.getId()+":"+e.getName());
			}
		}
	}
	
	
	@Test
	public void addEmployeeTest(){
		Employee employee = new Employee();
		employee.setName("Tom2");
		employee.setRemark("Department Emp Tom2");
		employee.setDepartment(new Department(11));
		
		Employee saved=this.departmentService.addEmployee(employee);
		if(saved==null)
			System.out.println("Add Faild");
		else
			System.out.println(saved.getId()+":"+saved.getName()+":"+saved.getDepartment().getId());
	}
	
	@Test
	public void moveEmployeeTest(){
		Employee employee = new Employee();
		employee.setId(27);
		employee.setName("Tom");
		employee.setRemark("Department Emp Tom");
		employee.setDepartment(new Department(22));
		
		Employee saved=this.departmentService.moveEmployee(employee);
		if(saved==null)
			System.out.println("Move Faild");
		else
			System.out.println(saved.getId()+":"+saved.getName()+":"+saved.getDepartment().getId());
	}
	
	@Test
	public void deleteEmployeeTest(){
		Integer employeeId=10;
		boolean result=this.departmentService.deleteEmployee(employeeId);
		System.out.println(result);
	}
	
	
	
}
