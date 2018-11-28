# springboot-demo


## Spring Data JPA

### Entity

1. Table (`@Table`): 
	- pe_employee : id,name,remark,department_id
	- pe_department : id,name,remark
2. Entity (`@Entity`):
	- Employee : id,name,remark,Department department
	- Department : id,name,remark,List<Employee> employees
3. Relationship (`@ManyToOne`,`@OneToMany`): 
	- Employee -> Department : ManyToOne
	- Employee <- Department : OneToMany


### Code

1. pom.xml
	```xml
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter</artifactId>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-data-jpa</artifactId>
	</dependency>
	<dependency>
		<groupId>mysql</groupId>
		<artifactId>mysql-connector-java</artifactId>
		<scope>runtime</scope>
	</dependency>
	<dependency>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-test</artifactId>
		<scope>test</scope>
	</dependency>
	<dependency>
		<groupId>commons-beanutils</groupId>
		<artifactId>commons-beanutils</artifactId>
		<version>1.8.3</version>
	</dependency>
	```

2. resources/application.yml:
	```yml
	spring:
	  datasource:
	    url: jdbc:mysql://localhost:3306/demo1?characterEncoding=utf8&useSSL=false
	    username: cj
	    password: 123
	    driver-class-name: com.mysql.jdbc.Driver
	  jpa:
	    database: mysql
	    show-sql: true
	    open-in-view: false
	    properties:
	      hibernate.format_sql: true
	    hibernate:
	      naming:
	        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
	  jackson:
	    serialization:
	      FAIL_ON_EMPTY_BEANS: false  
	    default-property-inclusion: NON_EMPTY
	```

3. entity:
	+ Employee
	+ Department

4. repository:
	+ EmployeeRepository
	+ EmployeeRepositoryCustom
	+ EmployeeRepositoryImpl
	+ DepartmentRepository

5. service:
	+ EmployeeService
	+ DepartmentService

6. util:
	+ FetchEntity
	+ SearchCondition
	+ SpecificationBuildUtils

7. test(service):
	+ EmployeeServiceTest
	+ DeploymentServiceTest

