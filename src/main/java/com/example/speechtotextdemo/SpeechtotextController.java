package com.example.speechtotextdemo;

import com.example.speechtotextdemo.services.azurestt.AzureConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpeechtotextController {

    @Autowired
    AzureConfiguration azureConfiguration;

    @RequestMapping(value = "/")
    public String home() {
        return "home";
    }
}
