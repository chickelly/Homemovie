"# homemovie" 
# Homemovie(영화 구매 시스템) #
# Table of contents
- [영화 구매 시스템 ](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)

# 서비스 시나리오 

[기능적 요구사항]
1.	고객이 영화를 선택하여 주문한다.
2.	고객이 결제한다.
3.	구매가 결제되면, 신청 내역이 Movie시스템에 전달된다.
4.	Movie 시스템은 신청 내역을 전달 받으면, 신청한 영화를 구매한다.
5.	고객이 영화를 시청한다.
6.	고객은 신청 내역을 조회할 수 있다.
7.	고객은 영화 신청 내역을 취소할 수 있다.

[비기능적 요구사항]
1.	트랜 잭션
  :	결제가 되지 않은 신청 영화는 시스템에 등록 되지 않아야 한다. (sync 호출)
2.	장애 격리
  : 영화 시청 기능이 작동되지 않더라도, 영화 신청은 365일 24시간 받을 수 있어야 한다. (Async)
  :	결제 시스템이 과중되면 사용자를 잠시동안 받지 않고 결제를 잠시후에 하도록 유도한다. (circuit breaker, fallback)
3.	성능
  :	고객이 마이 페이지에서 영화 신청 내역을 조회할 수 있다 (CQRS)


# 분석/설계 

[이벤트 스토밍]



[서브 도메인 , 바운디드 컨텍스트 분리] 



[컨텍스트 매핑/이벤트 드리븐 아키텍쳐]


![image](https://user-images.githubusercontent.com/86760605/132372530-4da7a718-0b07-4980-877f-4644c7bed35a.png)

    -

헥사고날 아키텍처 다이어그램 도출


  - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
  - 호출관계에서 Pub/Sub 과 Req/Resp 를 구분함
  - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현 

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8084, 8088 이다)




mvn spring-boot:run  

![image](https://user-images.githubusercontent.com/86760605/132375907-04d57530-f023-455f-95f9-ccec2f84febc.png)





DDD(Domain-Driven-Design)의 적용
msaez.io 를 통해 구현한 Aggregate 단위로 Entity 를 선언 /구현을 진행하였다. 
Entity Pattern 과 Repository Pattern을 적용하기 위해 Spring Data REST 의 RestRepository 를 적용하였다.

MovieApplicaiton.java 

package homemovie;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;
import java.util.Date;

@Entity
@Table(name="MovieApplication_table")
public class MovieApplication {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long appId;
    private String userId;
    private String movieName;
    private Long movieId;
    private String status;

    @PostPersist
    public void onPostPersist(){
       MoviePicked moviePicked = new MoviePicked();
       BeanUtils.copyProperties(this, moviePicked);
        //moviePicked.setStatus("Moviepicked");
       moviePicked.publishAfterCommit();

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        homemovie.external.Payment payment = new homemovie.external.Payment();
        // mappings goes here
    
        payment.setAppId(moviePicked.getAppId());
        payment.setMovieId(moviePicked.getMovieId());
        payment.setMovieName(moviePicked.getMovieName());
        payment.setStatus("Paid");
        payment.setUserId(moviePicked.getUserId());      
        AppApplication.applicationContext.getBean(homemovie.external.PaymentService.class)
            .pay(payment);

    }


    @PostUpdate
    public void onPostUpdate(){
        System.out.println("\n\n##### app onPostUpdate, getStatus() : " + getStatus() + "\n\n");
        if(getStatus().equals("MovieAppCancelled")) {
            MovieAppCancelled movieAppCancelled = new MovieAppCancelled();
            BeanUtils.copyProperties(this, movieAppCancelled);
            //movieAppCancelled.setStatus("MovieAppCancelled");
            movieAppCancelled.publishAfterCommit();
        }
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

Request-Response 방식의 서비스 중심 아키텍처 구현


CQRS





## 폴리글랏 프로그래밍 

mypage>pom.xml

![image](https://user-images.githubusercontent.com/86760605/132375275-1f6d60d4-1229-4eb2-8d0c-5b3214093a7b.png)

![image](https://user-images.githubusercontent.com/86760605/132375292-eb4bf12f-4910-4b71-bf48-22207f697f41.png)




# pom.xml - in myPage 인스턴스

		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
..............

## API 게이트 웨이





## 동기식 호출 과 Fallback 처리 

서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
시나리오는 신청서비스(app) -> 결제(payment) 시의 연결을 RESTful Request/Response로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CircuitBreaker를 통하여 장애 격리.
 
Hystrix를 설정: 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
 
 



## CQRS

- Table 구조


