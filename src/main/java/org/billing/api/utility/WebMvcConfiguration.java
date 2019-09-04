package org.billing.api.utility;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@EnableWebMvc
public class WebMvcConfiguration extends WebMvcConfigurerAdapter
{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/public/**").addResourceLocations("classpath:/public/");
    }

   /* @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        super.addViewControllers(registry);
        registry.addViewController("/public*//**").setViewName("/public/index.html");
        *//*registry.addViewController("").setViewName("static/pages/login.html");*//*
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
    }*/
}
