# kakaopay
2020 kakaopay 과제
<br>
## 기술스택
* Kotlin
* Spring Boot
* MongoDB
<br>
## 구현 기능
### 1. 뿌리기 API
    `POST` localhost:8080/boradcast
    Content-Type: application/json
### 2. 받기 API
    `PUT` localhost:8080/boradcast/{token}
### 3. 조회 API
    `GET` localhost:8080/boradcast/{token}
<br>

## 해결 전략
1. 각 데이터("사용자", "방", "뿌리기")에 대한 관계성이 필요가 없어 document로 빠르게 작성할 수 있는 MongoDB 사용
2. 여러 인스턴스에서 Transaction을 보장하기 위해 MongoDB 4.4버전을 사용하여 replica set 구현 (해당부분 미제출)
3. Token의 생성 pool이 작아 중복이 발생할 수 있는 부분 DB에서 에러로 처리
4. 7일이 지난 뿌리기건 조회 불가능한 부분 DB에서 TTL처리외에 소스단에서이도 예외처리
5. 각 커스텀 자료구조는 entity로 구현(ex. request, response...)
