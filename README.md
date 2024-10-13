# 간단한 WAS 구현 프로젝트

직접 간단히 WAS를 구현해봄으로써 그 기능에 대해 확실히 이해해보자!
프로젝트를 진행하기 전에 WAS가 무엇이고 톰캣이 무엇인지에 대해 먼저 정리해보자.

## WAS란?

여러 web client의 요구를 web server가 감당할수없는 기능을 구조적으로 web server와 분리하기 위해 만들어진 것으로 `Web Application Server(WAS)` 라고 한다.
web server가 요청을 받으면, WAS가 애플리케이션에 대한 로직을 실행하여 web server로 다시 반환해준다.

### Web Server와 WAS 차이점

![웹 시스템](https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Fblog.kakaocdn.net%2Fdn%2FcEStJi%2FbtsBir9X4WE%2FoD01dyroxKYkwfEvHnoUh0%2Fimg.png)

Web server와 WAS는 사용 목적이 다르다.

- Web Server : html, 이미지 요청등 정적 데이터 요청을 처리하는데 빠르다.
- WAS : servlet, jsp 등 비즈니스 로직을 수행하는데 적합하다.

그렇다고 WAS가 html,이미지 등의 요청을 처리하지 못한다는 말은 아니다. 다만 처리속도가 web서버에 비해 느리기 때문이다.
이렇게 서로 다른 강점을 합해서 사용하기 위해 web server 와 WAS를 연동하여 서비스를 하는것이 대부분이다.

### Web Server와 WAS를 나누는 이유 (= 굳이 Web Server를 사용하는 이유)

굳이 Web Server가 없더라도 WAS만으로 웹 서버의 역할을 수행하도록 할 수 있다.
하지만, 굳이 웹 서버와 WAS를 나누는 이유는 무엇일까?

그 이유는 다음과 같다.

1. 데이터 처리 방식의 차이
    - Web Server는 정적인 데이터를 처리하는데 사용되며, 동적인 데이터를 다룰 수는 없다.
    - 반면, Tomcat 같은 WAS는 애플리케이션 로직을 내부에서 실행할 수 있기에 동적인 데이터를 처리하는데 유용하다.
2. 웹 서버의 Reverse Proxy 기능
    - `Forward Proxy`: 사용자들이 서버에 접속할 때, 자신의 아이피 주소를 감추기 위해 중간에 프록시를 두어 이를 통해 데이터를 주고받는 것
    - Forward Proxy는 클라이언트의 정보를 감추는 것이라면, Reverse Proxy는 반대로 서버의 정보를 감추는 것
    - 보안상 내부 구조를 **감출 필요가 있을 때 reverse proxy를 통한 보안 설정 가능
        - 서버 내부에 파일의 위치, 서비스가 제공되고 있는 포트 번호 등을 감출 수 있다.
3. 로드 밸런싱
    - 이 역시 Reverse Proxy로 인해 제공되는 기능이다.
    - 여러 서버에 부하를 분산할 수 있으며, 무중단 배포 또한 가능하다.
4. 캐싱
    - 이 역시 Reverse Proxy로 인해 제공되는 기능이다.
    - 서버 단에서의 캐시를 말하며 서버로 요청을 보내는 클라이언트들이 반복적으로 조회하는 리소스들을 캐싱해둘 수 있다.
5. 헬스 체크

웹 서버와 WAS의 역할이 겹치는 부분이 있지만, 각자가 특화된 부분들은 명확하다.
웹 서버를 통해 보안과 운영에 집중하고, WAS를 통해 서비스를 실행하여 동적 데이터를 제공하는데 집중한다.

### WAS가 하는 일

위에서 섬령한 것처럼 WAS는 웹 서버의 역할도 하면서 추가로 애플리케이션 로직을 실행하여 동적 데이터를 제공하는 역할을 한다.
정리하자면, 웹(HTTP)를 기반으로 작동하는 서버인데, 이 서버를 통해서 프로그램의 코드도 실행할 수 있다는 것이다. 그리고 여기서 말하는 프로그램의 코드가 바로 `Servlet` 구현체이다.
-> HTTP 요청을 받고, 적절한 서블릿 구현체를 실행하여 동적 컨텐츠 제공하는 것

> 참고
>
> 보통 자바 진영에서 WAS라고 한다면, 서블릿 기능을 포함하는 서버를 말한다. 하지만, 서블릿 기능을 포함하지 않아도 프로그램 코드를 수행할 수 있다면 WAS라고 볼 수 있다.

### 톰캣(Tomcat)이란?

- 웹 서버와 웹 컨테이너의 결합(컨테이너, 웹 컨테이너, 서블릿 컨테이너라고 부름)
- 현재 가장 일반적이고 많이 사용되는 WAS(웹 애플리케이션 서버)
- 톰캣은 JSP와 서블릿 처리, 서블릿의 수명 주기 관리, 요청 URL을 서블릿 코드로 매핑, HTTP 요청 수신 및 응답, 필터 체인 관리 등을 처리해준다.
- 8080 포트를 사용

[출처: Inpa Dev: 아파치 톰캣 개념 구성 & 설정 정리](https://inpa.tistory.com/entry/TOMCAT-%E2%9A%99%EF%B8%8F-%EC%84%A4%EC%B9%98-%EC%84%A4%EC%A0%95-%EC%A0%95%EB%A6%AC)

## 프로젝트 개요

### 구현 내용

- 클라이언트 요청 동시 처리 [OK]
- 스레드 풀 관리 [OK]
- 요청 처리
    - 요청 경로에 따른 적절한 Servlet 실행 by 애노테이션 [OK]
    - Content-Type은 다음 3가지 지원
        - "application/x-www-form-urlencoded" [OK]
        - "application/json" [OK]
        - "multipart/form-data"
    - GET, POST 요청 처리 가능 [OK]
    - 다양한 헤더 정보 저장 가능 [OK]
    - 캐싱 기능 지원 [OK]
- 응답
    - 200, 201, 302, 400, 401, 404, 405, 500 지원 [OK]
    - Content-Type은 다음 3개 지원
        - text/html [OK]
        - application/json [OK]
        - text/plain [OK]
    - 쿠키 & 세션 지원 [OK]
        - 세션 타임아웃 [OK]
    - 리다이렉트 지원 [OK]

이렇게 구현된 WAS를 사용하여 간단한 유저 관리 웹 서비스 구현