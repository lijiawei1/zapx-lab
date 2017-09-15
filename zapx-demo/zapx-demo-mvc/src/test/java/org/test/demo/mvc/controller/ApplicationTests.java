package org.test.demo.mvc.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.zapx.demo.mvc.controller.MvcUserController;
import org.zapx.web.tests.SimpleWebApplicationTests;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 简单测试
 *
 * Created by Administrator on 2017/9/9 0009.
 */
public class ApplicationTests extends SimpleWebApplicationTests {

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.standaloneSetup(new MvcUserController()).build();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMvcUserController() throws Exception {

//  	测试UserController
        RequestBuilder request = null;

        // 1、get查一下user列表，应该为空
        request = get("/users/");
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("[]")));

        // 2、post提交一个user
        request = post("/users/")
                .param("id", "1")
                .param("name", "测试大师")
                .param("age", "20");

        mvc.perform(request)
				.andDo(print())
                .andExpect(content().string(equalTo("success")));
    }

}
