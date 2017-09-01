spring-rythm
============

Enable SpringFramework application developer to use Rythm template engine


Maven dependency
----------------
    <dependency>
      <groupId>org.rythmengine</groupId>
      <artifactId>spring-rythm</artifactId>
      <version>1.0</version>
    </dependency>


Example Configuration
---------------------

    @SpringBootApplication
    public class DemoApplication {
        @Bean
        public RythmConfigurer rythmConfigurer(){
            RythmConfigurer rythmConfigurer = new RythmConfigurer();
            rythmConfigurer.setResourceLoaderPath("/templates");
            rythmConfigurer.setDevMode(true);
            return rythmConfigurer;
        }

      @Bean
      public RythmViewResolver rythmViewResolver() {
        RythmViewResolver viewResolver = new RythmViewResolver();
        viewResolver.setCache(true);
        viewResolver.setSuffix(".html");
        return viewResolver;
      }

      public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
      }
    }

