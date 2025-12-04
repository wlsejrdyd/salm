package kr.salm.community.controller;

import jakarta.validation.Valid;
import kr.salm.auth.entity.User;
import kr.salm.auth.service.CustomUserDetails;
import kr.salm.community.dto.PostCreateRequest;
import kr.salm.community.entity.Post;
import kr.salm.community.service.CommentService;
import kr.salm.community.service.PostService;
import kr.salm.core.util.SecurityUtils;
import kr.salm.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * 게시글 웹 컨트롤러
 */
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final FileService fileService;

    /**
     * 게시글 목록
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String category,
                       Model model) {
        if (category != null && !category.isBlank()) {
            model.addAttribute("posts", postService.findByCategory(category, page, 20));
            model.addAttribute("category", category);
        } else {
            model.addAttribute("posts", postService.findAll(page, 20));
        }
        return "community/posts";
    }

    /**
     * 게시글 상세
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        User currentUser = SecurityUtils.getCurrentUser().orElse(null);
        model.addAttribute("post", postService.getDetail(id, currentUser));
        model.addAttribute("comments", commentService.findByPost(id));
        return "community/post-detail";
    }

    /**
     * 글쓰기 페이지
     */
    @GetMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String writeForm(Model model) {
        model.addAttribute("postRequest", new PostCreateRequest());
        return "community/write";
    }

    /**
     * 게시글 작성
     */
    @PostMapping("/write")
    @PreAuthorize("isAuthenticated()")
    public String create(@Valid @ModelAttribute("postRequest") PostCreateRequest request,
                         BindingResult bindingResult,
                         @RequestParam(required = false) MultipartFile[] images,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        if (bindingResult.hasErrors()) {
            return "community/write";
        }

        List<String> savedImages = fileService.saveFiles(images);
        Post post = postService.create(request, userDetails.getUser(), savedImages);

        redirectAttributes.addFlashAttribute("message", "게시글이 작성되었습니다.");
        return "redirect:/posts/" + post.getId();
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editForm(@PathVariable Long id, 
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model) {
        Post post = postService.findByIdWithoutView(id);
        
        if (!post.getAuthor().getId().equals(userDetails.getUserId()) && !userDetails.getUser().isAdmin()) {
            return "redirect:/posts/" + id;
        }

        model.addAttribute("post", post);
        return "community/edit";
    }

    /**
     * 게시글 수정
     */
    @PostMapping("/{id}/edit")
    @PreAuthorize("isAuthenticated()")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute PostCreateRequest request,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        
        postService.update(id, request, userDetails.getUser());
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/posts/" + id;
    }

    /**
     * 게시글 삭제
     */
    @PostMapping("/{id}/delete")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        
        postService.delete(id, userDetails.getUser());
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/posts";
    }
}
