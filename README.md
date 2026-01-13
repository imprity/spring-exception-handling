# Spring 간단 에러 핸들링

Spring의 간단한 에러 핸들링 연습

## 기술 스택
- Language: Java 17
- IDE: IntelliJ IDEA

## 빌드

빌드를 위해서는 JDK 17이 필요합니다.

이 명령어를 입력해 주세요
```
gradlew build
```

리눅스에서는 이렇게 해야 합니다.
```
./gradlew build
```

## 실행
```
gradlew bootRun
```

## API 명세서

### POST /api/sign-up

REQUEST
```
{
    "name" : "momo",
    "age" : 26
}
```

RESPONSE 201
```
{
    "name" : "momo",
    "age" : 26
}
```

### GET /api/members

RESPONSE 200
```
[
    {
        "name" : "momo",
        "age" : 26
    },
    {
        "name" : "kiki",
        "age" : 26
    },
]
```

### GET /api/check?name={name}

RESPONSE 200
```
{
    msg : "사용 가능한 이름입니다"
}
```

## git convention

- FEAT:     feature 추가
- FIX:      버그 수정
- REFACTOR: refactoring
- MISC:     기타

