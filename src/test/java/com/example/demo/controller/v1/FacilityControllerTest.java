package com.example.demo.controller.v1;

import com.example.demo.model.Facility;
import com.example.demo.repository.FacilityRepository;
import com.example.demo.service.FacilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.demo.controller.APIConfig.getV1RoutePrefix;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FacilityController.class)
@Import({FacilityService.class})
class FacilityControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    FacilityRepository repository;

    List<Facility> expectedFacilities;

    ObjectMapper jsonMapper = new ObjectMapper();

    static String apiRoute(String route) {
        return getV1RoutePrefix() + "/facility/" + route;
    }

    @BeforeEach
    void setUp() {
        expectedFacilities = ImmutableList.of(
            Facility.builder()
                .name("Facility 1")
                .addressLine1("123 Fake St")
                .city("Fake City")
                .state(Facility.State.NC)
                .zip("00000")
                .build(),
            Facility.builder()
                .name("Facility 2")
                .addressLine1("234 Unbelievable Ave")
                .city("35th of Nevuary")
                .state(Facility.State.AL)
                .zip("11111")
                .build()
        );
    }

    @Test
    void GET__facility__200OK__withFacility__whenFacilityExists() throws Exception {
        UUID expectedId = UUID.randomUUID();
        Facility expectedFacility = expectedFacilities.get(0);
        expectedFacility.setId(expectedId);
        when(repository.findById(eq(expectedId))).thenReturn(Optional.of(expectedFacility));

        this.mockMvc
            .perform(get(apiRoute(expectedId.toString())))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedFacility.toString()));
    }

    @Test
    void GET__facility__404NotFound__whenFacilityDoesNotExist() throws Exception {
        when(repository.findById(any())).thenReturn(Optional.empty());
        this.mockMvc
            .perform(get(apiRoute(UUID.randomUUID().toString())))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    public void GET__facility_all__200OK__withEmptyList__whenDatabaseEmpty() throws Exception {
        when(repository.findAllByOrderByName()).thenReturn(new ArrayList<>());
        this.mockMvc
            .perform(get(getV1RoutePrefix() + "/facility/all"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void GET__facility_all__200OK__withListOfFacilities__whenDatabaseContainsFacilities() throws Exception {
        when(repository.findAllByOrderByName()).thenReturn(expectedFacilities);
        this.mockMvc
            .perform(get(apiRoute("all")))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(jsonMapper.writeValueAsString(expectedFacilities)));
    }

    @Test
    void POST__facility_create__200OK__withFacility__whenCreateSuccessful() throws Exception {
        Facility expectedFacility = expectedFacilities.get(0);
        String expectedFacilityJSON = expectedFacility.toString();
        when(repository.save(any())).thenReturn(expectedFacility);

        this.mockMvc
            .perform(
                post(apiRoute("create"))
                    .contentType("application/json")
                    .content(expectedFacilityJSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedFacilityJSON));
    }

    @Test
    void POST__facility_create__400BadRequest__whenInvalidInputProvided() throws Exception {
        Facility expectedFacility = expectedFacilities.get(0);
        expectedFacility.setName(null); // this is what makes it invalid input
        String expectedFacilityJSON = expectedFacility.toString();

        this.mockMvc
            .perform(
                post(apiRoute("create"))
                    .contentType("application/json")
                    .content(expectedFacilityJSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void POST__facility_create_batch__200OK__withListOfFacilities__whenCreateSuccessful() throws Exception {
        String expectedFacilitiesJSON = jsonMapper.writeValueAsString(expectedFacilities);
        when(repository.saveAll(anyList())).thenReturn(expectedFacilities);
        this.mockMvc
            .perform(
                post(apiRoute("create/batch"))
                    .contentType("application/json")
                    .content(expectedFacilitiesJSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedFacilitiesJSON));
    }

    @Test
    void POST__facility_create_batch__400BadRequest__whenAnyItemIsInvalid() throws Exception {
        List<Facility> badFacilities = List.copyOf(expectedFacilities);
        badFacilities.get(0).setName(null); // this makes the list bad
        String badFacilitiesJSON = jsonMapper.writeValueAsString(badFacilities);

        this.mockMvc
            .perform(
                post(apiRoute("create/batch"))
                    .contentType("application/json")
                    .content(badFacilitiesJSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void PUT__facility__200OK__withFacility__whenUpdateSuccessful() throws Exception {
        UUID expectedId = UUID.randomUUID();
        Facility expectedExistingFacility = expectedFacilities.get(0);
        expectedExistingFacility.setId(expectedId);
        Facility expectedUpdates = Facility.builder()
            .name("Star trek")
            .addressLine1("227 Starcraft Blvd")
            .build();
        Facility expectedCompositeFacility = Facility.builder()
            .id(expectedId)
            .name(expectedUpdates.getName())
            .addressLine1(expectedUpdates.getAddressLine1())
            .city(expectedExistingFacility.getCity())
            .state(expectedExistingFacility.getState())
            .zip(expectedExistingFacility.getZip())
            .build();

        String updateJSON = jsonMapper.writeValueAsString(expectedUpdates);
        String resultJSON = expectedCompositeFacility.toString();

        when(repository.findById(eq(expectedId))).thenReturn(Optional.of(expectedExistingFacility));
        when(repository.save(refEq(expectedCompositeFacility))).thenReturn(expectedCompositeFacility);

        mockMvc
            .perform(
                put(apiRoute(expectedId.toString()))
                    .contentType("application/json")
                    .content(updateJSON)
            )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(resultJSON));
    }

    @Test
    void PUT__facility__404NotFound__whenFacilityDoesNotExistInDatabase() throws Exception {
        UUID expectedId = UUID.randomUUID();
        Facility expectedUpdates = Facility.builder()
            .name("Star trek")
            .addressLine1("227 Starcraft Blvd")
            .build();
        String updateJSON = expectedUpdates.toString();

        when(repository.findById(eq(expectedId))).thenReturn(Optional.empty());

        mockMvc
            .perform(
                put(apiRoute(expectedId.toString()))
                    .contentType("application/json")
                    .content(updateJSON)
            )
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void PUT__facility__400BadInput__whenUpdateValidationFails() throws Exception {
        UUID expectedId = UUID.randomUUID();
        Facility expectedExistingFacility = expectedFacilities.get(0);
        expectedExistingFacility.setId(expectedId);
        Facility expectedUpdates = Facility.builder()
            .zip(UUID.randomUUID().toString())
            .build();
        String updateJSON = expectedUpdates.toString();

        when(repository.findById(eq(expectedId))).thenReturn(Optional.of(expectedExistingFacility));

        mockMvc
            .perform(
                put(apiRoute(expectedId.toString()))
                    .contentType("application/json")
                    .content(updateJSON)
            )
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    void DELETE__facility__200OK__whenFacilityExistsInDatabase() throws Exception {
        UUID expectedId = UUID.randomUUID();
        Facility expectedFacility = expectedFacilities.get(0);
        expectedFacility.setId(expectedId);
        when(repository.findById(eq(expectedId))).thenReturn(Optional.of(expectedFacility));

        this.mockMvc
            .perform(delete(apiRoute("delete/" + expectedId)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void DELETE__facility__404NotFound__whenFacilityDoesNotExistInDatabase() throws Exception {
        UUID expectedId = UUID.randomUUID();
        when(repository.findById(any())).thenReturn(Optional.empty());

        this.mockMvc
            .perform(delete(apiRoute("delete/" + expectedId)))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void DELETE__facility_batch__200OK__whenAllFacilitiesExistInDatabase() throws Exception {
        UUID expectedId1 = UUID.randomUUID(), expectedId2 = UUID.randomUUID();
        String expectedFacilityJSON = jsonMapper.writeValueAsString(List.of(expectedId1, expectedId2));
        Facility expectedFacility1 = expectedFacilities.get(0),
            expectedFacility2 = expectedFacilities.get(1);
        expectedFacility1.setId(expectedId1);
        expectedFacility2.setId(expectedId2);

        when(repository.findById(eq(expectedId1))).thenReturn(Optional.of(expectedFacility1));
        when(repository.findById(eq(expectedId2))).thenReturn(Optional.of(expectedFacility2));

        this.mockMvc
            .perform(
                delete(apiRoute("delete/batch"))
                    .contentType("application/json")
                    .content(expectedFacilityJSON)
            )
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    void DELETE__facility_batch__404NotFound__whenAtLeastOneDoesNotExistInDatabase() throws Exception {
        UUID expectedId1 = UUID.randomUUID(), expectedId2 = UUID.randomUUID();
        String expectedFacilityJSON = jsonMapper.writeValueAsString(List.of(expectedId1, expectedId2));
        Facility expectedFacility1 = expectedFacilities.get(0);
        expectedFacility1.setId(expectedId1);

        when(repository.findById(eq(expectedId1))).thenReturn(Optional.of(expectedFacility1));
        when(repository.findById(eq(expectedId2))).thenReturn(Optional.empty());

        this.mockMvc
            .perform(
                delete(apiRoute("delete/batch"))
                    .contentType("application/json")
                    .content(expectedFacilityJSON)
            )
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    void DELETE__facility_all__200OK__whenDeleteSuccessful() throws Exception {
        this.mockMvc
            .perform(delete(apiRoute("delete/all")))
            .andDo(print())
            .andExpect(status().isOk());
    }

}