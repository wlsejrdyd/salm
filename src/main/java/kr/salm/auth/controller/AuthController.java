package kr.salm.auth.controller;

import jakarta.validation.Valid;
import kr.salm.auth.dto.SignupRequest;
import kr.salm.auth.service.AuthService;
import kr.salm.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 웹용 인증 컨트롤러
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인 페이지
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (logout != null) {
            model.addAttribute("message", "로그아웃 되었습니다.");
        }
        return "auth/login";
    }

    /**
     * 회원가입 페이지
     */
    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/signup";
    }

    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest request,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        if (!request.isPasswordMatching()) {
            model.addAttribute("passwordError", "비밀번호가 일치하지 않습니다.");
            return "auth/signup";
        }

        try {
            authService.signup(request);
            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/signup/success";
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }

    /**
     * 회원가입 성공 페이지
     */
    @GetMapping("/signup/success")
    public String signupSuccess() {
        return "auth/signup-success";
    }
}
