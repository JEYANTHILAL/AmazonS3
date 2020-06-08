package com.example.aws.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.base.Strings;

import com.example.aws.services.UserInfo;

@Controller
public class DeleteAccountController extends AuthenticationBase {

    @GetMapping("/delete_account")
    public String deleteAccount(Model model,  HttpServletRequest request) {
        String nextPage = "delete_account";
        // Make sure that the delete account page cannot be referenced by someone who is not logged in
        UserInfo info = (UserInfo)request.getSession().getAttribute(USER_SESSION_ATTR);
        if (info == null) {
            nextPage = "index";
        }
        return nextPage;
    }
    
    @PostMapping("/delete_account_form")
    public String deleteAccountForm(@RequestParam("password") final String password,
                                    RedirectAttributes redirect,
                                    HttpServletRequest request ) {
        String nextPage = "redirect:delete_account";
        UserInfo info = (UserInfo)request.getSession().getAttribute(USER_SESSION_ATTR);
        if (info != null) {
            if (Strings.isNullOrEmpty(password)) {
                redirect.addFlashAttribute("password_error", "Please enter your password");
            } else {
                try {
                    String userName = info.getUserName();
                    authService.deleteUser(userName, password);
                    // The user's account has been deleted... So make sure that the session information is 
                    // gone as well.
                    request.getSession().setAttribute(USER_SESSION_ATTR, null);
                    nextPage = "redirect:/";
                } catch (Exception e) {
                    redirect.addFlashAttribute("password_error", e.getLocalizedMessage());
                }
            }
        } else {
            nextPage = "redirect:/";
        }
        return nextPage;
    }
    
}
