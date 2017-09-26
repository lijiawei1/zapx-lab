package org.zapx.demo.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Shin on 2017/9/26.
 */
@SpringBootApplication
@RestController
@RequestMapping("demo/dao")
public class DemoDaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoDaoApplication.class);
    }

    @Autowired
    JdbcTemplate template;

    @GetMapping("/testDao")
    @ResponseBody
    public Object testDao() {
        return template.queryForList("SELECT * FROM EMP");
    }
}
