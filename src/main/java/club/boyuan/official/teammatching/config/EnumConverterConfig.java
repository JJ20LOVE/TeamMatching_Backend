package club.boyuan.official.teammatching.config;

import club.boyuan.official.teammatching.dto.request.community.CommunityQueryRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EnumConverterConfig implements WebMvcConfigurer {


    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToSectionTypeConverter());
        registry.addConverter(new StringToOrderTypeConverter());
    }

    private static class StringToSectionTypeConverter implements Converter<String, CommunityQueryRequest.SectionType> {
        @Override
        public CommunityQueryRequest.SectionType convert(String source) {
            int code = Integer.parseInt(source);
            for (CommunityQueryRequest.SectionType type : CommunityQueryRequest.SectionType.values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null;
        }
    }

    private static class StringToOrderTypeConverter implements Converter<String, CommunityQueryRequest.OrderType> {
        @Override
        public CommunityQueryRequest.OrderType convert(String source) {
            for (CommunityQueryRequest.OrderType type : CommunityQueryRequest.OrderType.values()) {
                if (type.getValue().equals(source)) {
                    return type;
                }
            }
            return null;
        }
    }
}
