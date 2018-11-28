package com.cj.demo.repository;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import org.apache.commons.beanutils.PropertyUtils;

import com.cj.demo.entity.Employee;

public class EmployeeRepositoryImpl implements EmployeeRepositoryCustom<Employee,Integer>{

	@PersistenceContext
    private EntityManager em;
	
	@Override
	public boolean update(Employee entity, String... properties) {
		System.out.println("Update dynamic....");
		if(properties==null || properties.length==0)
			return false;
		
		try {
			EntityManagerFactory factory=em.getEntityManagerFactory();
			
			Object idValue=factory.getPersistenceUnitUtil().getIdentifier(entity);
			if(idValue==null)
				return false;
			
			Metamodel metamodel=factory.getMetamodel();
			EntityType<? extends Object> entityType=metamodel.entity(entity.getClass());
			
			String entityName = entityType.getJavaType().getSimpleName();
			String idProperty=entityType.getId(Integer.class).getName();
			
			String hql="Update "+entityName+" t set ";
			for(String property:properties) {
				if(property!=null && properties.length!=0)
					hql+="t."+property+"=?,";
			}
			hql=hql.substring(0,hql.length()-1)+" where t."+idProperty+"=?";

			Query query=em.createQuery(hql);
			for(int i=0;i<properties.length;i++) {
				String property=properties[i];
				query.setParameter(i, PropertyUtils.getProperty(entity, property));
			}
			query.setParameter(properties.length, idValue);
			return query.executeUpdate()>0;
			
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return false;
		}
	}

	@Override
	public int delete(Employee entity, String... properties) {
		System.out.println("Delete dynamic....");
		
		if(properties==null || properties.length==0)
			return 0;
		
		try {
			EntityManagerFactory factory=em.getEntityManagerFactory();

			Metamodel metamodel=factory.getMetamodel();
			EntityType<? extends Object> entityType=metamodel.entity(entity.getClass());
			
			String entityName = entityType.getJavaType().getSimpleName();
		
			String hql="delete from "+entityName+" t where ";
			for(String property:properties) {
				if(property!=null && properties.length!=0)
					hql+="t."+property+"=? and ";
			}
			hql=hql+" 1=1";

			Query query=em.createQuery(hql);
			for(int i=0;i<properties.length;i++) {
				String property=properties[i];
				query.setParameter(i, PropertyUtils.getProperty(entity, property));
			}
			return query.executeUpdate();
			
		}catch(Exception ex) {
			System.out.println(ex.getMessage());
			return 0;
		}
	}
	
}
