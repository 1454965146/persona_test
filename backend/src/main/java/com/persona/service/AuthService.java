package com.persona.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persona.config.AuthProperties;
import com.persona.config.WechatProperties;
import com.persona.model.AuthToken;
import com.persona.model.User;
import com.persona.repository.AuthTokenRepository;
import com.persona.repository.UserRepository;
import com.persona.security.PasswordHasher;
import com.persona.security.UserContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String WECHAT_CODE_URL =
            "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";

    private final UserRepository userRepository;
    private final AuthTokenRepository authTokenRepository;
    private final AuthProperties authProperties;
    private final WechatProperties wechatProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AuthService(UserRepository userRepository, AuthTokenRepository authTokenRepository,
                       AuthProperties authProperties, WechatProperties wechatProperties,
                       RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.authTokenRepository = authTokenRepository;
        this.authProperties = authProperties;
        this.wechatProperties = wechatProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> wechatLogin(String code, String nickname) {
        if (code == null || code.trim().isEmpty()) throw new RuntimeException("微信登录 code 不能为空");

        String openid;
        if (wechatProperties.isMockEnabled()) {
            openid = "mock_" + sha256(code);
        } else {
            if (isBlank(wechatProperties.getAppId()) || isBlank(wechatProperties.getAppSecret())) {
                throw new RuntimeException("微信登录未配置，请设置 WECHAT_APP_ID 和 WECHAT_APP_SECRET");
            }
            openid = resolveWechatOpenid(code.trim());
        }

        User user = userRepository.findByOpenid(openid).orElse(null);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setNickname(safeNickname(nickname, "微信用户"));
        } else if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(safeNickname(nickname, user.getNickname()));
        }
        user = userRepository.save(user);
        return buildLoginResult(user, issueToken(user));
    }

    @Transactional
    public Map<String, Object> devLogin(String username, String password) {
        if (!authProperties.isDevLoginEnabled()) throw new RuntimeException("开发登录已关闭");
        if (isBlank(username) || isBlank(password)) throw new RuntimeException("账号和密码不能为空");

        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("账号不存在"));
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new RuntimeException("密码错误");
        }
        return buildLoginResult(user, issueToken(user));
    }

    @Transactional
    public Map<String, Object> devRegister(String username, String password) {
        if (!authProperties.isDevLoginEnabled()) throw new RuntimeException("开发登录已关闭");
        if (isBlank(username) || username.trim().length() < 2) throw new RuntimeException("账号至少2个字符");
        if (isBlank(password) || password.length() < 4) throw new RuntimeException("密码至少4位");
        if (userRepository.findByUsername(username.trim()).isPresent()) throw new RuntimeException("账号已存在");

        User user = new User();
        user.setUsername(username.trim());
        user.setPasswordHash(PasswordHasher.hash(password));
        user.setNickname(username.trim());
        user = userRepository.save(user);
        return buildLoginResult(user, issueToken(user));
    }

    @Transactional
    public User authenticate(String token) {
        if (token == null || token.isEmpty()) return null;
        String tokenHash = sha256(token);
        AuthToken authToken = authTokenRepository.findByTokenHash(tokenHash)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(null);
        if (authToken == null) return null;
        User user = authToken.getUser();
        // 初始化懒加载字段，避免在拦截器事务外访问导致 no Session。
        user.getNickname();
        user.getUsername();
        return user;
    }

    @Transactional
    public void logout(String token) {
        if (token == null || token.isEmpty()) return;
        String tokenHash = sha256(token);
        authTokenRepository.findByTokenHash(tokenHash).ifPresent(t -> {
            t.setRevoked(true);
            authTokenRepository.save(t);
        });
    }

    public User requireCurrentUser() {
        User user = UserContext.get();
        if (user == null) throw new RuntimeException("未登录");
        return user;
    }

    public Map<String, Object> currentUser() {
        User user = requireCurrentUser();
        return userView(user);
    }

    public Map<String, Object> publicConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("wechatMockEnabled", wechatProperties.isMockEnabled());
        result.put("devLoginEnabled", authProperties.isDevLoginEnabled());
        return result;
    }

    private String resolveWechatOpenid(String code) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    WECHAT_CODE_URL, String.class,
                    wechatProperties.getAppId(), wechatProperties.getAppSecret(), code);
            JsonNode root = objectMapper.readTree(response.getBody());
            String openid = root.path("openid").asText(null);
            if (openid == null || openid.isEmpty()) {
                String errmsg = root.path("errmsg").asText("unknown error");
                throw new RuntimeException("微信登录失败: " + errmsg);
            }
            return openid;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("微信登录服务暂时不可用", e);
        }
    }

    private String issueToken(User user) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        AuthToken authToken = new AuthToken();
        authToken.setTokenHash(sha256(token));
        authToken.setUser(user);
        authToken.setExpiresAt(LocalDateTime.now().plusHours(authProperties.getTokenTtlHours()));
        authToken.setRevoked(false);
        authTokenRepository.save(authToken);
        return token;
    }

    private Map<String, Object> buildLoginResult(User user, String token) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        result.put("user", userView(user));
        return result;
    }

    private Map<String, Object> userView(User user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("nickname", user.getNickname());
        view.put("username", user.getUsername());
        return view;
    }

    private String safeNickname(String nickname, String fallback) {
        if (nickname == null || nickname.trim().isEmpty()) return fallback;
        String value = DimensionUtil.sanitize(nickname.trim());
        return value.length() > 20 ? value.substring(0, 20) : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
