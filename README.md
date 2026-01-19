# 🗑️ 감정 쓰레기통 (Emotion Trash)
순간순간 떠오르는 불만을 버리는 공간입니다. 작성된 불만은 5분 뒤 자동으로 삭제됩니다.

---

## 🏗 아키텍처
- **Fat Controller**: 비즈니스 로직과 DB 접근을 컨트롤러에서 처리하는 학습용 아키텍처
- **Restful API**: 표준 REST 원칙을 준수하는 API 설계

---

## 🛠 기술 스택
* **Framework**: Spring Boot 3.5.9
* **Language**: Java 17
* **Database**: H2 Database (In-Memory)
* **Library**: Spring JDBC, Springdoc OpenAPI 2.1.0(Swagger)

---

## 주요 API 명세
| 기능 | 메서드 | 경로 | 설명 |
| --- | --- | --- | --- |
| 감정 등록 | `POST` | `/emotions` | 버리고 싶은 감정 등록 |
| 상세 조회 | `GET` | `/emotions/{id}` | 아이디에 해당하는 감정 정보 조회 |
| 목록 조회 | `GET` | `/emotions` | 검색 조건, 페이징, 다중 정렬을 지원하는 목록 조회 |
| 감정 수정 | `PUT` | `/emotions/{id}` | 아이디에 해당하는 감정 정보를 수정합니다. |
| 감정 패치 | `PATCH` | `/emotions/{id}` | 아이디에 해당하는 감정 정보를 패치합니다. |
| 감정 삭제 | `DELETE` | `/emotions/{id}` | 아이디에 해당하는 감정 정보를 삭제합니다. |

---

## ⏰ 주요 스케줄러(Scheduler)
| 기능 | 주기 | 설명 |
| --- | --- | --- |
| 만료 데이터 정리 | 매 분 0초 (`0 * * * * *`) | 등록된 지 5분이 지난 감정 데이터를 논리 삭제(`USE_YN = 'N'`) 처리 |

---

## 프로젝트 학습 목표
### 1. Fat Controller 구조의 이해
비즈니스 로직과 데이터 접근 로직을 컨트롤러에 집중시켜 구현하며, 아키텍처 구조와 상관없이 기능은 정상적으로 작동함을 확인합니다.
이 과정을 통해 왜 실무에서 계층(Controller, Service, Repository)을 분리하는지 그 이유를 체감합니다.

* **분리하는 이유**:
* **가독성**: 컨트롤러 코드가 지나치게 길어져 핵심 로직을 한눈에 파악하기 어렵습니다.
* **유지보수**: 로직이 수정될 때마다 컨트롤러 전체를 수정해야 하며, 코드의 재사용성이 떨어집니다.
* **테스트**: 특정 비즈니스 로직만 따로 떼어내어 단위 테스트(Unit Test)를 수행하기가 매우 까다롭습니다.


### 2. RESTful API 설계 및 개발
HTTP 메서드와 자원(Resource) 중심의 엔드포인트 설계를 통해 누구나 이해하기 쉬운 API를 구축하는 방법을 학습합니다.

* **학습 내용**:
* `GET` (조회), `POST` (등록), `PUT/PATCH` (전체/일부 수정), `DELETE` (삭제) 메서드의 적절한 활용.
* HTTP 상태 코드(`200 OK`, `201 Created`, `400 Bad Request`, `500 Internal Error`)를 이용한 명확한 응답 제공.
* RESTful API 명명 규칙을 준수하여 API URL 생성

**[REST API URL 명명 규칙 리스트]**
* **자원은 명사를 사용하며 소문자를 권장합니다.**
* **자원은 단수형보다는 복수형을 사용합니다.**
* **행위는 HTTP Method(GET, POST, PUT, PATCH, DELETE)로 표현합니다.**
* **계층 관계를 나타낼 때는 슬래시(/) 구분자를 사용합니다.**
* **URL 마지막에는 슬래시(/)를 포함하지 않습니다.**
* **불가피하게 긴 경로를 가질 경우 하이픈(-)을 사용하며, 언더바(_)는 사용하지 않습니다.**
* **파일 확장자는 URL에 포함하지 않습니다.**


