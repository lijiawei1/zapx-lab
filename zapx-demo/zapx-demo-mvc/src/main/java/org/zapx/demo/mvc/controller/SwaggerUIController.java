package org.zapx.demo.mvc.controller;

import com.didispace.swagger.EnableSwagger2Doc;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.zapx.demo.mvc.controller.message.ResponseMessage;
import org.zapx.demo.mvc.entity.MvcUser;

/**
 * Created by Shin on 2017/9/26.
 */
@EnableSwagger2Doc
@SpringBootApplication
@RestController
@RequestMapping(value = "/demo/swaggerui")
public class SwaggerUIController {


    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)

    @ApiOperation(value = "测试SwaggerUI接口文档")
    @ApiResponses({
            @ApiResponse(code = 201, message = "创建成功,返回创建数据的ID"),
            @ApiResponse(code = 401, message = "未授权"),
            @ApiResponse(code = 403, message = "无权限")
    })
    public ResponseMessage add(@RequestBody MvcUser data) {
        return ResponseMessage.ok(data);

    }

    public static void main(String[] args) {
        SpringApplication.run(SwaggerUIController.class);
    }
}
