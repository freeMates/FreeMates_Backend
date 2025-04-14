package jombi.freemates.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("title", "로그인 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/login";
  }

  @GetMapping("/login")
  public String login(Model model) {
    model.addAttribute("title", "로그인 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/login";
  }

  @GetMapping("/error")
  public String error(Model model) {
    model.addAttribute("title", "에러 - FreeMates");
    model.addAttribute("headerType", "default");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 0);
    return "pages/error";
  }

  @GetMapping("/sign-up-step1")
  public String signUpStep1(Model model) {
    model.addAttribute("title", "회원가입 - Step 1");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 1);
    return "pages/signUpStep1";
  }

  @GetMapping("/sign-up-step2")
  public String signUpStep2(Model model) {
    model.addAttribute("title", "회원가입 - Step 2");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 2);
    return "pages/signUpStep2";
  }

  @GetMapping("/sign-up-step3")
  public String signUpStep3(Model model) {
    model.addAttribute("title", "회원가입 - Step 3");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 3);
    return "pages/signUpStep3";
  }

  @GetMapping("/sign-up-step4")
  public String signUpStep4(Model model) {
    model.addAttribute("title", "회원가입 - Step 4");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 4);
    return "pages/signUpStep4";
  }

  @GetMapping("/sign-up-step5")
  public String signUpStep5(Model model) {
    model.addAttribute("title", "회원가입 - Step 5");
    model.addAttribute("headerType", "signUp");
    model.addAttribute("footerType", "default");
    model.addAttribute("step", 5);
    return "pages/signUpStep5";
  }
}