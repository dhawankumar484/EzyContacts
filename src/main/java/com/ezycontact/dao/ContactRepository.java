package com.ezycontact.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ezycontact.entities.Contact;
import com.ezycontact.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer >{

	//current page
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactByUser(@Param ("userId") int userId, Pageable pageable);
	
	//search contact by kerword
	public List<Contact> findByNameContainingAndUser(String keyword, User user);
}