### 3. try-with-resources를 활용한 자원 관리
데이터베이스 연결과 같은 외부 자원은 사용 후 반드시 닫아야 메모리 누수를 방지할 수 있습니다. 전통적인 `try-catch` 방식의 **보일러 플레이트(Boilerplate)** 코드를 제거하고 안전하게 자원을 해제하는 방법을 학습합니다.

* **보일러 플레이트**: 로직과 상관없이 자원 해제를 위해 필수적으로 반복되는 코드들입니다.
* **코드 예제**:
```java
// 별도의 close() 호출 없이 블록이 끝나면 자동으로 자원이 반납됩니다.
try (Connection connection = dataSource.getConnection();
     PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
    preparedStatement.setString(1, content);
    preparedStatement.executeUpdate();
} catch (Exception e) {
    logger.error("에러 발생: ", e);
}
```


### 4. 고급 페이징 처리
대용량 데이터를 효율적으로 조회하고, 클라이언트가 UI를 구성하는 데 필요한 부가 정보를 함께 제공하는 법을 학습합니다.

* **학습 내용**:
* `LIMIT`과 `OFFSET`을 이용한 SQL 페이징 쿼리 작성.
* 검색 조건에 따른 **전체 건수(Total Count)** 및 **전체 페이지 수(Total Pages)** 계산.
* 데이터(`data`)와 페이징 정보(`page`, `size`, `totalCount`, `totalPages`)를 결합한 응답 객체 설계.


### 5. 다중 정렬 구현
String.split을 이용하여 정렬 조건을 분리하고 동적으로 ORDER BY문을 적용해봅니다.

* **학습 내용**:
* 다중 정렬 조건(예: `content,desc;reg_dtm,asc`) 파싱 및 SQL 적용.


### 6. Spring Scheduler를 활용한 자동화 작업
정해진 주기마다 시스템이 특정 로직을 자동으로 수행하도록 설정하는 방법을 익힙니다.

* **핵심 어노테이션**: `@EnableScheduling`, `@Scheduled`

* **코드 예제**:
```java
@EnableScheduling // 스케줄링 기능 활성화
@Scheduled(cron = "0 * * * * *") // 초-분-시-일-월-요일
@Scheduled(fixedRate = 60000) // 1분마다 실행 (60,000ms = 1분)
```


### 7. Swagger(OpenAPI)를 이용한 API 문서 자동화
협업 효율성을 높이기 위해 백엔드 API 명세를 자동으로 문서화하고 테스트 환경을 구축합니다.

* **Dependency**: `implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0'`
* **핵심 어노테이션**: `@Tag`, `@Operation`, `@ApiResponses`, `@ApiResponse`, `@Parameter`, `@io.swagger.v3.oas.annotations.parameters.RequestBody`, `@Content`, `@Schema`


### 8. JDBC
DB 접속 정보를 설정하고 `schema.sql`, `data.sql`을 통한 초기 데이터 세팅법을 학습하고 `PreparedStatement`를 이용한 **SQL Injection** 방지 방법을 학습합니다.
`PreparedStatement`는 SQL 쿼리의 구조를 미리 컴파일하고 사용자 입력값을 단순한 파라미터(데이터)로 취급함으로써 입력값이 실행 코드로 해석되는 것을 원천 차단하여 이를 방지합니다.

* **SQL Injection**: 악의적인 SQL 구문을 주입하여 데이터베이스를 비정상적으로 조작하는 공격 기법
* **Dependency**: `implementation 'org.springframework.boot:spring-boot-starter-jdbc'`
* **properties**:
```
# DB 접속 정보(JDBC)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

# 초기화 스크립트 실행 모드(JDBC)
spring.sql.init.mode=embedded
```


### 9. H2DB
h2-console 설정 및 활용법을 학습합니다.

* **Dependency**: `implementation 'com.h2database:h2'`
* **properties**:
```
# H2 콘솔 정보(H2DB)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```


