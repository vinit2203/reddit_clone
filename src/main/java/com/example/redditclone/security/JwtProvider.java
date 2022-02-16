package com.example.redditclone.security;

import com.example.redditclone.exceptions.RedditException;
import com.example.redditclone.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;

import static io.jsonwebtoken.Jwts.parser;

@Service
public class JwtProvider {

    private KeyStore keyStore;
    @PostConstruct
    public void init()  {
        try{
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/springreddit.jks");
            keyStore.load(resourceAsStream,"123456".toCharArray());
        } catch ( CertificateException | NoSuchAlgorithmException | IOException | KeyStoreException e){
            throw new RedditException("Exception occured while loading keystore");
        }
    }
    public String generateToken(Authentication authentication){
        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User)authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey(){
        try{
            return (PrivateKey) keyStore.getKey("reddit_clone","123456".toCharArray());
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e){
            throw new RedditException("exception occurred while retrieving private key from keystore");
        }
    }

    public boolean validateToken(String jwt){
        parser().setSigningKey(getPublicKey()).parseClaimsJws(jwt);
        return true;
    }

    public PublicKey getPublicKey(){
        try{
            return keyStore.getCertificate("reddit_clone").getPublicKey();
        }catch (KeyStoreException e){
            throw new RedditException("exception occurred while retrieving public key from keystore");
        }
    }

    public String getUsernameFromJwt(String token){
        Claims claims = parser()
                .setSigningKey(getPublicKey())
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
