package com.ezycontact.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ezycontact.dao.ContactRepository;
import com.ezycontact.dao.UserRespository;
import com.ezycontact.entities.Contact;
import com.ezycontact.entities.User;

@RestController
public class SearchController {
	
	@Autowired
	private UserRespository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	

	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal pricipal){
		
		User user = this.userRepository.getUserByUserName(pricipal.getName());
		 List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);
		 
		 return ResponseEntity.ok(contacts);
	}
}
