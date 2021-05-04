package com.ibm.demo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "Customers")
public class Customer {

    @Id
    @GeneratedValue(generator = "customers_id_generator")
    @SequenceGenerator(name = "customers_id_generator", allocationSize = 1, initialValue = 10)
    private long id;
    private String firstName;
    private String location;

    Customer() {}

    public Customer(String firstName, String location) {
        this.firstName = firstName;
        this.location = location;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLocation() {
        return location;
    }
}
