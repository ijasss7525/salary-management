package com.acme.salary.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The Angular build is served from the same jar. Browser routes such as /employees are Angular routes,
 * so a page refresh on them must return index.html instead of a 404.
 */
@Controller
public class SpaForwardingController {

    @GetMapping({"/employees", "/employees/**", "/insights", "/insights/**"})
    public String forward() {
        return "forward:/index.html";
    }
}
