package com.ezycontact.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ezycontact.dao.ContactRepository;
import com.ezycontact.dao.UserRespository;
import com.ezycontact.entities.Contact;
import com.ezycontact.entities.User;
import com.ezycontact.helper.Message;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRespository userRespository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("USERNAME" + username);
		User user = this.userRespository.getUserByUserName(username);
		System.out.println("USER " + user);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
		try {
			String name = principal.getName(); 
			
			User user = this.userRespository.getUserByUserName(name);
			
			//proccessing and uploading file
			
			if(file.isEmpty()) {
				System.out.print("File is Empty!!");
				contact.setImage("contact.jpg");
			}
			else {
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("/static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.print("Image is uploaded!!");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			
			this.userRespository.save(user);
			
			System.out.println("DATA: " + contact);
			System.out.println("Addded to database!!");
			
			session.setAttribute("message", new Message("Your contact is added!!", "success"));
		}
		catch(Exception e) {
			System.out.println("ERROR: "+ e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong, Try again!", "danger"));
		}
		
		return "normal/add_contact_form"; 
	}
	
	
	//show contacts
	
	@GetMapping("/view-contacts/{page}")
	public String viewContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contacts");
		
		String username = principal.getName();
		User user = this.userRespository.getUserByUserName(username);
		
		Pageable pageable =  PageRequest.of(page, 10);

		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/view-contacts";
	}
	
	//showing particular contact detail
	
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID: " + cId);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRespository.getUserByUserName(userName);
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}else {
			model.addAttribute("title", "Contact details");
		}
		return "normal/contact-details";
	}
	
	
	//Delete the contact
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cid, Model model, Principal principal, HttpSession session){
		 Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		 Contact contact = contactOptional.get();
		 
		 String userName = principal.getName();
		 User user = this.userRespository.getUserByUserName(userName);
		 
		 //Deleting Image
		 if(user.getId()==contact.getUser().getId()) {
			 
			 user.getContacts().remove(contact);
			 
			 this.userRespository.save(user);
			 
			 System.out.println("Image DELETED");
			 session.setAttribute("message", new Message("Contact deleted successfully...", "success"));
		 }
		 
		 
		return "redirect:/user/view-contacts/0";
	}
	
	//Update the contact
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model){
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update-form";
	}
	
	
	//update contact handler
	
	@PostMapping("process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		
		try {
			
			//old contact details
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			
			
			if(!file.isEmpty()) {
				//delete old image
				File deleteFile = new ClassPathResource("/static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				
				//update new image
				File saveFile = new ClassPathResource("/static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRespository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated succesfully!!", "success"));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/" + contact.getcId()+"/contact";
	}
	
	
	// Your profile Page
	
	@GetMapping("/profile")
	public String yourProfile(Model model){
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
}
