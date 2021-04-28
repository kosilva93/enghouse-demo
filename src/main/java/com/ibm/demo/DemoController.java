package com.ibm.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class DemoController {

  @GetMapping
  public ResponseEntity<String> helloWorld(){
   return ResponseEntity.ok("Hello World");
  }
}