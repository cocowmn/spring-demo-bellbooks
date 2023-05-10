package com.example.demo.service;

import com.example.demo.lib.validation.ValidationFailedException;
import com.example.demo.lib.validation.Validator;
import com.example.demo.model.Facility;
import com.example.demo.repository.FacilityRepository;
import com.google.common.base.Strings;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FacilityService implements Validator<Facility> {

    private final FacilityRepository facilityRepository;

    public FacilityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    public boolean isValid(Facility facility) {
        return !(
            Strings.isNullOrEmpty(facility.getName())
                || Strings.isNullOrEmpty(facility.getAddressLine1())
                || facility.getAddressLine1().length() > 100
                || (!Strings.isNullOrEmpty(facility.getAddressLine2()) && facility.getAddressLine2().length() > 100)
                || Strings.isNullOrEmpty(facility.getCity())
                || facility.getCity().length() > 100
                || Strings.isNullOrEmpty(facility.getZip())
                || facility.getZip().length() > 10
                || facility.getState() == null
        );
    }

    public void validate(Facility facility) throws ValidationFailedException {
        if (!isValid(facility)) throw new ValidationFailedException();
    }

    public void validateAll(List<Facility> facilities) throws ValidationFailedException {
        for (Facility facility : facilities) validate(facility);
    }

    public Optional<Facility> getFacility(UUID facilityId) {
        return facilityRepository.findById(facilityId);
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAllByOrderByName();
    }

    public Facility createFacility(Facility facility) throws ValidationFailedException {
        validate(facility);
        return facilityRepository.save(facility);
    }

    public List<Facility> createFacilities(List<Facility> facilities) throws ValidationFailedException {
        validateAll(facilities);
        return (List<Facility>) facilityRepository.saveAll(facilities);
    }

    public Facility updateFacility(Facility existingFacility, Facility updates) throws ValidationFailedException {
        Facility compositeFacility = Facility.builder()
            .id(existingFacility.getId())
            .name(Optional.ofNullable(updates.getName()).orElse(existingFacility.getName()))
            .addressLine1(Optional.ofNullable(updates.getAddressLine1()).orElse(existingFacility.getAddressLine1()))
            .addressLine2(Optional.ofNullable(updates.getAddressLine2()).orElse(existingFacility.getAddressLine2()))
            .city(Optional.ofNullable(updates.getCity()).orElse(existingFacility.getCity()))
            .state(Optional.ofNullable(updates.getState()).orElse(existingFacility.getState()))
            .zip(Optional.ofNullable(updates.getZip()).orElse(existingFacility.getZip()))
            .additionalInfo(Optional.ofNullable(updates.getAdditionalInfo()).orElse(existingFacility.getAdditionalInfo()))
            .build();

        validate(compositeFacility);
        return facilityRepository.save(compositeFacility);
    }

    public void deleteFacility(UUID facilityId) {
        facilityRepository.deleteById(facilityId);
    }

    public void deleteFacilities(List<UUID> facilityIds) {
        facilityRepository.deleteAllById(facilityIds);
    }

    public void deleteAllFacilities() {
        this.facilityRepository.deleteAll();
    }
}
