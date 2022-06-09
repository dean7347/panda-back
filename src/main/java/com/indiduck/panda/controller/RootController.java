package com.indiduck.panda.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RootController implements ErrorController {

    @RequestMapping("/error")
    public String handleError() {
        return "/index.html";
    }

    public String getErrorPath() {
        return "/error";
    }

}