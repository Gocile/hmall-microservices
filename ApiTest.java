import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiTest {

    // 严谨点：生产环境应从环境变量或配置中心获取
    private static final String API_KEY = System.getenv("LLM_API_KEY");
    private static final String BASE_URL = "https://api.openai.com/v1/chat/completions";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // LLM 响应通常较慢，需调大读取超时
            .build();

    public static void main(String[] args) throws IOException {
        String prompt = "请简述什么是领域驱动设计（DDD）中的聚合根？";

        // 1. 构建请求体 (符合 OpenAI 兼容的 API 标准)
        ChatRequest chatRequest = new ChatRequest("gpt-3.5-turbo",
                Collections.singletonList(new Message("user", prompt)));

        String jsonPayload = objectMapper.writeValueAsString(chatRequest);

        // 2. 发起请求
        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

        System.out.println("--- 正在发送请求 ---");
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求失败: HTTP " + response.code() + " - " + response.message());
                return;
            }

            // 3. 解析响应
            String responseBody = response.body().string();
            ChatResponse chatResponse = objectMapper.readValue(responseBody, ChatResponse.class);

            System.out.println("--- 模型回复 ---");
            System.out.println(chatResponse.choices.get(0).message.content);
        }
    }

    // --- 数据模型 (POJO) ---
    static class ChatRequest {
        public String model;
        public List<Message> messages;
        public ChatRequest(String model, List<Message> messages) {
            this.model = model;
            this.messages = messages;
        }
    }

    static class Message {
        public String role;
        public String content;
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    static class ChatResponse {
        public List<Choice> choices;
        static class Choice {
            public Message message;
        }
    }
}