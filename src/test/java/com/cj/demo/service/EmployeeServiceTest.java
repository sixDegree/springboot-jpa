package com.cj.demo.service;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.LazyInitializationException;
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
import org.springframework.transaction.annotation.Transactional;

import com.cj.demo.entity.Department;
import com.cj.demo.entity.Employee;
import com.cj.demo.util.FetchEntity;
import com.cj.demo.util.SearchCondition;
import com.cj.demo.util.SpecificationBuildUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeServiceTest {

	@Autowired
	private EmployeeService employeeService;

	@Test
	public void setupData(){
		for(int i=0;i<3;i++){
			Employee e=new Employee();
			e.setName("EMP"+i);
			e.setRemark("For Emp "+i);
			e.setDepartment(new Department(3));
			this.employeeService.save(e);
		}
		
		for(int i=0;i<3;i++){
			Employee e=new Employee();
			e.setName("Test"+i);
			e.setRemark("For Test "+i);
			e.setDepartment(new Department(5));
			this.employeeService.save(e);
		}
	}
	
	/*
	 * Search by Default Function: E getOne(id)
	 * 
	 * return a proxy: E_$$_jvstxxx, lazyLoad
	 * 
	 * trigger query when get property value.
	 * 
	 * when not found,throw EntityNotFoundException
	 * 
	 * */
	
	@Test
	public void getOneTest() {
		try {
			Integer id=1;
			Employee employee=this.employeeService.getOne(id); // used employeeRepository.getOne(id), return a proxy,lazyLoad
			System.out.println(employee);
		}catch(LazyInitializationException e) {
			System.out.println("need to add @Transactional on test function when testing.");
		}
	}
	@Test
	@Transactional
	public void getOneTxnTest1() {
		try {
			Integer id=100;
			Employee employee=this.employeeService.getOne(id); // used employeeRepository.getOne(id)
			System.out.println(employee);
		}catch(EntityNotFoundException e) {
			System.out.println("Throw exception when not found,details:"+e.getMessage());
		}
	}
	@Test
	@Transactional
	public void getOneTxnTest2() {
		Integer id=1;
		Employee employee=this.employeeService.getOne(id); // used employeeRepository.getOne(id)
		System.out.println(employee);
		// will trigger 3 queries: employee -> department -> employees
	}
	
	/*
	 * Search by Default Function: Optional<E> findById(id)
	 * 
	 * execute query immediately
	 * 
	 * won't throw EntityNotFoundException
	 * 
	 * */
	@Test
	public void findByIdTest() {
		Integer id=1;
		Employee employee=this.employeeService.findById(id);
		if(employee!=null)
			System.out.println(employee.getName());
		else
			System.out.println("Not exist");
		
		try {
			System.out.println(employee);
		}catch(LazyInitializationException e) {
			System.out.println("can't get join object,need to add @Transactional on test function when testing.");
		}
	}
	
	/*
	 * Search by Default Function: List<E> findAll()
	 * 
	 * execute query immediately
	 * */
	@Test
	public void findAllTest() {
		List<Employee> list=this.employeeService.findAll();
		System.out.println(list.size());
		for(Employee e:list) {
			System.out.println(e.getName());
		}
	}
	
	
	/*
	 * Search by Named Function: findByName
	 * 
	 * */
	@Test
	public void findByNameTest() {
		String name="test";
		List<Employee> list=this.employeeService.findByName(name);
		System.out.println(list.size());
		for(Employee e:list) {
			System.out.println(e.getName());
		}
	}
	
	
	/*
	 * Search by Named Function: findByDepartmentId(departmentId)
	 * 
	 * one query : join two tables: Employee left join Department, but only select Employee
	 * 
	 * same as:
	 * select p from Employee p left join p.department c where c.id=?
	 * 
	 * return:
	 * 	Employee:
	 * 		id
	 * 		name
	 * 		remark
	 * 		department - proxy: department_$$_xxxx (id)
	 * 
	 * */
	@Test
	public void findByDepartmentIdTest() {
		Integer departmentId=1;
		List<Employee> list=this.employeeService.findByDepartmentId(departmentId);
		System.out.println(list.size());
		for(Employee e:list) {
			System.out.println(e.getName());
			System.out.println(e.getDepartment().getId());
			//System.out.println(e.getDepartment().getName()); // will throw LazyInitializationException if not add @Transactional
		}
	}
	
	/*
	 * Search by Named Function: findByDepartment(department) -- same as: findByDepartmentId(departmentId)
	 * 
	 * Note: only search by department id, the department name won't be used in query.
	 * 
	 * */
	@Test
	public void findByDepartmentTest() {
		Department department=new Department();
		department.setId(1);
		department.setName("QA");
		List<Employee> list=this.employeeService.findByDepartment(department);
		System.out.println(list.size());
		for(Employee e:list) {
			System.out.println(e.getName());
			System.out.println(e.getDepartment().getId());
			//System.out.println(e.getDepartment().getName()); // will throw LazyInitializationException if not add @Transactional
		}
	}
	
	/*
	 *  Search by @Query : from Employee where name like ?1
	 *  
	 * */
	@Test
	public void queryByNameTest() {
		//String name="%o%";
		String name="john";
		Employee e=this.employeeService.queryByName(name);	
		// if has multiple results,throw IncorrectResultSizeDataAccessException, should return List<Employee>
		if(e!=null) {
			System.out.println(e.getId()+":"+e.getName());
			System.out.println(e.getDepartment().getId());
		}
	}
	
	/*
	 *  Search by @Query : from Employee e where e.department.id=?1
	 *  
	 * */
	@Test
	public void queryByDepartmentIdTest() {
		Integer departmentId=1;
		List<Employee> list=this.employeeService.queryByDepartmentId(departmentId);
		System.out.println(list.size());
		for(Employee e:list) {
			System.out.println(e.getName());
			System.out.println(e.getDepartment().getId());
		}
	}
	
	/*
	 * Search by @Query : from Employee e left join e.department where e.id=?1
	 * 
	 * Note: 
	 * 	1. can't use " select * "
	 * 	2. if use " select e " ,won't get department,may be throw LazyInitializationException
	 * 	3. could use "select e,e.department"
	 *  4. for ManyToOne, only " left join " is enough,"fetch" is not necessary.
	 *  
	 *  return:
	 *  	Employee:
	 *  		id
	 *  		name
	 *  		remark
	 *  		department:
	 *  			id
	 *  			name
	 *  			remark
	 *  			employees:empty
	 *  
	 * */
	@Test
	public void queryByIdWithDepartmentTest() {
		Integer id=1;
		Employee e=this.employeeService.queryByIdWithDepartment(id);
		if(e!=null) {
			System.out.println(e.getName());
			System.out.println(e.getDepartment().getId()+":"+e.getDepartment().getName());
			System.out.println(e.getDepartment().getEmployees());
		}else {
			System.out.println("Not Exist!");
		}
	}
	
	
	//----------------------------------------------------------------------------------
	
	/*
	 * Create: T save(T)
	 * 
	 * not set T primary key, execute "insert" directly.
	 * 
	 * return:
	 * 	Employee
	 * 		id
	 * 		name
	 * 		remark
	 * 		department:
	 * 			id
	 * 			name: null
	 * 			remark:null
	 * 			employees:empty
	 * eg: 
	 * Employee [id=3, name=Test, department=Department [id=1, name=null, remark=null, employees=[]], remark=This is Test Employee]
	 * 
	 * */
	@Test
	public void createTest() {
		Employee e=new Employee();
		e.setName("Test");
		e.setRemark("This is Test Employee");
		e.setDepartment(new Department(1));
		Employee saved=this.employeeService.save(e);
		System.out.println(e.equals(saved));			// true
		System.out.println(saved);
	}
	
	/*
	 * Create: T save(T)
	 * 
	 * if department not exist, throw Exception when execute insert.
	 *
	 * */
	@Test
	public void createTest2() {
		Employee e=new Employee();
		e.setName("Test2");
		e.setRemark("This is Test Employee 2");
		e.setDepartment(new Department(100));	// not exist department
		Employee saved=this.employeeService.save(e);	// throw DataIntegrityViolationException,ConstraintViolationException
		System.out.println(e.equals(saved));
		System.out.println(saved);
	}
	
	/*
	 * Create: T save(T)
	 * 
	 * if set primary key, system will execute "select" by the primaryKey first:
	 * 		if not exist => execute "insert", return new saved persist T ( not same with previous T )
	 * 		if exist & has change => execute "update", return updated persist T ( not same with previous T )
	 * 
	 * return:
	 * 	Employee
	 * 		id
	 * 		name
	 * 		remark
	 * 		department - proxy : department_$$_xxxx (id)
	 * 
	 * */
	@Test
	public void createTest3() {
		Employee e=new Employee();
		e.setId(100);					// set not exist primary key - id
		e.setName("Test3");
		e.setRemark("This is Test Employee 3");
		e.setDepartment(new Department(1));
		Employee saved=this.employeeService.save(e);
		System.out.println(e.equals(saved));	// false
		//System.out.println(saved);
	}
	
	//----------------------------------------------------------------------------------
	
	/*
	 * Update: T save(T e)
	 * 
	 * Not a persist T:
	 * 		"select" by Id first
	 * 		if not exist => execute "insert"
	 * 		if exist:
	 * 				=> if no change => do nothing, just return the persist T
	 * 				=> if has change => do "update" (update all columns),then return the udpated presist T
	 * 
	 * a persist T:
	 * 		won't execute "select" again.
	 * 		if no change => do nothing, just return the presist T
	 * 		if has change => do "udpate" (udpate all columns),then return the udpated persist T
	 * 
	 * Note: 
	 * 	For Hibernate implentation, if add @DynamicUpdate on the entity
	 * 		=> the update would be dynamic,just update the changed column value
	 * 
	 * return:
	 * 	Employee
	 * 		id
	 * 		name
	 * 		remark
	 * 		department - proxy : department_$$_xxxx (id)
	 * */
	
	@Test
	public void updateUnPersistTest() {
		Employee e = new Employee();
		e.setId(1);						// set exist primary key - id
		e.setName("Hello");
		e.setDepartment(new Department(1));
		Employee saved=this.employeeService.update(e);
		System.out.println(e.equals(saved));
		//System.out.println(saved);
	}
	
	@Test
	public void updateSelectdOutTxnTest() {
		Integer id=1;
		Employee e = this.employeeService.findById(id);			// execute select
		e.setName(e.getName()+" to Test");	
		Employee saved=this.employeeService.update(e);			// execute select and update
		System.out.println(e.equals(saved));
		//System.out.println(saved);
	}
	
	@Test
	public void updateSelectdInTxnTest() {
		Employee e = new Employee();
		e.setId(1);						// set exist primary key - id
		e.setName("Hello2");
		e.setDepartment(new Department(1));
		
		Employee saved=this.employeeService.updateSelectedInTxn(e);
		System.out.println(e.equals(saved));
		//System.out.println(saved);
	}
	
	/*
	 * Update by @Query
	 * 
	 * Note - Repository
	 * 	add @Modify
	 *  Modifying queries can only use void or int/Integer as return type.
	 * 
	 * */
	@Test
	public void updateNameByQueryTest() {
		Employee e = new Employee();
		e.setId(2);
		e.setName("Hello-Name3");
		
		int result=this.employeeService.updateName(e);
		System.out.println(result);
	}
	
	/*
	 * Update by Custom Implementation
	 * 
	 * Use em and build dynamic "update" hql by properties
	 * ( No need add @Modify )
	 * 
	 * */
	@Test
	public void updateDynamicByHQLTest() {
		Employee e = new Employee();
		e.setId(3);
		e.setName("Hello-Dynamic4");
		e.setDepartment(new Department(1));
		
		boolean result=this.employeeService.updateDynamic(e, "name");
		System.out.println(result);
	}
	
	//----------------------------------------------------------------------------------
	
	
	/*
	 * Delete by default function ( no return): delete(T entity)
	 * 
	 *  execute "select" by Id first
	 *  	if exist => execute "delete" by Id
	 *  	if not exist => execute "insert" !!!
	 * 
	 * */
	
	@Test
	public void deleteEntityTest() {
		Employee e = new Employee();
		e.setId(3);
		e.setName("Hello-Delete");
		this.employeeService.delete(e);
		System.out.println("finished");
	}
	
	/*
	 * Delete by default function (no return): delete(ID id)
	 * 
	 *  execute "select" by Id first
	 *  	if exist => execute "delete" by Id
	 *  	if not exist => throw EmptyResultDataAccessException
	 * 
	 * */
	@Test
	public void deleteByIdTest() {
		Integer id=2;
		this.employeeService.deleteById(id);
	}
	
	@Test
	public void deleteSelectedInTxnTest() {
		Integer id=6;
		Employee deleted=this.employeeService.deleteSelectedInTxn(id);
		System.out.println(deleted!=null?deleted.getName():null);
		//System.out.println(deleted);
	}
	
	/*
	 * Delete by @Query - return changed row count
	 * 
	 * Note - 
	 * 	add @Modify on Repository function
	 * 
	 * */
	@Test
	public void deleteDirectlyTest() {
		Integer id=2;
		int result=this.employeeService.deleteDirectly(id);
		System.out.println(result);
	}
	
	/*
	 * Delete by Custom Implementation
	 * 
	 * Use em and build dynamic "delete" hql by properties
	 * ( No need add @Modify )
	 * 
	 * */
	@Test
	public void deleteDynamicTest() {
		Employee e = new Employee();
		e.setId(6);
		e.setName("Test");
		int result=this.employeeService.deleteDynamic(e,"id","name");
		System.out.println(result);
	}
	
	/*
	 * Delete - Use named deleteByXxx,removeByXxxx
	 * 
	 * select e from Employee e left join e.department d where d.id=?
	 * => employee ids
	 * 		delete from Employee where id=?
	 * 		delete from Employee where id=?
	 * 		... 
	 * 
	 * could return int/Integer/removed entities
	 * 
	 * */
	@Test
	public void deleteByDepartmentIdTest(){
		Integer departmentId=6;
//		int result=this.employeeService.deleteByDepartmentId(departmentId);
//		System.out.println(result);
		List<Employee> removed=this.employeeService.deleteByDepartmentId(departmentId);
		for(Employee e: removed){
			System.out.println(e.getId()+":"+e.getName());
			
		}
	}
	
	//----------------------------------------------------------------------------------
	
	/*
	 * Advance - Pagable
	 * 
	 * execute two sqls:
	 * 	select * from xxx where ...
	 * 	select count(id) from xxx where ...
	 * 
	 * Note: 
	 * 	The JPQL should use alias for tables. 
	 * eg:
	 * 	Don't use "from Employee where department.id=?1",Please use "from Employee e where e.department.id=?1"
	 * 
	 * */
	@Test
	public void listByPageTest() {
		Integer departmentId=1;
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Page<Employee> page=this.employeeService.listByPage(departmentId, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentId:"+e.getDepartment().getId());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - Pagable (join)
	 * 
	 * JPQL: from Employee e left join e.department where ....
	 * -> for query & countQuery[same as : select count(e) from Employee e left join e.department where ...]
	 * 
	 * Note: 
	 * 	return e & e.department and store in Employee.
	 * 	if use pageable, the queryed e & e.department won't store in Employee,will be two objects
	 * 
	 * */
	@Test
	public void listWithDepartmentByPageTest() {
		Integer departmentId=1;
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Page<Object[]> page=this.employeeService.listWithDepartmentByPage(departmentId, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Object[]> list=page.getContent();
		for(Object[] obj:list) {
			Employee e=(Employee)obj[0];
			Department d=(Department)obj[1];
			System.out.println(e.getId()+":"+e.getName()+" Department:"+d.getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - Pagable (fetch)
	 * 
	 * JPQL: 
	 * query(use fetch): 
	 * 		from Employee e left join fetch e.department where ....
	 * countQuery(can't use fetch): 
	 * 		select count(e) from Employee e left join e.department where ....
	 * 		alse could just use: select count(e) from Employee e where ...
	 * 
	 * Note: 
	 * 	Used pageable, if want queryed e & e.department store in Employee,need to use fetch.
	 *  but can't use fetch for countQuery 
	 *  ( else will throw org.hibernate.QueryException: 
	 *  	query specified join fetching, but the owner of the fetched association was not present in the select list .
	 *  )
	 * 
	 * */
	@Test
	public void listFetchWithDepartmentByPageTest() {
		Integer departmentId=1;
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Page<Employee> page=this.employeeService.listFetchWithDepartmentByPage(departmentId, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" Department:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	//----------------------------------------------------------------------------------
	
	/*
	 * Advance - Specification
	 * Note: 
	 * 	1. criteriaBuilder 可以从EntityManager 或 EntityManagerFactory类中获得CriteriaBuilder，构建Predicate
	 *  eg: CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
	 *  criteriaBuilder.equal,like,gt,le,...,and,or,not
	 *  
	 *  2. query 可以从criteriaBuilder.createQuery, criteriaBuilder.createTupleQuery获取
	 *  
	 *  3. root 相当于SQL中的FROM，可以使用query.from获取，常用方法root.join,root.fetch,root.get
	 * 
	 * 使用
	 * 	1. criteriaBuilder.equal(root.get("department").get("id"), 1);  -- can't use root.get("department.id")
	 * 	select * from Employee where department.id=? and name like ?; (won't join Department)
	 * 
	 * 	2. criteriaBuilder.equal(root.get("department").get("name"), "QA"); -- will auto inner join Department
	 *  select * from Employee e join e.department where department.name=? and name like ?;  ( )
	 * 
	 *  3. criteriaBuilder.equal(root.join("department",JoinType.LEFT).get("name"),"QA"); -- will left join Department
	 *  select * from Employee e left join e.department where department.name=? and name like ?;
	 *
	 * */
	
	@Test
	public void listBySpecificationTest() {
		Specification<Employee> spec1=new Specification<Employee>() {
			@Override
			public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
//				return criteriaBuilder.equal(root.get("department").get("id"), 1);
//				return criteriaBuilder.equal(root.get("department").get("name"), "QA");
				return criteriaBuilder.equal(root.join("department",JoinType.LEFT).get("name"),"QA");
			}
		};
		Specification<Employee> spec2=new Specification<Employee>() {
			@Override
			public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				return criteriaBuilder.like(root.get("name"), "Test%");
			}
		};
		
		//List<Employee> list=this.employeeService.listBySpecification(Specification.where(spec1).and(spec2));
		List<Employee> list=this.employeeService.listBySpecification(spec1.and(spec2));
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentId:"+e.getDepartment().getId());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - Specification ( fetch )
	 * 
	 * root: join,fetch,get
	 * left join fetch:  root.fetch("department",JoinType.LEFT);
	 * 
	 * query.select	- return query
	 * query.from	- return root
	 * query.where	- return query
	 * 
	 * */
	@Test
	public void listWithDepartmentBySpecificationTest() {
		Specification<Employee> spec=new Specification<Employee>() {
			@Override
			public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				root.fetch("department",JoinType.LEFT);
				Predicate p1=criteriaBuilder.equal(root.get("department").get("id"), 1);
				Predicate p2=criteriaBuilder.like(root.get("name"), "Test%");
				query.where(criteriaBuilder.and(p1,p2))
					.orderBy(criteriaBuilder.desc(root.get("id")));
				return query.getRestriction();
			}
		};
		List<Employee> list=this.employeeService.listBySpecification(spec);
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" Department:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - Specification ( custom )
	 * 
	 * Simplify build Specification
	 * 
	 * */
	@Test
	public void listByCustomBuildSpecification() {
		List<SearchCondition> conditions=new ArrayList<SearchCondition>();
		conditions.add(new SearchCondition("department.id","=",1));
		conditions.add(new SearchCondition("name",":","Test%"));
		Specification<Employee> spec=SpecificationBuildUtils.build(conditions,new FetchEntity("department",JoinType.LEFT));
		
		List<Employee> list=this.employeeService.listBySpecification(spec);
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" Department:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - Specification + Pageable
	 * 
	 * Note:
	 * 	countQuery can't use fetch, so use the CriteriaQuery.getResultType() method 
	 * 	to check whether the query's projection is Long or the class the Specification is operating on. 
	 * 
	 * */
	
	@Test
	public void listBySpecificationAndPageTest() {
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Specification<Employee> spec=new Specification<Employee>() {
			@Override
			public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				// to check whether it's a count query
				if(!Long.class.equals(query.getResultType())){
					root.fetch("department",JoinType.LEFT);
				}
				Predicate p1=criteriaBuilder.equal(root.get("department").get("id"), 1);
				Predicate p2=criteriaBuilder.like(root.get("name"), "Test%");
				query.where(criteriaBuilder.and(p1,p2))
					.orderBy(criteriaBuilder.desc(root.get("id")));
				return query.getRestriction();
			}
		};
		Page<Employee> page=this.employeeService.listBySpecificationAndPage(spec,pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentName:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	@Test
	public void listBySpecificationAndPageTest2() {
		
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		List<SearchCondition> conditions=new ArrayList<SearchCondition>();
		conditions.add(new SearchCondition("department.name","=","QA"));
		conditions.add(new SearchCondition("name",":","Test%"));
//		Specification<Employee> spec=SpecificationBuildUtils.build(conditions);
		Specification<Employee> spec=SpecificationBuildUtils.build(conditions,new FetchEntity("department",JoinType.LEFT));
		
		Page<Employee> page=this.employeeService.listBySpecificationAndPage(spec, pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentName:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	
	//----------------------------------------------------------------------------------
	
	/*
	 * Advance - @EntityGraph (Pageable + @Query)
	 * 
	 * @EntityGraph(attributePaths="department")
	 * @Query("from Employee e")
	 * + pageable
	 * 
	 * =>
	 * query: select e,e.department from Employee left join fetch e.department where ...
	 * countQuery: select count(e.id) from Employee
	 * 
	 * */
	
	@Test
	public void listByGraphAndPageTest() {
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Page<Employee> page=this.employeeService.listByGraphAndPage(pageable);
		
		System.out.println("Total Records:"+page.getTotalElements());
		System.out.println("Total Pages:"+page.getTotalPages());
		System.out.println("Current Page:"+(page.getNumber()+1));
		System.out.println("Current Records:"+page.getNumberOfElements());
		System.out.println("Limit:"+page.getSize());
		System.out.println("Sort:"+page.getSort());
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentName:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	/*
	 * Advance - @EntityGraph ( Pageable + Specification )
	 * 
	 * after override the findAll in Repository level. below test will be ok.
	 * no need to set fetch manually.
	 * 
	 * @EntityGraph(attributePaths="department")
	 * public Page<Employee> findAll(Specification<Employee> spec,Pageable pageable);
	 *
	 * */
	@Test
	public void listByGraphAndSpecificationAndPageTest() {
		Sort sort=Sort.by(Order.desc("name"),Order.asc("id"));
		Pageable pageable=PageRequest.of(0, 3,sort);
		
		Specification<Employee> spec=new Specification<Employee>() {
			@Override
			public Predicate toPredicate(Root<Employee> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				
				// to check whether it's a count query
//				if(!Long.class.equals(query.getResultType())){
//					root.fetch("department",JoinType.LEFT);
//				}
				Predicate p1=criteriaBuilder.equal(root.get("department").get("id"), 1);
				Predicate p2=criteriaBuilder.like(root.get("name"), "Test%");
				query.where(criteriaBuilder.and(p1,p2))
					.orderBy(criteriaBuilder.desc(root.get("id")));
				return query.getRestriction();
			}
		};
		
		Page<Employee> page=this.employeeService.listByGraphAndSpecificationAndPage(spec,pageable);
		
		List<Employee> list=page.getContent();
		for(Employee e:list) {
			System.out.println(e.getId()+":"+e.getName()+" DepartmentName:"+e.getDepartment().getName());
		}
		System.out.println("Record Size:"+list.size());
	}
	
	//----------------------------------------------------------------------------------
	
	
}
