Deploying Spring Boot to an OpenShift Cluster using Tekton

Prerequisites
You will need the following to walk through this code example:

•	Java 8+
•	An IBM Cloud account
•	IBM Cloud Command Line Interface
•	An IDE of your choice

Initialize an OpenShift Cluster
We’ve already created the OpenShift cluster named wrm-pilot-enghouse. Can continue to next step.

Inside the cluster create a new project demo-application

Configure Database
1.	If you have not already log in to IBM cloud

2.	Once logged in, in the catalog, search for PostgreSQL

3.	Select the Databases for PostgreSQL option

4.	Change the name wrm-demo-db

The rest of the default values are fine for the purpose of the application we are building.

5.	Once database is provisioned, scroll down. Toward the bottom of the page, you should see a field labeled “TLS certificate,” which contains the self-signed certificate for the database that we can use to have secure communication between our application and the database.

6.	On your machine create a file called root.crt in ~/.postgresql (or %APPDATA%\postgresql, if you are using Windows) and copy every thing in the content

7.	On the left side of the screen, select the Service credentials option.

8.	From this “Service credentials” page we can create new credentials. Click the (blue) “New credential” button on the right side of the page.
The defaults are fine; though, if you want a more memorable name than “Service credentials-1,” feel free to change it.
9.	Once the credentials are created, expand View credentials, which should display JSON.

10.	Look for the composed value.

11.	Open a command line terminal, and copy and paste the values into your terminal, but don’t press Enter. You will want to remove the PGSSLROOTCERT value, as we already added the cert to the .postgresql folder earlier.

12.	With PGSSLROOTCERT removed, press Enter, and you should be signed in to your database.
From here we can configure the database.
13.	Let’s update the database to match the Customer JPA entity. The following sql statements should accomplish this:
1.	create sequence customers_id_generator start 10 increment 1;
2.	
3.	create table customers (id int8 not null, first_name varchar(255), last_name varchar(255), location varchar(255), company_name varchar(255), primary key (id));
4.	
5.	insert into customers (id, first_name, last_name, location, company_name) values (nextval('storms_id_generator'), 'Test', 'User', 'UK', 'Enghouse');
14.	Once all of the above statements have been run, you can log out of the database with the following command:
\q

15.	Next, sign in to IBM Cloud with the CLI tool, and point to your OpenShift cluster. You should be able to open you cluster dashboard and on top right corner you will find CLI-command to log in to your cluster

16.	Once signed in, run the following command to create a binding between the OpenShift cluster and the PostgreSQL database:
ibmcloud ks cluster service bind wrm-pilot-enghouse --service wrm-demo-db --namespace demo-application
17.	Last step on this section, change the directory to where you stored the root.crt file and run to create a secret with our certificate in order to connect the app to DB in our cluster
oc create secret generic cert-secret --from-file=./root.crt
Create Demo Application
Create a project on start.spring.io.

Name the project
Include the Web dependency
Open the project in your IDE.

Create a new class called DemoController and modified it. Here is what the completed class should look like

@RestController
@RequestMapping("/api/v1/customers")
public class DemoController {

	@Autowired
	private CustomerRepo repo;
    
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

Next create a Customer entity class and it should look like this:

@Entity
@Table(name = "Customers")
public class Customer {

    @Id
    @GeneratedValue(generator = "customers_id_generator")
    @SequenceGenerator(name = "customers_id_generator", allocationSize = 1, initialValue = 10)
    private long id;
    private String firstName;
    private String lastName;
    private String location;
    private String companyName;

    Customer() {}

    public Customer(String firstName, String lastName, String location, String companyName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.companyName = companyName;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
    	return lastName;
    }

    public String getLocation() {
        return location;
    }
    
    public String getCompanyName() {
        return companyName;
        }
    }

Now we should be able to create a CustomerRepo class and add the following:

@Repository
public interface CustomerRepo extends CrudRepository<Customer, Long> {
	public Iterable<Customer> findByLocation(String location);
}

Lastly update the application.properties with the statements below:

spring.datasource.url=jdbc:postgresql://${connection.postgres.hosts[0].hostname}:${connection.postgres.hosts[0].port}${connection.postgres.path}?sslmode=${connection.postgres.query_options.sslmode}
spring.datasource.username=${connection.postgres.authentication.username}
spring.datasource.password=${connection.postgres.authentication.password}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true
#^^^prevents a warning exception from being thrown. See: https://github.com/spring-projects/spring-boot/issues/12007
spring.jpa.open-in-view=false
#^^^suppresses warning exception related to OSIV https://vladmihalcea.com/the-open-session-in-view-anti-pattern/

Containerizing the Application
Create a file called Dockerfile in the root of the project directory and add the following:

FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/demo-enghouse.jar /

ENTRYPOINT ["java", "-jar", "demo-enghouse.jar" ]

In the pom.xml, under <build> in pom.xml, add the following:

<finalName>demo-enghouse</finalName>

Deploy to OpenShift
For this we are going to setup a tekton pipeline for our project. In order to do this you can follow this tutorial https://gudipatinrao.github.io/tekton-tutorial/

In the tutorial above it will ask you to clone the repo but instead clone the following repo since it has been modified to work with your application.

git clone https://github.com/kosilva93/tekton-setup.git

It will also ask you to provide an API key. In order to create an api key, make sure you are logged in to ibm account through you command line and run the following

ibmcloud iam api-key-create [API-token-name] -d “[token description]” –file [key_file_name]

When you open the file we just created with the previous command, you the contents should look something like this:

{
	"name": "apikey-demo",
  "description": "ApiKey used for demo",
  "apikey": "n6P30qr7efUhsHzoJFmwVAKGkT4o4wLCNrXzSBfDQPBZ",
  "locked": false,
  "entity_tag": "1-84792c11854ac35c4092abd391021904",
  "created_at": "2021-05-10T19:53+0000",
  "created_by": "IBMid-50T3S11YRC",
  "modified_at": "2021-05-10T19:53+0000"
}

Note: This file contains sensitive information so you will want to keep it secure and memborable location.

Once you clone this repository continue following the tutorial.

You should be able to deploy your application once you complete the tekton tutorial
