// package payment.example.demo.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// import java.util.Arrays;

// /**
//  * Configuration CORS globale pour le Payment Service
//  * Cette configuration s'applique à tous les endpoints /api/**
//  */
// @Configuration
// public class CorsConfig {

//     @Bean
//     public CorsFilter corsFilter() {
//         CorsConfiguration config = new CorsConfiguration();
        
//         // Autoriser toutes les origines (pour développement)
//         // En production, spécifier les domaines exacts
//         config.setAllowCredentials(false);
//         config.addAllowedOriginPattern("*");
        
//         // Autoriser tous les headers
//         config.setAllowedHeaders(Arrays.asList("*"));
        
//         // Autoriser toutes les méthodes HTTP nécessaires
//         config.setAllowedMethods(Arrays.asList(
//             "GET", 
//             "POST", 
//             "PUT", 
//             "DELETE", 
//             "OPTIONS"
//         ));
        
//         // Exposer les headers de réponse
//         config.setExposedHeaders(Arrays.asList(
//             "Authorization",
//             "Content-Type",
//             "X-Total-Count"
//         ));
        
//         // Appliquer cette configuration à tous les endpoints /api/**
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/api/**", config);
        
//         return new CorsFilter(source);
//     }
// }