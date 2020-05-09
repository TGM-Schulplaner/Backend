package at.tgm.schulplaner.config;

/**
 * @author Georg Burkl
 * @version 2020-05-08
 */
public class OpenApiConfig {
    /*@Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("basicScheme",
                        new SecurityScheme().type(SecurityScheme.Type.APIKEY)))
                .info(new Info().title("Petstore API").version(appVersion).description(
                        "This is a sample server Petstore server.  You can find out more about     Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).      For this sample, you can use the api key `special-key` to test the authorization     filters.")
                        .termsOfService("http://swagger.io/terms/")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }*/

    /*@Bean
    public GroupedOpenApi actuatorApi() {
        return GroupedOpenApi.builder().setGroup("Actuator")
                .pathsToMatch("/actuator/**")
                .build();
    }*/
}
