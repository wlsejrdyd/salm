package kr.salm.community.controller;

import kr.salm.community.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final VideoService videoService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("latestVideos", videoService.findLatest(8));
        model.addAttribute("popularVideos", videoService.findPopular(4));
        return "index";
    }

    @GetMapping("/category/{slug}")
    public String category(@PathVariable String slug,
                          @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("currentCategory", categoryService.findBySlug(slug));
        model.addAttribute("videos", videoService.findByCategory(slug, page, 12));
        return "community/videos";
    }
}
