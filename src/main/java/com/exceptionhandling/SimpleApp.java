package com.exceptionhandling;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootApplication
public class SimpleApp {
  @Size(min = 2, message = "이름은 {min}글자 이상이어야 합니다")
  @NotBlank(message = "이름이 비어있으면 안됩니다")
  @Target({ElementType.FIELD, ElementType.PARAMETER})
  @Retention(RetentionPolicy.RUNTIME)
  @Constraint(validatedBy = {})
  public @interface ValidMemberName {
    String message() default "잘못된 이름 입니다";

    Class[] groups() default {};

    Class[] payload() default {};
  }

  @Getter
  @AllArgsConstructor
  public static class Member {
    @ValidMemberName private String name;

    @Min(value = 18, message = "나이는 {value}세 이상이어야 합니다")
    private int age;
  }

  @RestController
  @Validated
  @RequiredArgsConstructor()
  public static class Controller {
    private final MemberService memberService;

    @PostMapping("/api/sign-up")
    public ResponseEntity<Member> signUp(@Valid @RequestBody Member member) {
      memberService.saveMember(member);

      return ResponseEntity.status(HttpStatus.CREATED).body(member);
    }

    @GetMapping("/api/members")
    public ResponseEntity<List<Member>> getMembers() {
      List<Member> members = memberService.getMembers();

      return ResponseEntity.status(HttpStatus.OK).body(members);
    }

    @GetMapping("/api/check")
    public ResponseEntity<SimpleResponse> check(@ValidMemberName @RequestParam String name) {
      SimpleResponse res = memberService.check(name);
      return ResponseEntity.status(HttpStatus.OK).body(res);
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class SimpleResponse {
    private final String msg;
  }

  public static class SimpleException extends RuntimeException {
    public SimpleException(String msg) {
      super(msg);
    }

    @Override
    public boolean equals(Object other) {
      if (other == null) {
        return false;
      }
      if (!(other instanceof SimpleException)) {
        return false;
      }
      SimpleException otherException = (SimpleException) other;
      return this.getMessage().equals(otherException.getMessage());
    }
  }

  @Service
  public static class MemberService {
    private final List<Member> members = Collections.synchronizedList(new ArrayList<>());

    public void saveMember(Member member) {
      if (this.members.size() >= 10) {
        throw new SimpleException("가입한 회원이 10명이여서 더 회원을 받지 않습니다");
      }

      members.add(member);
    }

    public List<Member> getMembers() {
      return new ArrayList<Member>(members);
    }

    public SimpleResponse check(String name) {
      for (final Member member : members) {
        if (member.getName().equals(name)) {
          throw new SimpleException("이미 회원가입된 이름입니다");
        }
      }

      return new SimpleResponse("사용 가능한 이름입니다");
    }
  }

  @RestControllerAdvice
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ExceptionHandlerController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SimpleResponse> handleValidationError(MethodArgumentNotValidException e) {
      String msg =
          e.getBindingResult().getAllErrors().stream()
              .map(x -> x.getDefaultMessage())
              .sorted()
              .findFirst()
              .orElse("잘못된 요청 입니다");

      SimpleResponse res = new SimpleResponse(msg);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(ConstraintViolationException .class)
    public ResponseEntity<SimpleResponse> handleValidationError(ConstraintViolationException e) {
      String msg = 
        e.getConstraintViolations().stream()
        .map(x -> x.getMessage())
        .sorted()
        .findFirst()
        .orElse("잘못된 요청 입니다");

      SimpleResponse res = new SimpleResponse(msg);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    @ExceptionHandler(SimpleException.class)
    public ResponseEntity<SimpleResponse> handleValidationError(SimpleException e) {
      String msg = e.getMessage();

      SimpleResponse res = new SimpleResponse(msg);

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(SimpleApp.class, args);
  }
}
