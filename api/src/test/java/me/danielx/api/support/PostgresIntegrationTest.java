package me.danielx.api.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersConfiguration.class)
public abstract class PostgresIntegrationTest {}
