package com.berruhanedar.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class TrainerWorkloadServiceApplicationMainTest {

    @Test
    void shouldRunApplicationWithoutArguments() {

        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        try (var mockedSpringApplication =
                     mockStatic(SpringApplication.class)) {

            mockedSpringApplication
                    .when(() -> SpringApplication.run(
                            TrainerWorkloadServiceApplication.class,
                            new String[]{}
                    ))
                    .thenReturn(context);

            TrainerWorkloadServiceApplication.main(new String[]{});

            mockedSpringApplication.verify(() ->
                    SpringApplication.run(
                            TrainerWorkloadServiceApplication.class,
                            new String[]{}
                    )
            );
        }
    }

    @Test
    void shouldRunApplicationWithArguments() {

        String[] args = {
                "--spring.profiles.active=test",
                "--server.port=0"
        };

        ConfigurableApplicationContext context =
                mock(ConfigurableApplicationContext.class);

        try (var mockedSpringApplication =
                     mockStatic(SpringApplication.class)) {

            mockedSpringApplication
                    .when(() -> SpringApplication.run(
                            TrainerWorkloadServiceApplication.class,
                            args
                    ))
                    .thenReturn(context);

            TrainerWorkloadServiceApplication.main(args);

            mockedSpringApplication.verify(() ->
                    SpringApplication.run(
                            TrainerWorkloadServiceApplication.class,
                            args
                    )
            );
        }
    }
}