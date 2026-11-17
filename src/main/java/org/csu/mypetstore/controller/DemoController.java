package org.csu.mypetstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {

    // GET /demo：直接返回 UTF-8 纯文本，不经过数据库和模板渲染
    @GetMapping(value = "/demo", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String demo() {
        return "Spring MVC 初始化成功！";
    }
}
