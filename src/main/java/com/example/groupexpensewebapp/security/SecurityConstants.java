package com.example.groupexpensewebapp.security;

public class SecurityConstants {
    public static final String SECRET = "SecretKeyToGenJWTs";
    public static final long EXPIRATION_TIME = 864_000_000;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String LOGIN_URL = "/api/login";
    public static final String SIGN_UP_URL = "/api/user/sign-up";
    public static final String CHECK_USER_EXISTS_URL = "/api/user/*/exists";
}