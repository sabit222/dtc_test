package userservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import userservice.user.User;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long jwtExpiration = 86400000; // 1 день
    private static final long refreshExpiration = 604800000; // 7 дней

    // Извлечение username из токена
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Извлечение любого claim из токена
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Генерация токена (Access Token)
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("firstname", user.getFirstname());
        claims.put("roles", user.getRole().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())); // Добавляем роли как строки
        return generateToken(claims, user, jwtExpiration);
    }
    public String generateToken(Map<String, Object> claims, User user, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())// Или другой идентификатор пользователя
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Генерация Refresh Token
    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    // Общий метод для генерации токенов
    private String buildToken(Map<String, Object> extraClaims, User user, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail()) // Сохраняем email как идентификатор
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Проверка валидности токена
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // Проверка срока действия токена
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Извлечение срока действия токена
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Проверка наличия ролей в токене
    public boolean containsRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.containsKey("roles");
    }

    // Извлечение ролей из токена
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get("roles");

        if (rolesObject instanceof List) {
            return ((List<?>) rolesObject).stream()
                    .map(role -> {
                        if (role instanceof LinkedHashMap) {
                            // Проверяем и извлекаем ключ "name" из LinkedHashMap
                            Object roleName = ((LinkedHashMap<?, ?>) role).get("name");
                            if (roleName != null) {
                                return roleName.toString();
                            } else {
                                throw new RuntimeException("Role name is missing or null");
                            }
                        } else {
                            return role.toString(); // Если это просто строка
                        }
                    })
                    .collect(Collectors.toList());
        }

        throw new IllegalArgumentException("Invalid roles format in token");
    }

    // Извлечение всех Claims из токена
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    // Получение секретного ключа для подписи токена
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);

    }
}
