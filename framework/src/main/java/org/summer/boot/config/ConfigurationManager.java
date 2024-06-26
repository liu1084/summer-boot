package org.summer.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.summer.boot.constants.Constant.DEFAULT_CONFIGURATION_FILE;


public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    private static Map<String, Object> configurations = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static String activeProfile;
    private static boolean isDebug;

    static {
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); //禁用将日期时间写成时间戳的特性
        mapper.findAndRegisterModules();
    }

    public static void loadProperties(Class<?> mainClass) throws IOException {
        loadDefaultConfiguration(mainClass);
    }



    private static void loadDefaultConfiguration(Class<?> mainClass) throws IOException {
        logger.debug("load config file {}", DEFAULT_CONFIGURATION_FILE);
        try (InputStream fileInputStream = mainClass.getClassLoader().getResourceAsStream(DEFAULT_CONFIGURATION_FILE);) {
            ServerProperties serverProperties = mapper.readValue(fileInputStream, ServerProperties.class);

        }catch (FileNotFoundException e1) {
            logger.error("file {} not exist", DEFAULT_CONFIGURATION_FILE);
            e1.printStackTrace();
            throw new FileNotFoundException(DEFAULT_CONFIGURATION_FILE);
        }
    }


    public static <T> T getConfiguration(Class<T> configClass) {
        try {
            String json = mapper.writeValueAsString(configurations);
            return mapper.readValue(json, configClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to bind configuration", e);
        }
    }

}
