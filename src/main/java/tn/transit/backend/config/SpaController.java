package tn.transit.backend.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Order(Integer.MAX_VALUE)
public class SpaController {

    @GetMapping(value = {
            "/",
            "/vehicles/**",
            "/maintenance/**",
            "/lines/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}