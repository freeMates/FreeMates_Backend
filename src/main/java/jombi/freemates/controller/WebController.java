package jombi.freemates.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
  @GetMapping("/")
  public String index() {
    return "pages/login"; // templates/pages/login.html
  }

  @GetMapping("/login")
  public String login() {
    return "pages/login"; // templates/pages/login.html
  }

  @GetMapping("/error")
  public String error() {
    return "pages/error"; // templates/pages/error.html
  }
}
