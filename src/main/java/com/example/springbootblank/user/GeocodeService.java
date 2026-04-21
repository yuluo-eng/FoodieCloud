package com.example.springbootblank.user;

import com.example.springbootblank.common.error.BusinessException;
import com.example.springbootblank.user.dto.GeocodeReverseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * 逆地理编码：服务端请求 OpenStreetMap Nominatim（需合规 User-Agent，限流约 1 次/秒）。
 */
@Service
public class GeocodeService {

    private static final String USER_AGENT = "FoodieCloud/1.0 (https://github.com/yuluo-eng/FoodieCloud; dev)";

    /** 不依赖 Spring Bean，避免部分环境下 Jackson 未作为编译可见依赖解析 */
    private static final ObjectMapper JSON = new ObjectMapper();

    public GeocodeService() {}

    public GeocodeReverseResponse reverse(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(400, "请提供 latitude 与 longitude");
        }
        double lat = latitude.doubleValue();
        double lon = longitude.doubleValue();
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new BusinessException(400, "经纬度超出有效范围");
        }

        String latStr = latitude.stripTrailingZeros().toPlainString();
        String lonStr = longitude.stripTrailingZeros().toPlainString();
        String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json&accept-language=zh",
                URLEncoder.encode(latStr, StandardCharsets.UTF_8),
                URLEncoder.encode(lonStr, StandardCharsets.UTF_8));

        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("User-Agent", USER_AGENT)
                            .header("Accept", "application/json")
                            .timeout(Duration.ofSeconds(15))
                            .GET()
                            .build();
            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new BusinessException(502, "地理编码服务暂时不可用");
            }
            JsonNode root = JSON.readTree(res.body());
            if (root.has("error")) {
                throw new BusinessException(400, "无法解析该坐标对应的地址");
            }
            String display = root.path("display_name").asText(null);
            if (display == null || display.isBlank()) {
                throw new BusinessException(400, "未返回可读地址，请手动填写");
            }
            return new GeocodeReverseResponse(display, latitude, longitude);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(502, "逆地理编码失败：" + e.getMessage());
        }
    }
}
