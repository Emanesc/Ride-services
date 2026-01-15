package ride.example.demo.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig {

    // 1️⃣ MessageDispatcherServlet pour SOAP
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext context) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(context);
        servlet.setTransformWsdlLocations(true); // important pour générer le WSDL
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    // 2️⃣ Définition du WSDL
    @Bean(name = "rides")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema rideSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("RidePort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://example.com/ride/ws");
        wsdl11Definition.setSchema(rideSchema);
        return wsdl11Definition;
    }

    // 3️⃣ Schéma XSD
    @Bean
    public XsdSchema rideSchema() {
        return new SimpleXsdSchema(new ClassPathResource("ride.xsd"));
    }
}
