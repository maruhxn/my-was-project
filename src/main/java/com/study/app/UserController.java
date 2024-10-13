package com.study.app;

import com.study.exception.BadRequestException;
import com.study.exception.NotFoundException;
import com.study.exception.NotImplementedException;
import com.study.exception.UnauthorizedException;
import com.study.request.HttpMethod;
import com.study.request.HttpRequest;
import com.study.response.HttpResponse;
import com.study.response.HttpStatus;
import com.study.servlet.annotation.RequestMapping;
import com.study.session.HttpCookie;
import com.study.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;

import static com.study.session.SessionConst.LOGIN_USER;
import static com.study.session.SessionManager.SESSION_COOKIE_NAME;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    private final Resource resource;

    public UserController(UserRepository userRepository, Resource resource) {
        this.userRepository = userRepository;
        this.resource = resource;
    }

    @RequestMapping("/")
    public void home(HttpResponse response) {
        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/index.html");
    }

    @RequestMapping("/login")
    public void login(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.GET);

        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/login.html");
    }

    @RequestMapping("/login-json")
    public void loginWithJson(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.POST);

        Map<String, Object> body = request.getJsonBody();
        String id = (String) body.get("id");
        String password = (String) body.get("password");

        if (id.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다");
        }

        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다"));

        boolean isCorrectPassword = findUser.getPassword().equals(password);

        if (!isCorrectPassword) {
            throw new UnauthorizedException("인증 실패");
        }

        log.info("로그인 성공: {}", findUser.getName());

        Session session = request.getSession();
        session.setAttribute(LOGIN_USER, findUser);

        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/");
        HttpCookie cookie = new HttpCookie(SESSION_COOKIE_NAME, session.getId());
        response.addCookie(cookie);
    }

    @RequestMapping("/login-action")
    public void loginAction(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.POST);

        Map<String, String> parameters = request.getParameters();
        String id = parameters.get("id");
        String password = parameters.get("password");

        if (id.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다");
        }

        User findUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("유저 정보를 찾을 수 없습니다"));

        boolean isCorrectPassword = findUser.getPassword().equals(password);

        if (!isCorrectPassword) {
            throw new UnauthorizedException("인증 실패");
        }

        log.info("로그인 성공: {}", findUser.getName());

        Session session = request.getSession();
        session.setAttribute(LOGIN_USER, findUser);

        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/");
        HttpCookie cookie = new HttpCookie(SESSION_COOKIE_NAME, session.getId());
        response.addCookie(cookie);
    }

    @RequestMapping("/register")
    public void register(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.GET);

        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/register.html");
    }

    @RequestMapping("/register-action")
    public void registerAction(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.POST);

        Map<String, String> parameters = request.getParameters();
        String id = parameters.get("id");
        String name = parameters.get("name");
        String password = parameters.get("password");

        if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다");
        }

        userRepository.findById(id)
                .ifPresent(user -> {
                    throw new BadRequestException("이미 존재하는 유저입니다");
                });

        User user = new User(id, name, password);
        userRepository.save(user);
        log.info("회원가입 성공: {}", user.getName());

        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/login");
    }

    @RequestMapping("/register-json")
    public void registerWithJson(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.POST);

        Map<String, Object> body = request.getJsonBody();
        String id = (String) body.get("id");
        String name = (String) body.get("name");
        String password = (String) body.get("password");

        if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다");
        }

        userRepository.findById(id)
                .ifPresent(user -> {
                    throw new BadRequestException("이미 존재하는 유저입니다");
                });

        User user = new User(id, name, password);
        userRepository.save(user);
        log.info("회원가입 성공: {}", user.getName());

        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/login");
    }

    @RequestMapping("/info")
    public void userInfo(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.GET);

        Session session = request.getSession(false);
        if (session == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        User loginUser = (User) session.getAttribute(LOGIN_USER);

        response.setStatus(HttpStatus.OK);
        response.setResponseBody(loginUser);
    }

    @RequestMapping("/logout")
    public void logout(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.GET);

        Session session = request.getSession(false);

        if (session == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        session.invalidate();
        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/");
        HttpCookie cookie = new HttpCookie(SESSION_COOKIE_NAME, null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

    @RequestMapping("/resource")
    public void getResourceWithCache(HttpRequest request, HttpResponse response) throws NoSuchAlgorithmException {
        checkIsEnableMethod(request, HttpMethod.GET);

        String eTag = generateETag(resource);

        String ifNoneMatch = request.getHeaderValue("If-None-Match");

        // If-None-Match와 ETag가 일치하는지 확인
        if (Objects.equals(ifNoneMatch, eTag)) {
            // ETag가 일치하면 304 Not Modified 반환
            response.setStatus(HttpStatus.NOT_MODIFIED);
            response.setETag(eTag);
            return;
        }

        // ETag가 일치하지 않으면 리소스와 함께 ETag를 반환
        response.setStatus(HttpStatus.OK);
        response.setETag(eTag);
        response.setCacheControl(10);
        response.setResponseBody(resource);
    }

    @RequestMapping("/update-resource")
    public void updateResource(HttpRequest request, HttpResponse response) {
        checkIsEnableMethod(request, HttpMethod.POST);

        Map<String, Object> body = request.getJsonBody();
        String id = (String) body.get("id");
        String content = (String) body.get("content");

        if (id != null && !id.isEmpty()) resource.setId(id);
        if (content != null && !content.isEmpty()) resource.setContent(content);

        response.setStatus(HttpStatus.OK);
        response.setResponseBody(resource);
    }

    private String generateETag(Resource resource) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(resource.toString().getBytes(StandardCharsets.UTF_8));

        // 바이트 배열을 16진수 문자열로 변환하여 ETag로 사용
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static void checkIsEnableMethod(HttpRequest request, HttpMethod httpMethod) {
        if (request.getMethod() != httpMethod) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }
    }
}
