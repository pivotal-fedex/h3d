package com.fedex.h3d;

import org.fluentlenium.adapter.junit.FluentTest;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(IntegrationTestRunner.class)
public class TestTest extends FluentTest {

    @Test
    public void titleOfDuckDuckGoShouldContainSearchQueryName() {
        goTo("http://localhost:8080");
        assertThat($("h1").text()).contains("Welcome to app!");
    }
}