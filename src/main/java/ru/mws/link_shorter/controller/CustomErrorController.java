package ru.mws.link_shorter.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
  private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

  @RequestMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

    logger.warn("Error occurred - Status: {}, Message: {}", status, message);

    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());

      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        model.addAttribute("errorTitle", "Ссылка не найдена");
        model.addAttribute("errorMessage", "Короткая ссылка не существует или была удалена");
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
        model.addAttribute("errorTitle", "Ошибка сервера");
        model.addAttribute("errorMessage", "Произошла внутренняя ошибка сервера");
      } else {
        model.addAttribute("errorTitle", "Произошла ошибка");
        model.addAttribute("errorMessage", message != null ? message : "Неизвестная ошибка");
      }

      model.addAttribute("statusCode", statusCode);
    }

    return "error";
  }
}
