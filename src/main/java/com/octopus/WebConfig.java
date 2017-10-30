package com.octopus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.octopus.auth.SessionInterceptor;

@Configuration
@EnableWebMvc
@Import(DataSourceConfig.class)
public class WebConfig extends WebMvcConfigurerAdapter {

  @Autowired
  private SessionInterceptor sessionInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(sessionInterceptor).addPathPatterns("/odata/**");
  }

  @Bean
  public InternalResourceViewResolver viewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/views/");
    resolver.setSuffix(".jsp");
    return resolver;
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController("/index").setViewName("index");
    registry.addRedirectViewController("/", "/index");
    registry.addViewController("/access-denied").setViewName("access-denied");
    registry.addViewController("/logout").setViewName("logout");
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    VersionResourceResolver versionResourceResolver = new VersionResourceResolver().addContentVersionStrategy("/**");
    registry.addResourceHandler("/static/**").addResourceLocations("/static/").setCachePeriod(60 * 60 * 24)
        .resourceChain(true).addResolver(versionResourceResolver);
  }

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

}
