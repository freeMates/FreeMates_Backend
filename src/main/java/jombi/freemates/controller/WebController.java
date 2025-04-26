package jombi.freemates.controller;

import me.suhsaechan.suhlogger.annotation.LogMonitor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  @GetMapping("/")
  @LogMonitor
  public String index(Model model) {
    model.addAttribute("title", "로그인 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/login";
  }

  @GetMapping("/login")
  @LogMonitor
  public String login(Model model) {
    model.addAttribute("title", "로그인 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/login";
  }

  @GetMapping("/error")
  @LogMonitor
  public String error(Model model) {
    model.addAttribute("title", "에러 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/error";
  }

  @GetMapping("/register")
  @LogMonitor
  public String signUpStep1(Model model) {
    model.addAttribute("title", "회원가입 - Step 1");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 1);
    return "pages/register";
  }

  // 메일 인증 성공 페이지
  @GetMapping("/mail/verification-confirm")
  public String showVerificationSuccess() {
    return "/mail/verificationConfirm";
  }

  // 메일 인증 실패 페이지
  @GetMapping("/mail/verification-fail")
  public String showVerificationFail() {
    return "/mail/verificationFail";
  }
}