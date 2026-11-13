package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.salary.common.ConflictException;
import com.acme.salary.common.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    private static final String VALID_BODY = """
            {"employeeCode":"EMP10001","fullName":"Asha Rao","email":"asha@acme.com",
             "jobTitle":"Software Engineer","department":"Engineering","country":"IN",
             "salary":2400000,"hireDate":"2024-04-01"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EmployeeService service;

    @Test
    void invalidRequestReturns400WithFieldErrors() throws Exception {
        String body = """
                {"employeeCode":"","fullName":"Asha","email":"not-an-email","jobTitle":"Dev",
                 "department":"Eng","country":"IN","salary":-5,"hireDate":"2024-04-01"}
                """;

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.employeeCode").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.salary").exists());
        verify(service, never()).create(any());
    }

    @Test
    void unknownCountryIsRejected() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("\"IN\"", "\"XX\"")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void duplicateReturns409() throws Exception {
        when(service.create(any())).thenThrow(new ConflictException("Email asha@acme.com is already in use"));

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Email asha@acme.com is already in use"));
    }

    @Test
    void missingEmployeeReturns404() throws Exception {
        when(service.get(42L)).thenThrow(new NotFoundException("Employee 42 not found"));

        mvc.perform(get("/api/employees/42")).andExpect(status().isNotFound());
    }

    @Test
    void searchPassesFiltersToService() throws Exception {
        mvc.perform(get("/api/employees").param("search", "asha").param("country", "IN").param("size", "50"))
                .andExpect(status().isOk());

        verify(service).search(eq(new EmployeeFilter("asha", com.acme.salary.country.Country.IN, null, null)),
                any(Pageable.class));
    }

    @Test
    void pageableClampsSizeAndIgnoresUnknownSortFields() {
        Pageable pageable = EmployeeController.pageable(-3, 10_000, "password", "desc");

        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(EmployeeController.MAX_PAGE_SIZE);
        assertThat(pageable.getSort().getOrderFor("fullName")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("fullName").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
