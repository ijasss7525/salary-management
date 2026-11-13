package com.acme.salary.insights;

import com.acme.salary.country.Country;
import com.acme.salary.insights.InsightDtos.CountryInsight;
import com.acme.salary.insights.InsightDtos.JobTitleInsight;
import com.acme.salary.insights.InsightDtos.Overview;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
public class InsightsController {

    private final InsightsService service;

    public InsightsController(InsightsService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public Overview overview() {
        return service.overview();
    }

    @GetMapping("/countries")
    public List<CountryInsight> byCountry() {
        return service.byCountry();
    }

    @GetMapping("/countries/{country}/job-titles")
    public List<JobTitleInsight> byJobTitle(@PathVariable Country country) {
        return service.byJobTitle(country);
    }
}
