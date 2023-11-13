package com.ezycontact.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ezycontact.dao.UserRespository;
import com.ezycontact.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

	@Autowired
	private UserRespository userRespository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRespository.getUserByUserName(username);
		if(user==null) {
			throw new UsernameNotFoundException("Could not found user!!");
		}
		
		CustomUserDetails customUserDetails = new CustomUserDetails(user);
		return customUserDetails;
	}

}
