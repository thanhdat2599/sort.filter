# Sort and Filter Library for Spring Boot

This library provides common sorting, filtering, and pagination utilities for **Spring Boot** applications, helping you simplify querying with **JPA Specifications**.

---
## Setup Instructions

### 1.Add the Dependency

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>vn.cnj.shared</groupId>
    <artifactId>sortfilter</artifactId>
    <version>1.0.4</version>
</dependency>

### 2.How to Use

Here is an example of how to use the library to implement searching, filtering, sorting, and pagination in a Spring Boot application:

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import vn.cnj.shared.specifications.SearchRequest;
import vn.cnj.shared.specifications.SearchSpecification;

public Page<LogDto> search(SearchRequest request) {
    // Create a JPA Specification based on the search request
    Specification<Log> specification = new SearchSpecification<>(request);
    
    // Create a Pageable object from request parameters
    Pageable pageable = SearchSpecification.getPageable(request.getPage(), request.getSize());
    
    // Execute the query with the specification and pagination
    Page<Log> entities = logRepository.findAll(specification, pageable);
    
    // Map entities to DTOs using a mapper (e.g., LogMapper)
    return entities.map(LogMapper.INSTANCE::toDto);
}

