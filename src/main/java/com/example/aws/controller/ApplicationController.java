package com.example.aws.controller;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.aws.services.UserInfo;

@Controller
public class ApplicationController extends AuthenticationBase {

	@Autowired
	private AmazonS3 s3client;

	@Value("${s3.endpointUrl}")
	private String endpointUrl;

	@Value("${s3.bucketName}")
	private String bucketName;

	@GetMapping("/application")
	public ModelAndView application(ModelMap model, RedirectAttributes redirect, HttpServletRequest request) {
		String nextPage = "application";
		UserInfo info = (UserInfo) request.getSession().getAttribute(USER_SESSION_ATTR);
		if (info != null) {
			model.addAttribute("user_info", info);
			HashMap<String, String> bucket = new HashMap<String, String>();
			String URL = "";
			ListObjectsV2Result result = s3client.listObjectsV2(bucketName);
			List<S3ObjectSummary> objects = result.getObjectSummaries();
			for (S3ObjectSummary os : objects) {
				URL = endpointUrl + "" + os.getKey();
				// bucket.add(URL);
				// bucket.add(os.getKey());

				// bucket.put("url", URL);
				bucket.put(URL, os.getKey());
				System.out.println("* " + os.getKey());
			}
			model.addAttribute("itemName", bucket);
			return new ModelAndView("application", model);

		} else {
			// The user is not logged in, so go to the login page
			nextPage = "index";
		}
		return new ModelAndView(nextPage, model);
	}

	@GetMapping("/logout_form")
	public String logoutPage(HttpServletRequest request) {
		UserInfo info = (UserInfo) request.getSession().getAttribute(USER_SESSION_ATTR);
		if (info != null) {
			authService.userLogout(info.getUserName());
			request.getSession().setAttribute(USER_SESSION_ATTR, null);
		}
		return "index";
	}
}
