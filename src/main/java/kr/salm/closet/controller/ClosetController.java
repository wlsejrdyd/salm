package kr.salm.closet.controller;

import kr.salm.auth.entity.User;
import kr.salm.auth.service.AuthUtil;
import kr.salm.closet.service.ClothCategoryService;
import kr.salm.closet.service.ClothService;
import kr.salm.closet.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/closet")
@RequiredArgsConstructor
public class ClosetController {

    private final ClothService clothService;
    private final ClothCategoryService categoryService;
    private final WeatherService weatherService;

    @GetMapping
    public String index(Model model) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("weather", weatherService.getWeather(37.5665, 126.9780));
        model.addAttribute("clothes", clothService.findByUser(user, 0, 20));
        return "closet/index";
    }

    @GetMapping("/wardrobe")
    public String wardrobe(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(required = false) String category,
                          Model model) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.findAll());
        
        if (category != null && !category.isBlank()) {
            model.addAttribute("clothes", clothService.findByCategoryPaged(user, category, page, 20));
            model.addAttribute("currentCategory", category);
        } else {
            model.addAttribute("clothes", clothService.findByUser(user, page, 20));
        }
        return "closet/wardrobe";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.findAll());
        return "closet/add";
    }

    @GetMapping("/outfit")
    public String outfit(Model model) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        var weather = weatherService.getWeather(37.5665, 126.9780);
        model.addAttribute("weather", weather);
        model.addAttribute("outfit", clothService.getRandomOutfit(user, weather.getTemperature()));
        model.addAttribute("categories", categoryService.findAll());
        return "closet/outfit";
    }

    @GetMapping("/avatar")
    public String avatar(Model model) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("clothes", clothService.findByUser(user, 0, 100));
        return "closet/avatar";
    }
}
