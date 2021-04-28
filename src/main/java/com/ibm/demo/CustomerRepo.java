package com.ibm.demo;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepo extends CrudRepository<Customer, Long> {
	public Iterable<Customer> findByLocation(String location);
}