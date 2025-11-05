package com.example.int221_ssi_03;

import com.example.int221_ssi_03.Mapper.ListMapper;
import com.example.int221_ssi_03.utils.FileStorageProperties;
import jakarta.validation.Validator;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({
        FileStorageProperties.class
})
public class Int221Ssi03Application {

    public static void main(String[] args) {
        SpringApplication.run(Int221Ssi03Application.class, args);
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public ListMapper listMapper() { return ListMapper.getInstance(); }

    @Bean
    public Validator validator() {
        return new LocalValidatorFactoryBean();
    }

}
