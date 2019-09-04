package org.billing.api.app;

import custom.zuul.filters.pre.RequestBodyWrapperFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.context.request.RequestContextListener;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class,
        MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@EnableZuulProxy
@ComponentScan(value = {
        "org.billing.api.resource.config",
        "org.billing.api.resource.utility",
        "org.billing.api.utility",
        "custom.zuul.filters.pre",
        "custom.zuul.filters.post"
})
@EnableEurekaClient
public class EdgeServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(EdgeServerApplication.class, args);
    }

    /**
     * This method is used to generate WAR of application
     * <br> No to maintain any deployment descriptor file (web.xml)
     * @param builder
     * @return SpringApplicationBuilder
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(EdgeServerApplication.class);
    }

    @Bean
    public RequestBodyWrapperFilter simpleFilter(){
        return new RequestBodyWrapperFilter();
    }

    @Bean
    public RequestContextListener requestContextListener(){
        return new RequestContextListener();
    }

}