### 10. JavaDocs
코드의 가독성을 높이고 협업 시 API 및 로직의 의도를 명확히 전달하기 위한 표준 문서화 주석 작성법을 학습합니다.

* **표준 태그 활용**: `@param`(파라미터 설명), `@return`(반환값 설명), `@throws` 또는 `@exception`(예외 상황 설명) 등 Java 표준 태그 사용법 숙지.
* **코드 예제**:
```java
/**
 * 버리고 싶은 감정 등록
 * @param params 감정 내용(content)과 태그(tag)를 포함한 맵 객체
 * @return 성공 시 201 Created, 필수값 누락 시 400 Bad Request, 서버 오류 시 500 Internal Server Error
 */
```

—

## 💡 배운 점 및 고민한 흔적
### 1. Fat Controller 아키텍처와 계층 분리의 필요성
실무에서 흔히 쓰이는 **MVC(Model-View-Controller) 패턴**은 계층별로 역할을 엄격히 나누어 개발합니다.  
하지만 작은 기능을 하나 만들더라도 Controller, Service, DAO, DTO 등 **생성해야 할 파일이 너무 많다**고 느껴질 때가 있었습니다.
그래서 이번 프로젝트에서는 MVC 패턴 대신 **'Fat Controller'** 라고 불리는 방식을 사용하여 서비스와 DAO에서 제공할 기능을 모두 컨트롤러에 담았습니다.

* **Fat Controller**: MVC 중 `M(Model)`이 담당해야 할 데이터 접근 로직과 비즈니스 로직을 `C(Controller)`가 모두 떠안은 형태입니다.
* **결론**: 코드를 모두 컨트롤러 파일에 작성하니 개발 속도는 확실히 빨랐으며, 기능이 적은 경우에는 서비스와 DAO를 같이 개발하는 것보다 컨트롤러 하나만 개발하는 것이 더 낫다는 생각도 들었습니다.   
하지만 기능이 조금만 복잡해져도 **컨트롤러의 코드가 수백 줄**로 불어났고, 쿼리까지 같이 컨트롤러에 존재하니 가독성이 많이 떨어지는 것을 경험했습니다. 프로젝트의 규모와 복잡도가 많이 낮은 경우에는 **'Fat Controller'** 방식은 충분히 고려해 볼 만한 방식이지만, 유지보수와 추후 확장성을 생각해본다면 실무에서 번거롭더라도 MVC 계층을 분리하는 이유를 분명히 알 수 있었습니다.   

### 2. @RequestParam과 @io.swagger.v3.oas.annotations.parameters.RequestBody 차이점
RequestParam 이나 PathVariable 등 은 데이터 '하나'에 대한 정보라서 Parameter어노테이션으로 부가설명을 추가해줘야한다.   
RequestBody는 데이터 '뭉치' = Map 이기 때문에 얘만 스웨거에 있는 @io.swagger.v3.oas.annotations.parameters.RequestBody로 지정해서 부가설명을 추가해줘야한다.   

### 3. 자바의 변수 스코프(Scope)에 대한 이해 
`try-catch-finally` 문법을 사용할 때는 `try` 블록 위에 `Connection`과 `PreparedStatement` 객체를 생성했었는데,`try-with-resources` 문법을 사용할 때는 그럴 필요가 없었습니다. 왜 `try-catch-finally` 문법을 사용할때는 `try` 위에 객체들을 미리 선언했었는지 생각해보니, `try`안에서 생성한 변수들은 `finally`에서 접근하지 못한다는 사실을 알았습니다. 자바의 변수 스코프(Scope;범위)는 블록(`{}`) 단위로 구분됩니다. 이렇게 블록 단위로 구분되는 변수 스코프를 블록 스코프라고 부릅니다.

* **블록 스코프(Block Scope):** * 자바는 중괄호 `{ }`를 기준으로 변수의 생명주기가 결정됩니다.
    * `try` 블록 내부에서 선언된 객체는 `finally` 블록에서 접근할 수 없습니다.
    * 따라서 자원 해제(`close()`)를 위해 `finally`를 사용한다면, 반드시 **블록 외부**에서 변수를 미리 선언해야 합니다.  