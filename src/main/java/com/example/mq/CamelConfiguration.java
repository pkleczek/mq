package com.example.mq;

import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Configuration
public class CamelConfiguration {
    @Bean
    RouteBuilder serverSide() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:question?replyTo=answer")
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getIn().setBody(body.toUpperCase());
                    })
                    .to("log:server");
            }
        };
    }

    @Bean
    RouteBuilder clientSide() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:foobar")
                    .process(exchange -> {
                        String body = exchange.getIn().getBody(String.class);

                    })
                    .to(ExchangePattern.InOut, "jms:queue:question?replyTo=answer&requestTimeout=5s")
                    .to("log:client");
            }
        };
    }

    @Component
    class Start implements CommandLineRunner {
        @Autowired
        ProducerTemplate template;

        @Autowired
        JmsTemplate jmsTemplate;


        @Override
        public void run(String... args) throws Exception {
            Object fooBarBaz = template.requestBody("direct:foobar", "Foo bar baz");
            System.out.println(fooBarBaz);
            template.stop();
        }
    }

}
