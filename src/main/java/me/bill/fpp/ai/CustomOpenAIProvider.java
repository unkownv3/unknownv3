package me.bill.fpp.ai;

public class CustomOpenAIProvider extends OpenAIProvider {
    public CustomOpenAIProvider(String apiKey, String model, String baseUrl) {
        super(apiKey, model, baseUrl);
    }

    @Override
    public String getName() { return "custom-openai"; }
}
