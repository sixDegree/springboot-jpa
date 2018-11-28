package com.cj.demo.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuildUtils {
	
	public static <T> Specification<T> build(SearchCondition...conditions){
		return build(Arrays.asList(conditions));
	}
	
	public static <T> Specification<T> build(List<SearchCondition> conditions,FetchEntity...fetchs){
		if(conditions==null || conditions.size()==0)
			return null;
		
//		List<Specification<T>> specificationList=new ArrayList<Specification<T>>();
//		if(fetchs!=null && fetchs.length>0) {
//			Specification<T> spec=new Specification<T>() {
//				@Override
//				public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//					if(!Long.class.equals(query.getResultType())) {
//						for(FetchEntity f:fetchs) {
//							if(f.getJoinType()!=null)
//								root.fetch(f.getAttribute(),f.getJoinType());
//							else
//								root.fetch(f.getAttribute());
//						}
//					}
//					return null;
//				}
//			};
//			specificationList.add(spec);
//		}
//		
//		for(SearchCondition condition:conditions) {
//			Specification<T> spec=new Specification<T>() {
//				@SuppressWarnings("unchecked")
//				@Override
//				public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//
//					String[] keys=condition.getKey().split(Pattern.quote("."));
//					Path path=root.get(keys[0]);
//					for(int i=1;i<keys.length;i++)
//						path=path.get(keys[i]);
//					
//					if("=".equals(condition.getOperation()))
//						return criteriaBuilder.equal(path, condition.getValue());
//					if("!=".equals(condition.getOperation()))
//						return criteriaBuilder.notEqual(path, condition.getValue());
//					if(":".equals(condition.getOperation()))
//						return criteriaBuilder.like(path, condition.getValue().toString());
//					if("!".equals(condition.getOperation()))
//						return criteriaBuilder.isNotNull(path);
//					if(">".equals(condition.getOperation()))
//						return criteriaBuilder.greaterThan(path, condition.getValue().toString());
//					if(">=".equals(condition.getOperation()))
//						return criteriaBuilder.greaterThanOrEqualTo(path, condition.getValue().toString());
//					if("<".equals(condition.getOperation()))
//						return criteriaBuilder.lessThan(path, condition.getValue().toString());
//					if("<=".equals(condition.getOperation()))
//						return criteriaBuilder.lessThanOrEqualTo(path, condition.getValue().toString());
//					return null;
//				}
//			};
//			specificationList.add(spec);
//		}
//		
//		if(specificationList.size()==0)
//			return null;
//		
//		Specification<T> spec=specificationList.get(0);
//		for(int i=1;i<specificationList.size();i++) {
//			spec=spec.and(specificationList.get(i));
//		}
		
		Specification<T> spec=new Specification<T>() {

			private static final long serialVersionUID = 4755495056489389930L;

			@Override
			public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
				List<Predicate> predicateList=new ArrayList<Predicate>();
				
				if(!Long.class.equals(query.getResultType()) && fetchs!=null && fetchs.length>0) {
					for(FetchEntity f:fetchs) {
						if(f.getJoinType()!=null)
							root.fetch(f.getAttribute(),f.getJoinType());
						else
							root.fetch(f.getAttribute());
					}
				}
					
				for(SearchCondition condition:conditions) {
					String[] keys=condition.getKey().split(Pattern.quote("."));
					Path<String> path=root.get(keys[0]);
					for(int i=1;i<keys.length;i++)
						path=path.get(keys[i]);
					
					Predicate predicate=null;
					if("=".equals(condition.getOperation()))
						predicate=criteriaBuilder.equal(path, condition.getValue());
					else if("!=".equals(condition.getOperation()))
						predicate=criteriaBuilder.notEqual(path, condition.getValue());
					else if(":".equals(condition.getOperation()))
						predicate=criteriaBuilder.like(path, condition.getValue().toString());
					else if("!".equals(condition.getOperation()))
						predicate=criteriaBuilder.isNotNull(path);
					else if(">".equals(condition.getOperation()))
						predicate=criteriaBuilder.greaterThan(path, condition.getValue().toString());
					else if(">=".equals(condition.getOperation()))
						predicate=criteriaBuilder.greaterThanOrEqualTo(path, condition.getValue().toString());
					else if("<".equals(condition.getOperation()))
						predicate=criteriaBuilder.lessThan(path, condition.getValue().toString());
					else if("<=".equals(condition.getOperation()))
						predicate=criteriaBuilder.lessThanOrEqualTo(path, condition.getValue().toString());
					
					if(predicate!=null)
						predicateList.add(predicate);
				}
				
				if(predicateList.size()!=0) {
					Predicate[] predicateArray = new Predicate[predicateList.size()];
					query.where(predicateList.toArray(predicateArray));
					return query.getRestriction();
				}
				return null;
			}
		};
		
		return spec;
	}
	
}
