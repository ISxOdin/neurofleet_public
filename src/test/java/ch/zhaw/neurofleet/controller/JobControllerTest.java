package ch.zhaw.neurofleet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.zhaw.neurofleet.model.Job;
import ch.zhaw.neurofleet.model.JobCreateDTO;
import ch.zhaw.neurofleet.model.JobState;
import ch.zhaw.neurofleet.repository.JobRepository;
import ch.zhaw.neurofleet.security.TestSecurityConfig;
import ch.zhaw.neurofleet.service.JobService;
import ch.zhaw.neurofleet.service.UserService;
import static ch.zhaw.neurofleet.security.Roles.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@TestMethodOrder(OrderAnnotation.class)
class JobControllerTest {

        @Autowired
        private MockMvc mvc;

        @MockitoBean
        private JobRepository jobRepository;

        @MockitoBean
        private JobService jobService;

        @MockitoBean
        private UserService userService;

        private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        private static final String JOB_ID = "job-123";
        private static final String COMPANY_ID = "comp-1";
        private static final String ORIGIN_ID = "loc-a";
        private static final String DESTINATION_ID = "loc-b";
        private static final String VEHICLE_ID = "veh-1";

        private Job baseJob;

        @BeforeEach
        void setup() {
                baseJob = new Job("Test delivery", LocalDateTime.now(), ORIGIN_ID, DESTINATION_ID, VEHICLE_ID,
                                COMPANY_ID);
                baseJob.setId(JOB_ID);

                when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(baseJob));
                when(jobRepository.existsById(JOB_ID)).thenReturn(true);
                when(jobRepository.save(any())).thenReturn(baseJob);
                when(jobRepository.findAll(PageRequest.of(0, 5))).thenReturn(new PageImpl<>(List.of(baseJob)));
        }

        // Create Job Tests

        @Test
        @Order(1)
        void testCreateJob_AdminRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Transport X");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId(COMPANY_ID);
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                Job created = new Job(
                                dto.getDescription(),
                                dto.getScheduledTime(),
                                dto.getOriginId(),
                                dto.getDestinationId(),
                                dto.getVehicleId(),
                                dto.getCompanyId());
                created.setId(JOB_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobRepository.save(any())).thenReturn(created);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.description").value("Transport X"))
                                .andExpect(jsonPath("$.jobState").value("NEW"));
        }

        @Test
        @Order(2)
        void testCreateJob_OwnerRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Owner Job");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId(COMPANY_ID);
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                Job created = new Job(
                                dto.getDescription(),
                                dto.getScheduledTime(),
                                dto.getOriginId(),
                                dto.getDestinationId(),
                                dto.getVehicleId(),
                                dto.getCompanyId());
                created.setId(JOB_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobRepository.save(any())).thenReturn(created);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.description").value("Owner Job"));
        }

        @Test
        @Order(3)
        void testCreateJob_FleetmanagerRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Fleetmanager Job");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId(COMPANY_ID);
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                Job created = new Job(
                                dto.getDescription(),
                                dto.getScheduledTime(),
                                dto.getOriginId(),
                                dto.getDestinationId(),
                                dto.getVehicleId(),
                                dto.getCompanyId());
                created.setId(JOB_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobRepository.save(any())).thenReturn(created);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.description").value("Fleetmanager Job"));
        }

        @Test
        @Order(4)
        void testCreateJob_UnauthorizedRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Blocked");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId(COMPANY_ID);
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                                .content(json))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(5)
        void testGetAllJobs() throws Exception {
                when(jobRepository.findAll(PageRequest.of(0, 5)))
                                .thenReturn(new PageImpl<>(List.of(baseJob)));

                mvc.perform(get("/api/jobs?pageNumber=1&pageSize=5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(JOB_ID))
                                .andExpect(jsonPath("$.content[0].description").value("Test delivery"));
        }

        @Test
        @Order(6)
        void testGetJobById_Found() throws Exception {
                when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(baseJob));

                mvc.perform(get("/api/jobs/" + JOB_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(JOB_ID))
                                .andExpect(jsonPath("$.description").value("Test delivery"));
        }

        @Test
        @Order(7)
        void testGetJobById_NotFound() throws Exception {
                when(jobRepository.findById("invalid-id")).thenReturn(Optional.empty());

                mvc.perform(get("/api/jobs/invalid-id")
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(8)
        void testUpdateJob_AdminRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Updated Job");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId(COMPANY_ID);
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                Job updated = new Job(
                                dto.getDescription(), dto.getScheduledTime(), dto.getOriginId(), dto.getDestinationId(),
                                dto.getVehicleId(), dto.getCompanyId());
                updated.setId(JOB_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobService.updateJob(eq(JOB_ID), any(JobCreateDTO.class))).thenReturn(updated);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/" + JOB_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.description").value("Updated Job"));
        }

        @Test
        @Order(9)
        void testUpdateJob_UnauthorizedRole() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Blocked");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/" + JOB_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER)
                                .content(json))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(10)
        void testUpdateJob_NotFound() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Missing");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobService.updateJob(eq("not-found-id"), any(JobCreateDTO.class)))
                                .thenThrow(new NoSuchElementException());

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/not-found-id")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isNotFound());
        }

        @Test
        @Order(11)
        void testUpdateJob_SecurityException() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Wrong Access");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobService.updateJob(eq(JOB_ID), any(JobCreateDTO.class)))
                                .thenThrow(new SecurityException());

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/" + JOB_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(json))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(12)
        void testUpdateJob_InternalServerError() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Fails");

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobService.updateJob(eq(JOB_ID), any(JobCreateDTO.class)))
                                .thenThrow(new RuntimeException("Unexpected failure"));

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/" + JOB_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN)
                                .content(json))
                                .andExpect(status().isInternalServerError());
        }

        @Test
        @Order(13)
        void testDeleteJob_AdminRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);

                mvc.perform(delete("/api/jobs/" + JOB_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.ADMIN))
                                .andExpect(status().isOk())
                                .andExpect(content().string("DELETED"));
        }

        @Test
        @Order(14)
        void testDeleteJob_UnauthorizedRole() throws Exception {
                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(false);

                mvc.perform(delete("/api/jobs/" + JOB_ID)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.USER))
                                .andExpect(status().isForbidden());
        }

        @Test
        @Order(15)
        void testCreateJob_OwnerManipulatesCompanyId() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Hack Attempt");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId("evil-company");
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);

                Job created = new Job(dto.getDescription(), dto.getScheduledTime(), dto.getOriginId(),
                                dto.getDestinationId(), dto.getVehicleId(), COMPANY_ID);
                created.setId(JOB_ID);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(userService.userHasAnyRole(OWNER)).thenReturn(true);
                when(userService.getCompanyIdOfCurrentUser()).thenReturn(COMPANY_ID);
                when(jobRepository.save(any())).thenReturn(created);

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(post("/api/jobs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.OWNER)
                                .content(json))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.companyId").value(COMPANY_ID));
        }

        @Test
        @Order(16)
        void testUpdateJob_FleetmanagerWrongCompany() throws Exception {
                JobCreateDTO dto = new JobCreateDTO();
                dto.setDescription("Illegal fleet update");
                dto.setScheduledTime(LocalDateTime.now());
                dto.setCompanyId("wrong-company");
                dto.setOriginId(ORIGIN_ID);
                dto.setDestinationId(DESTINATION_ID);
                dto.setVehicleId(VEHICLE_ID);
                dto.setJobState(JobState.SCHEDULED);

                when(userService.userHasAnyRole(ADMIN, OWNER, FLEETMANAGER)).thenReturn(true);
                when(jobService.updateJob(eq(JOB_ID), any(JobCreateDTO.class)))
                                .thenThrow(new SecurityException("Job not accessible for fleetmanager"));

                String json = objectMapper.writeValueAsString(dto);

                mvc.perform(put("/api/jobs/" + JOB_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, TestSecurityConfig.FLEETMANAGER)
                                .content(json))
                                .andExpect(status().isForbidden());
        }

}
