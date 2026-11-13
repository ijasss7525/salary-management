package com.acme.salary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Boots the whole app on a seeded in-memory SQLite database and drives it through HTTP, like the UI does. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:sqlite:file:it-testdb?mode=memory&cache=shared",
        "app.seed.enabled=true",
        "app.seed.count=300"})
@AutoConfigureMockMvc
class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void seededDataIsPagedAndSummarised() throws Exception {
        mvc.perform(get("/api/employees").param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(25))
                .andExpect(jsonPath("$.totalElements").value(300));

        mvc.perform(get("/api/insights/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headcount").value(300))
                .andExpect(jsonPath("$.countries").value(8));

        mvc.perform(get("/api/insights/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stats.headcount").isNumber());

        mvc.perform(get("/api/insights/countries/IN/job-titles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].stats.median").isNumber());

        mvc.perform(get("/api/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries.length()").value(8));
    }

    @Test
    void createUpdateAndDeleteAnEmployee() throws Exception {
        String body = """
                {"employeeCode":"NEW-1","fullName":"Asha Rao","email":"asha.new@acme.com",
                 "jobTitle":"Software Engineer","department":"Engineering","country":"IN",
                 "salary":2400000,"hireDate":"2024-04-01"}
                """;
        String created = mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("INR"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(created).get("id").asLong();

        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mvc.perform(put("/api/employees/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("2400000", "2600000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.salary").value(2600000));

        JsonNode found = json.readTree(mvc.perform(get("/api/employees").param("search", "asha.new"))
                .andReturn().getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(found.get("totalElements").asLong()).isEqualTo(1);

        mvc.perform(delete("/api/employees/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/" + id)).andExpect(status().isNotFound());
    }
}
