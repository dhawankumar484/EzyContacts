package com.ezycontact.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ezycontact.dao.UserRespository;
import com.ezycontact.entities.Contact;
import com.ezycontact.entities.User;
import com.ezycontact.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private UserRespository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Ezy Contact");
		
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Ezy Contact");
		
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "SignUp - Ezy Contact");
		model.addAttribute("user", new User());
		
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult results, @RequestParam(value="agreement", defaultValue="false")boolean agreement, Model model, HttpSession session ) {
		
		try {
			if(results.hasErrors()) {
				System.out.println("ERROR: "+results.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			if(!agreement) {
				System.out.println("You have agreed to the terms and condition");
				throw new Exception("You have agreed to the terms and condition");
			}
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(this.passwordEncoder.encode(user.getPassword()));
			System.out.println("User"+user);
			System.out.println("Agreement: " + agreement);
			
			User result = this.userRepository.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Succesful Registered !!", "alert-signup"));
			return "signup";

		}
		catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Somthing went wrong!!"+ e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}

}
