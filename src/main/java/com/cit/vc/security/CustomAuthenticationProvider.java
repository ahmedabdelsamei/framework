/*package com.cit.vc.security;


import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
	
	@Autowired
	AuthenticationPropertiesConfig authenticationPropertiesConfig;	
	
    @Override
    public Authentication authenticate(Authentication auth) 
      throws AuthenticationException {
        String username = auth.getName();
        String password = auth.getCredentials()
            .toString();
  
        HashMap<String, String> usersMap =authenticationPropertiesConfig.getUsers();
        
        if(usersMap.containsKey(username)&&usersMap.get(username).equals(password)) {       	
            return new UsernamePasswordAuthenticationToken
                    (username, password, Collections.emptyList());	
        }else {
            throw new
            BadCredentialsException("External system authentication failed");
      }
        
//        if ("interpay".equals(username) && "aW50ZXJwYXk=".equals(password)) {
//        
//        } else {
//            throw new
//              BadCredentialsException("External system authentication failed");
//        }
    }
 
    @Override
    public boolean supports(Class<?> auth) {
    	 return true;
    }
}*/