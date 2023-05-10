package com.example.demo.repository;

import com.example.demo.model.Facility;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FacilityRepository extends CrudRepository<Facility, UUID> {

    List<Facility> findAllByOrderByName();

}
