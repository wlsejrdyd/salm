package kr.salm.auth.controller;

import jakarta.validation.Valid;
import kr.salm.auth.dto.SignupRequest;
import kr.salm.auth.service.AuthService;
import kr.salm.core.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("request", new SignupRequest());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("request") SignupRequest request,
                        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            authService.signup(request);
            return "redirect:/login?signup=success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }

    @GetMapping("/api/auth/check/username")
    @ResponseBody
    public ResponseEntity<ApiResponse<Boolean>> checkUsername(@RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.success(authService.isUsernameAvailable(value)));
    }

    @GetMapping("/api/auth/check/email")
    @ResponseBody
    public ResponseEntity<ApiResponse<Boolean>> checkEmail(@RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.success(authService.isEmailAvailable(value)));
    }

    @GetMapping("/api/auth/check/nickname")
    @ResponseBody
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String value) {
        return ResponseEntity.ok(ApiResponse.success(authService.isNicknameAvailable(value)));
    }
}
