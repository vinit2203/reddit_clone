package com.example.redditclone.service;

import com.example.redditclone.dto.AuthenticationResponse;
import com.example.redditclone.dto.LoginRequest;
import com.example.redditclone.dto.RegisterRequest;
import com.example.redditclone.exceptions.RedditException;
import com.example.redditclone.model.NotificationEmail;
import com.example.redditclone.model.User;
import com.example.redditclone.model.VerificationToken;
import com.example.redditclone.repository.UserRepository;
import com.example.redditclone.repository.VerificationTokenRepository;
import com.example.redditclone.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class AuthService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailContentBuilder mailContentBuilder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(@RequestBody RegisterRequest registerRequest){
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);

        String token = generatedVerificationToken(user);
        mailService.sendMail(new NotificationEmail("Please Activate your account", user.getEmail(),
                  "Thank you for signing up to Spring Reddit, " +
                        "please click on the below url to activate your account : " +
                        "http://localhost:8080/api/auth/accountVerification/" + token));
    }

    private String generatedVerificationToken(User user){
       String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token){
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() ->new RedditException("Invalid token")));

    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken){
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(()->new RedditException("user not found with name " + username) );
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest){
        Authentication authenticate = authenticationManager.authenticate((new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword())));
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        String token = jwtProvider.generateToken(authenticate);
        return new AuthenticationResponse(token, loginRequest.getUsername());
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(){

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) SecurityContextHolder.
                getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new RedditException("User name not found - " + principal.getUsername()));

//        Authentication principal = SecurityContextHolder.getContext().getAuthentication();
//        System.out.println(principal.getName() + "abcjkds");
//
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        String username;
//        if (principal instanceof UserDetails) {
//            username = ((UserDetails)principal).getUsername();
//        } else {
//            username = principal.toString();
//        }
//        return userRepository.findByUsername(username)
//                .orElseThrow(() -> new RedditException("User name not found - " + username));
    }

}
