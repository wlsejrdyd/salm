package kr.salm.community.controller;

import jakarta.validation.Valid;
import kr.salm.auth.entity.User;
import kr.salm.auth.service.AuthUtil;
import kr.salm.community.dto.VideoUploadRequest;
import kr.salm.community.entity.Video;
import kr.salm.community.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("categories", categoryService.findAll());
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("videos", videoService.search(keyword, page, 12));
            model.addAttribute("keyword", keyword);
        } else {
            model.addAttribute("videos", videoService.findAll(page, 12));
        }
        return "community/videos";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User user = AuthUtil.getCurrentUser();
        model.addAttribute("video", videoService.getDetail(id, user));
        return "community/video-detail";
    }

    @GetMapping("/upload")
    public String uploadForm(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("request", new VideoUploadRequest());
        return "community/upload";
    }

    @PostMapping("/upload")
    public String upload(@Valid @ModelAttribute("request") VideoUploadRequest request,
                        BindingResult bindingResult,
                        @RequestParam("videoFile") MultipartFile videoFile,
                        RedirectAttributes redirectAttributes, Model model) {
        
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "community/upload";
        }

        try {
            Video video = videoService.upload(request, videoFile, user);
            redirectAttributes.addFlashAttribute("message", "영상이 업로드되었습니다.");
            return "redirect:/videos/" + video.getId();
        } catch (Exception e) {
            log.error("업로드 실패: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "community/upload";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        
        videoService.delete(id, user);
        redirectAttributes.addFlashAttribute("message", "영상이 삭제되었습니다.");
        return "redirect:/";
    }
}
