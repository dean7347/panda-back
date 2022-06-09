package com.indiduck.panda.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
@CrossOrigin
@Controller
public class CORSController {
    @GetMapping("/api/product/api/proxy")
    @ResponseBody
    public String proxy( @RequestParam(name = "url") String getproxy) {

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(getproxy.substring(1), String.class);
    }

}
