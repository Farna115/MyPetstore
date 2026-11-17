package org.csu.mypetstore.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.csu.mypetstore.dto.UserReadResult;
import org.csu.mypetstore.service.UserService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * dev 环境专用的用户查询演示接口：只调用 Service，不直接触库、不返回实体。
 */
@Controller
@Profile("dev")
public class UserDemoController {

    private final UserService userService;

    // 构造器注入
    public UserDemoController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/demo/user")
    @ResponseBody
    public ResponseEntity<UserReadResult> user(
            @RequestParam(value = "username", defaultValue = "alice") String username) {
        // 存在：200 + UserReadResult JSON；不存在：404。
        // 不捕获数据访问异常——异常继续抛出，由 Spring 默认返回 500，而不是误报 404。
        Optional<UserReadResult> result = userService.findByUsername(username);
        return result
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * 页面演示：无 @ResponseBody，返回的字符串是视图名，由 Thymeleaf 渲染。
     * 复用同一个 UserService 查询，不重复实现查库逻辑。
     */
    @GetMapping("/demo/user/page")
    public String userPage(
            @RequestParam(value = "username", defaultValue = "alice") String username,
            Model model, HttpServletResponse response) {
        Optional<UserReadResult> result = userService.findByUsername(username);
        model.addAttribute("username", username);
        if (result.isPresent()) {
            model.addAttribute("message", "Thymeleaf初始化成功！");
            model.addAttribute("user", result.get());
        } else {
            // 查不到：状态码 404，页面提示不存在；数据访问异常仍然照常向上抛
            response.setStatus(HttpStatus.NOT_FOUND.value());
            model.addAttribute("message", "用户不存在：" + username);
        }
        return "demo/user";
    }
}
