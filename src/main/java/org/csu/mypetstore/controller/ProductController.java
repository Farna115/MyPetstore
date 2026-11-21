package org.csu.mypetstore.controller;

import org.csu.mypetstore.service.CategoryService;
import org.csu.mypetstore.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面视图控制器：仅把 URL 映射到 Thymeleaf 模板并组装 Model，
 * 查询规则全部留在 Service 层。URL 统一使用小写、不带 .html 后缀。
 */
@Controller
public class ProductController {

    private final CategoryService categoryService;
    private final ProductService productService;

    // 构造器注入，遵循工程现有分层与依赖注入规范
    public ProductController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    /** 欢迎入口：/ 和 /index 都返回 index 模板。 */
    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    /** 商城首页：读取分类导航、新品与精选栏目后返回 Main 模板。 */
    @GetMapping("/main")
    public String main(Model model) {
        model.addAttribute("categoryNavigation", categoryService.getHomeNavigation());
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        return "Main";
    }
}
