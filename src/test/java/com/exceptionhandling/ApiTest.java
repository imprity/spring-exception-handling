package com.exceptionhandling;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest
@AutoConfigureRestTestClient
class ApiTest {
  @Autowired private RestTestClient client;

  @Test
  public void SimpleTest() {
    doBadSignUp(new SimpleApp.Member("", 26), "이름은 2글자 이상이어야 합니다");
    doBadSignUp(new SimpleApp.Member("    ", 26), "이름이 비어있으면 안됩니다");
    doBadSignUp(new SimpleApp.Member("momo", 17), "나이는 18세 이상이어야 합니다");

    for (int i = 0; i < 10; i++) {
      client
          .post()
          .uri("/api/sign-up")
          .body(new SimpleApp.Member(String.format("회원 %s", i), 18 + 0))
          .exchangeSuccessfully()
          .expectBody(SimpleApp.Member.class);
    }

    doBadSignUp(new SimpleApp.Member("momo", 26), "가입한 회원이 10명이여서 더 회원을 받지 않습니다");

    for (int i = 0; i < 10; i++) {
      client
          .get()
          .uri(String.format("/api/check?name=회원 %s", i))
          .exchange()
          .expectStatus()
          .is4xxClientError()
          .expectBody(SimpleApp.SimpleResponse.class)
          .value(
              b -> {
                assert b.getMsg().equals("이미 회원가입된 이름입니다");
              });
    }

    doBadCheck("", "이름은 2글자 이상이어야 합니다");
    doBadCheck("  ", "이름이 비어있으면 안됩니다");

    client
        .get()
        .uri("/api/check?name=momo")
        .exchangeSuccessfully()
        .expectBody(SimpleApp.SimpleResponse.class)
        .value(
            b -> {
              assert b.getMsg().equals("사용 가능한 이름입니다");
            });
  }

  private void doBadSignUp(SimpleApp.Member member, String expectedMessage) {
    client
        .post()
        .uri("/api/sign-up")
        .body(member)
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(SimpleApp.SimpleResponse.class)
        .value(
            b -> {
              assertEquals(b.getMsg(), expectedMessage);
            });
  }

  private void doBadCheck(String name, String expectedMessage) {
    client
        .get()
        .uri(String.format("/api/check?name=%s", name))
        .exchange()
        .expectStatus()
        .is4xxClientError()
        .expectBody(SimpleApp.SimpleResponse.class)
        .value(
            b -> {
              assertEquals(b.getMsg(), expectedMessage);
            });
  }
}
