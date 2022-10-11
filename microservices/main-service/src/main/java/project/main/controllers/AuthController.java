package project.main.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import project.api.core.user.Role;
import project.api.core.user.User;
import project.api.core.user.Role.ERole;
import project.api.core.user.request.LoginRequest;
import project.api.core.user.request.SignupRequest;
import project.api.core.user.response.JwtResponse;
import project.api.core.user.response.MessageResponse;
import project.main.security.jwt.JwtUtils;
import project.main.security.services.UserDetailsImpl;
import project.main.services.MainIntegration;
import project.user.persistence.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/main/auth")
public class AuthController {

    @Autowired
    private MainIntegration integration;

    @Autowired
    public AuthController(MainIntegration integration) {
        this.integration = integration;
    }
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    PasswordEncoder encoder;

    @PostMapping(value = "/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(item -> item.getAuthority())
            .collect(Collectors.toList());
        return ResponseEntity.ok(new JwtResponse(jwt, 
                                userDetails.getId(), 
                                userDetails.getUsername(), 
                                userDetails.getEmail(),
                                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        if(userRepository.existsByUserName(signupRequest.getUsername())) {
            return ResponseEntity.badRequest()
                                    .body(new MessageResponse("Error: Username is already registered!"));
        }

        if(userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                                    .body(new MessageResponse("Error: Email is already registered!"));
        }

        Set<String> strRoles = new HashSet<>();

        if(signupRequest.getRoles() == null) {
            strRoles.add(ERole.ROLE_USER.toString());
        //Set<Role> roles = new HashSet<>();

        //if(strRoles == null) {
            //strRoles = new HashSet<>();
            //Create user role as default
            //Role userRole = new Role(ERole.ROLE_USER);
            //roles.add(userRole);
            strRoles.add(ERole.ROLE_USER.toString());

        } else {
            strRoles.forEach(strRole -> {
                switch (strRole) {
                case "admin":
                    //Role adminRole = new Role(ERole.ROLE_ADMIN);
                    //roles.add(adminRole);
                    strRoles.add(ERole.ROLE_ADMIN.toString());
                    break;

                case "mod":
                    //Role modRole = new Role(ERole.ROLE_MODERATOR);
                    //roles.add(modRole);
                    strRoles.add(ERole.ROLE_MODERATOR.toString());
                    break;

                default:
                    //Role userRole = new Role(ERole.ROLE_USER);
                    //roles.add(userRole);
                    strRoles.add(ERole.ROLE_USER.toString());
                    break;
                }
            });
        }

        //Create new user's account
        User user = new User(signupRequest.getUsername(), 
                        encoder.encode(signupRequest.getPassword()), 
                        signupRequest.getFirstName(), 
                        signupRequest.getLastName(), 
                        signupRequest.getEmail(),
                        strRoles,
                        signupRequest.getServiceAddress());
        integration.createUser(user);
        return ResponseEntity.ok(new MessageResponse("User is successfully registered!"));         
    }
}