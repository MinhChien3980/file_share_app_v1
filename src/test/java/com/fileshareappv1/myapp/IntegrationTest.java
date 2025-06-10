package com.fileshareappv1.myapp;

import com.fileshareappv1.myapp.config.AsyncSyncConfiguration;
import com.fileshareappv1.myapp.config.EmbeddedElasticsearch;
import com.fileshareappv1.myapp.config.EmbeddedSQL;
import com.fileshareappv1.myapp.config.JacksonConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(classes = { FileShareAppV1App.class, JacksonConfiguration.class, AsyncSyncConfiguration.class })
@EmbeddedElasticsearch
@EmbeddedSQL
public @interface IntegrationTest {
}
