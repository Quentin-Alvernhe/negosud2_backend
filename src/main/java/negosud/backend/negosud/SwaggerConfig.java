package negosud.backend.negosud;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "NEGOSUD API",
                version = "1.0",
                description = "Documentation de l'API NEGOSUD.",
//                termsOfService = "http://example.com/terms",
//                contact = @Contact(
//                        name = "Your Name",
//                        url = "http://example.com",
//                        email = "your-email@example.com"
//                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        )
)

public class SwaggerConfig {
}
