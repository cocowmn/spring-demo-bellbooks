package com.example.demo.controller.v1;

import com.example.demo.lib.validation.ValidationFailedException;
import com.example.demo.model.Facility;
import com.example.demo.service.FacilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * This class represents the controller for Facilities.
 * It provides basic Create, Read, Update, and Delete operations for facilities
 */
@APIv1
@CrossOrigin
@RestController
@RequestMapping("facility")
public class FacilityController {

    private final FacilityService facilityService;

    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    @GetMapping("{facilityId}")
    public ResponseEntity<Facility> getFacility(@PathVariable UUID facilityId) {
        return facilityService.getFacility(facilityId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("all")
    public ResponseEntity<List<Facility>> getAllFacilities() {
        return ResponseEntity.ok(facilityService.getAllFacilities());
    }

    @PostMapping("create")
    public ResponseEntity<Facility> createFacility(@RequestBody Facility facility) {
        try {
            return ResponseEntity.ok(facilityService.createFacility(facility));
        } catch (ValidationFailedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("create/batch")
    public ResponseEntity<List<Facility>> createFacilityBatch(@RequestBody List<Facility> facilities) {
        try {
            return ResponseEntity.ok(facilityService.createFacilities(facilities));
        } catch (ValidationFailedException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("{facilityId}")
    public ResponseEntity<Facility> updateFacility(@PathVariable UUID facilityId, @RequestBody Facility updates) {
        return facilityService.getFacility(facilityId)
            .map(facility -> {
                try {
                    Facility update = facilityService.updateFacility(facility, updates);
                    return ResponseEntity.ok(update);
                } catch (ValidationFailedException e) {
                    return ResponseEntity.badRequest().body((Facility) null);
                }
            }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("delete/{facilityId}")
    public ResponseEntity deleteFacility(@PathVariable UUID facilityId) {
        return facilityService.getFacility(facilityId)
            .map(facility -> {
                facilityService.deleteFacility(facilityId);
                return ResponseEntity.ok().build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("delete/batch")
    public ResponseEntity deleteFacilityBatch(@RequestBody List<UUID> facilityIds) {
        boolean anyFacilitiesMissing = facilityIds.stream()
            .map(facilityService::getFacility)
            .anyMatch(Optional::isEmpty);
        if (anyFacilitiesMissing) return ResponseEntity.notFound().build();

        facilityService.deleteFacilities(facilityIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("delete/all")
    public ResponseEntity deleteAllFacilities() {
        facilityService.deleteAllFacilities();
        return ResponseEntity.ok().build();
    }

}
