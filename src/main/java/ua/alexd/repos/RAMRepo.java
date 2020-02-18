package ua.alexd.repos;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import ua.alexd.domain.RAM;

public interface RAMRepo extends CrudRepository<RAM, Integer>, JpaSpecificationExecutor<RAM> { }