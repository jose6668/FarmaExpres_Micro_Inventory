package co.edu.corhuila.inventory_service.Service;


import org.springframework.stereotype.Service;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;


@Service
public class JwtService {

    private final String SECRET = "mi_clave_super_secreta_ultra_segura_de_256_bits_minimo_1234567890";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
