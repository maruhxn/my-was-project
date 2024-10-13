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
import com.study.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.study.session.SessionConst.LOGIN_USER;
import static com.study.session.SessionManager.SESSION_COOKIE_NAME;

public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @RequestMapping("/")
    public void home(HttpResponse response) {
        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/index.html");
    }

    @RequestMapping("/login")
    public void login(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.GET) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/login.html");
    }

    @RequestMapping("/login-json")
    public void loginWithJson(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.POST) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

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
        response.addCookie(SESSION_COOKIE_NAME, session.getId());
    }

    @RequestMapping("/login-action")
    public void loginAction(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.POST) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

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
        response.addCookie(SESSION_COOKIE_NAME, session.getId());
    }

    @RequestMapping("/register")
    public void register(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.GET) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

        response.setStatus(HttpStatus.OK);
        response.setResponseBody("/register.html");
    }

    @RequestMapping("/register-action")
    public void registerAction(HttpRequest request, HttpResponse response) {
        if (request.getMethod() != HttpMethod.POST) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

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
        if (request.getMethod() != HttpMethod.POST) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

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
        if (request.getMethod() != HttpMethod.GET) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

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
        if (request.getMethod() != HttpMethod.GET) {
            throw new NotImplementedException("잘못된 METHOD 입니다");
        }

        Session session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setStatus(HttpStatus.FOUND);
        response.setLocation("/");
        response.addCookie(SESSION_COOKIE_NAME, "; Path=/; Max-Age=0; HttpOnly");
    }
}
