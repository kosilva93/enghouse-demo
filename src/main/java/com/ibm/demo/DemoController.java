package com.ibm.demo;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class DemoController {

	@Autowired
	private CustomerRepo repo;

//    public DemoController(CustomerRepo repo) {
//        this.repo = repo;
//    }
    
    @GetMapping
    public ResponseEntity<Iterable<Customer>> findAllCustomers() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> findById(@PathVariable long customerId) {
        return ResponseEntity.ok(repo.findById(customerId).get());
    }
    
    @GetMapping("/search")
	public ResponseEntity<Iterable<Customer>> findByLocation(
			@RequestParam(name = "location") String location) {
		return ResponseEntity.ok(repo.findByLocation(location));
	}
    
    @PostMapping
    public ResponseEntity<?> addNewCustomer(@RequestBody Customer customer) {
    	customer = repo.save(customer);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + customer.getId())).build();
    }
}