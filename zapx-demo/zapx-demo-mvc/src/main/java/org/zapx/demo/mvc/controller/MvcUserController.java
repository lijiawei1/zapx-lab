package org.zapx.demo.mvc.controller;

import org.springframework.web.bind.annotation.*;
import org.zapx.demo.mvc.entity.MvcUser;

import java.util.*;

/**
 * Created by Administrator on 2017/9/9 0009.
 */
@RestController
@RequestMapping(value = "/users")
public class MvcUserController {


    // 创建线程安全的Map 
    static Map<String, MvcUser> MvcUsers = Collections.synchronizedMap(new HashMap<String, MvcUser>());

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<MvcUser> getMvcUserList() {
        // 处理"/MvcUsers/"的GET请求，用来获取用户列表 
        // 还可以通过@RequestParam从页面中传递参数来进行查询条件或者翻页信息的传递 
        List<MvcUser> r = new ArrayList<MvcUser>(MvcUsers.values());
        return r;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String postMvcUser(@ModelAttribute MvcUser MvcUser) {
        // 处理"/MvcUsers/"的POST请求，用来创建MvcUser 
        // 除了@ModelAttribute绑定参数之外，还可以通过@RequestParam从页面中传递参数 
        MvcUsers.put(MvcUser.getId(), MvcUser);
        return "success";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public MvcUser getMvcUser(@PathVariable String id) {
        // 处理"/MvcUsers/{id}"的GET请求，用来获取url中id值的MvcUser信息 
        // url中的id可通过@PathVariable绑定到函数的参数中 
        return MvcUsers.get(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String putMvcUser(@PathVariable String id, @ModelAttribute MvcUser MvcUser) {
        // 处理"/MvcUsers/{id}"的PUT请求，用来更新MvcUser信息 
        MvcUser u = MvcUsers.get(id);
        u.setName(MvcUser.getName());
        u.setAge(MvcUser.getAge());
        MvcUsers.put(id, u);
        return "success";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String deleteMvcUser(@PathVariable String id) {
        // 处理"/MvcUsers/{id}"的DELETE请求，用来删除MvcUser 
        MvcUsers.remove(id);
        return "success";
    }
}
