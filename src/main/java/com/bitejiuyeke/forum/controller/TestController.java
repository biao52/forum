package com.bitejiuyeke.forum.controller;

import com.bitejiuyeke.forum.common.AppResult;
import com.bitejiuyeke.forum.exception.ApplicationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;

/**
 * @Author 比特就业课
 */
@Api(tags = "测试类的相关接口")
//  表示返回的结果是数据
@RestController
// 定义一级映射路径
@RequestMapping("/test")
public class TestController {

    @ApiOperation("测试接口1，显示你好Spring boot")
    @GetMapping("/hello")
    public String hello () {
        return "hello, Spring boot...";
    }

    @ApiOperation("测试接口4，按传入的姓名显示你好信息")
    @PostMapping("/helloByName")
    public AppResult helloByName (@ApiParam(value = "姓名") @RequestParam("name") @NonNull String name) {
        return AppResult.success("hello : " + name);
    }


    @ApiOperation("测试接口2，显示抛出的异常信息")
    @GetMapping("/exception")
    public AppResult testException () throws Exception {
        throw new Exception("这是一个Exception...");
    }

    @ApiOperation("测试接口2，显示抛出的自定义的异常信息")
    @GetMapping("/appException")
    public AppResult testApplicationException () {
        throw new ApplicationException("这是一个ApplicationException...");
    }
}
