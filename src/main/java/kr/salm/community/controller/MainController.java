package kr.salm.community.controller;

import kr.salm.community.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 메인 페이지 컨트롤러
 */
@Controller
@RequiredArgsConstructor
public class MainController {

    private final PostService postService;

    /**
     * 홈페이지
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("latestPosts", postService.findLatest(6));
        model.addAttribute("popularPosts", postService.findPopular(4));
        return "index";
    }

    /**
     * 카테고리 페이지
     */
    @GetMapping("/category/{category}")
    public String category(@PathVariable String category, Model model) {
        model.addAttribute("category", category);
        model.addAttribute("posts", postService.findByCategory(category, 0, 20));
        return "community/posts";
    }
}
