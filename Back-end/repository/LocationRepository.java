package com.rental.rentalonline.repository;

import com.rental.rentalonline.model.Location;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends ElasticsearchRepository<Location, Long>, CustomLocationRepository {

}
