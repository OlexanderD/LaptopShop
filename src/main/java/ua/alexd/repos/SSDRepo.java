package ua.alexd.repos;

import org.springframework.data.repository.CrudRepository;
import ua.alexd.domain.SSD;

public interface SSDRepo extends CrudRepository<SSD, Long> {
}