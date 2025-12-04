package kr.salm.community.controller;

import kr.salm.community.service.CategoryService;
import kr.salm.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("latestPosts", postService.findLatest(6));
        model.addAttribute("popularPosts", postService.findPopular(4));
        return "index";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentCategory", categoryService.findBySlug(slug));
        model.addAttribute("posts", postService.findByCategory(slug, 0, 20));
        return "community/posts";
    }
}
