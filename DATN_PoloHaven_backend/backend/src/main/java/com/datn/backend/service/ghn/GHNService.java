package com.datn.backend.service.ghn;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GHNService {

    private final RestTemplate restTemplate;

    @Value("${ghn.api.token}")
    private String ghnToken;

    @Value("${ghn.api.url}")
    private String ghnApiUrl;

    @Value("${ghn.shop.hanoi}")
    private String hanoiShopId;

    @Value("${ghn.shop.hochiminh}")
    private String hoChiMinhShopId;

    @Value("${ghn.fallback.fee}")
    private String fallbackFee;

    public GHNService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Chọn shop ID phù hợp dựa trên địa chỉ giao hàng
    private String selectShopId(int toDistrictId) {
        // Map các district ID với shop tương ứng
        // Hà Nội và các tỉnh miền Bắc
        if (toDistrictId >= 200 && toDistrictId <= 299) {
            return hanoiShopId;
        }
        // TP.HCM và các tỉnh miền Nam
        else if (toDistrictId >= 700 && toDistrictId <= 799) {
            return hoChiMinhShopId;
        }
        // Mặc định sử dụng shop Hà Nội
        return hanoiShopId;
    }

    // Tính phí vận chuyển theo vùng khi không có API
    public int calculateRegionalFee(int toDistrictId) {
        // Bảng phí vận chuyển theo vùng (VND)
        // Miền Bắc
        if (toDistrictId >= 200 && toDistrictId <= 299) {
            return 25000; // Nội thành Hà Nội
        }
        // Miền Trung
        else if (toDistrictId >= 500 && toDistrictId <= 599) {
            return 45000; // Miền Trung
        }
        // Miền Nam
        else if (toDistrictId >= 700 && toDistrictId <= 799) {
            return 35000; // TP.HCM và lân cận
        }
        // ĐBSCL
        else if (toDistrictId >= 800 && toDistrictId <= 899) {
            return 40000; // Đồng bằng sông Cửu Long
        }
        // Các tỉnh khác
        else {
            return 50000; // Các tỉnh xa
        }
    }

    // API: Lấy danh sách tỉnh/thành phố (cần cho UI)
    public String getProvinces() {
        String url = ghnApiUrl + "master-data/province";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách tỉnh: " + e.getMessage());
        }
    }

    // API: Lấy danh sách quận/huyện (cần cho UI)
    public String getDistricts(int provinceId) {
        String url = ghnApiUrl + "master-data/district?province_id=" + provinceId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách quận/huyện: " + e.getMessage());
        }
    }

    // API: Lấy danh sách phường/xã (cần cho UI)
    public String getWards(int districtId) {
        String url = ghnApiUrl + "master-data/ward?district_id=" + districtId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return response.getBody();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi lấy danh sách phường/xã: " + e.getMessage());
        }
    }

    // Tính phí vận chuyển - hàm chính
    public String calculateShippingFee(Object requestBody) {
        String url = ghnApiUrl + "v2/shipping-order/fee";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Kiểm tra và chuyển đổi requestBody
        if (!(requestBody instanceof Map)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payload phải là một Map");
        }
        Map<String, Object> originalRequest = (Map<String, Object>) requestBody;

        // Kiểm tra các trường bắt buộc
        String[] requiredFields = {"from_district_id", "to_district_id", "to_ward_code", "weight"};
        for (String field : requiredFields) {
            if (!originalRequest.containsKey(field) || originalRequest.get(field) == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thiếu hoặc không hợp lệ: " + field);
            }
        }

        // Chọn shop ID phù hợp
        int toDistrictId = Integer.parseInt(originalRequest.get("to_district_id").toString());
        String selectedShopId = selectShopId(toDistrictId);

        // Payload với định dạng mới của GHN
        Map<String, Object> modifiedRequestBody = new HashMap<>();
        modifiedRequestBody.put("from_district_id", originalRequest.get("from_district_id"));
        modifiedRequestBody.put("to_district_id", originalRequest.get("to_district_id"));
        modifiedRequestBody.put("to_ward_code", originalRequest.get("to_ward_code"));
        modifiedRequestBody.put("weight", originalRequest.get("weight"));
        modifiedRequestBody.put("service_type_id", 2); // Giao hàng nhanh
        modifiedRequestBody.put("insurance_value", 0);
        modifiedRequestBody.put("cod_failed_amount", 0);
        modifiedRequestBody.put("coupon", null);

        // Thêm kích thước nếu có
        if (originalRequest.containsKey("height")) {
            modifiedRequestBody.put("height", originalRequest.get("height"));
        }
        if (originalRequest.containsKey("length")) {
            modifiedRequestBody.put("length", originalRequest.get("length"));
        }
        if (originalRequest.containsKey("width")) {
            modifiedRequestBody.put("width", originalRequest.get("width"));
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(modifiedRequestBody, headers);

        try {
            System.out.println("Shipping Fee Request: " + modifiedRequestBody);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            System.out.println("Shipping Fee API Response: " + response.getBody());
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            
            if (root.path("code").asInt() != 200) {
                String message = root.path("message").asText("Lỗi không xác định từ API GHN");
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
            
            JsonNode data = root.path("data");
            if (data.isMissingNode() || !data.has("total")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "API GHN không trả về phí vận chuyển hợp lệ");
            }
            
            return mapper.writeValueAsString(new HashMap<String, Object>() {{
                put("data", data);
            }});
            
        } catch (HttpClientErrorException e) {
            System.err.println("Shipping Fee API Error: " + e.getResponseBodyAsString());
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode errorNode = mapper.readTree(e.getResponseBodyAsString());
                String message = errorNode.path("message").asText("Lỗi không xác định từ API GHN");
                
                // Kiểm tra các lỗi đặc biệt
                if (message.contains("dịch bệnh") || message.contains("tạm thời ngừng")) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "API GHN tạm thời ngừng do ảnh hưởng của dịch bệnh");
                }
                
                if (message.contains("route not found") || message.contains("tuyến giao hàng")) {
                    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Không có tuyến giao hàng đến địa chỉ này");
                }
                
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lỗi từ API GHN: " + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi tính phí vận chuyển: " + e.getMessage());
        }
    }
}