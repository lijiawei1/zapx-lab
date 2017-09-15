package org.zapx.demo.mvc.controller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/9 0009.
 */
@SpringBootApplication
@RestController
@RequestMapping("demo/mvc")
public class DemoMvcController {

    public static void main(String[] args) {
        SpringApplication.run(DemoMvcController.class);
    }

    @GetMapping("/testMvc")
    @ResponseBody
    public Object testMvc() {
        Map<String, String> obj = new HashMap<>();
        obj.put("akey", "avalue");
        obj.put("bkey", "bvalue");
        obj.put("ckey", "cvalue");
        return obj;
    }

}
