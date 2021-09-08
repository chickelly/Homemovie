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

cd app
mvn spring-boot:run
cd payment
mvn spring-boot:run
cd movie
mvn spring-boot:run
cd mypage
mvn spring-boot:run
cd gateway
mvn spring-boot:run

![image](https://user-images.githubusercontent.com/86760605/132377431-f1c7d3d1-c08f-438d-9ac1-04546a950843.png)


DDD(Domain-Driven-Design)의 적용
msaez.io 를 통해 구현한 Aggregate 단위로 Entity 를 선언 /구현을 진행하였다. 
Entity Pattern 과 Repository Pattern을 적용하기 위해 Spring Data REST 의 RestRepository 를 적용하였다.

< MovieApplicaiton.java >  

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


< app 서비스의 PolicyHandler.java > 

package homemovie;

import homemovie.config.kafka.KafkaProcessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired MovieApplicationRepository movieApplicationRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieCancelled_ChangeStaus(@Payload MovieCancelled movieCancelled){

        if(!movieCancelled.validate()) return;

       System.out.println("\n\n##### listener ChangeStaus : " + movieCancelled.toJson() + "\n\n");

    
       // 상태 변경 - 영화취소됨 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieCancelled.getAppId());
        movieApplication.setStatus(movieCancelled.getStatus());
        movieApplicationRepository.save(movieApplication);          
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieWatched_ChangeStaus(@Payload MovieWatched movieWatched){

        if(!movieWatched.validate()) return;

        System.out.println("\n\n##### listener ChangeStaus : " + movieWatched.toJson() + "\n\n");

        // 상태 변경 - 영화시청함 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieWatched.getAppId());
        movieApplication.setStatus(movieWatched.getStatus());
        movieApplicationRepository.save(movieApplication);          
    }
 
    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverMovieOrdered_ChangeStaus(@Payload MovieOrdered movieOrdered){

        if(!movieOrdered.validate()) return;

        System.out.println("\n\n##### listener ChangeStaus : " + movieOrdered.toJson() + "\n\n");

        // 상태 변경 - 영화 구매됨 //
        MovieApplication movieApplication = movieApplicationRepository.findByAppId(movieOrdered.getAppId());
        movieApplication.setStatus(movieOrdered.getStatus());
        movieApplicationRepository.save(movieApplication);          

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}

}

< app 서비스의 MovieApplicationRepository.java >

package homemovie;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="movieApplications", path="movieApplications")
public interface MovieApplicationRepository extends PagingAndSortingRepository<MovieApplication, Long>{
    MovieApplication findByAppId(Long appId);
}

결과 첨부 

영화가 선택
![image](https://user-images.githubusercontent.com/86760605/132377520-28657d4b-677e-4fc2-b339-536778a6aca8.png)
 
MYPAGE 에서 확인 
 ![image](https://user-images.githubusercontent.com/86760605/132377535-6d25f00f-d79c-47a4-bee0-6d99e35c9385.png)

결재 확인
 ![image](https://user-images.githubusercontent.com/86760605/132377559-5c0a02c1-72f3-4978-9c7c-01ba188d2b8e.png)

영화 시청 완료 
 ![image](https://user-images.githubusercontent.com/86760605/132377567-5846dae8-8a17-499c-97fe-30f9c91d4077.png)

영화 신청 취소 
 ![image](https://user-images.githubusercontent.com/86760605/132377606-63875b56-bc14-4b7b-a044-25e995c64a57.png)

결재 취소
  ![image](https://user-images.githubusercontent.com/86760605/132377625-01f3bc7c-dec1-48ef-8f1d-78e12c54f2fd.png)



## CQRS

타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이)도 내 서비스의 영화 구매 내역 조회가 가능하게 구현해 두었다. 본 프로젝트에서 View 역할은 mypage 서비스가 수행한다.

![image](https://user-images.githubusercontent.com/86760605/132378735-f67dec73-0f7b-45d8-964b-08341c2d137b.png)

![image](https://user-images.githubusercontent.com/86760605/132378714-8d372eb1-3a08-4094-beb0-8f5483b2137c.png)



## 폴리글랏 프로그래밍 

<mypage>pom.xml> 

pom.xml - in myPage 인스턴스

		<dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<scope>runtime</scope>
		</dependency>
..............

![image](https://user-images.githubusercontent.com/86760605/132375275-1f6d60d4-1229-4eb2-8d0c-5b3214093a7b.png)

![image](https://user-images.githubusercontent.com/86760605/132375292-eb4bf12f-4910-4b71-bf48-22207f697f41.png)



## API 게이트 웨이

server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: app
          uri: http://localhost:8081
          predicates:
            - Path=/movieApplications/** 
        - id: movie
          uri: http://localhost:8082
          predicates:
            - Path=/movies/** 
        - id: payment
          uri: http://localhost:8083
          predicates:
            - Path=/payments/** 
        - id: mypage
          uri: http://localhost:8084
          predicates:
            - Path= /myPages/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

            
Gateway 통해 영화 신청/취소 

![image](https://user-images.githubusercontent.com/86760605/132378272-a6ff9ad8-56ec-4121-91a9-a734771c8903.png)

![image](https://user-images.githubusercontent.com/86760605/132378258-0271d3c0-fde5-4135-bb8a-19fbc29f131a.png)


## 동기식 호출 과 Fallback 처리 

서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함
시나리오는 신청서비스(app) -> 결제(payment) 시의 연결을 RESTful Request/Response로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CircuitBreaker를 통하여 장애 격리.
 
Hystrix를 설정: 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정

< external>Paymentservice.java >

package homemovie.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;
@FeignClient(name="payment", url="http://localhost:8083")
public interface PaymentService {
    @RequestMapping(method= RequestMethod.POST, path="/payments")
    public void pay(@RequestBody Payment payment);
}


< Movieapplication.java > 

  homemovie.external.Payment payment = new homemovie.external.Payment();
  // mappings goes here
    
  payment.setAppId(moviePicked.getAppId());
  payment.setMovieId(moviePicked.getMovieId());
  payment.setMovieName(moviePicked.getMovieName());
  payment.setStatus("Paid");
  payment.setUserId(moviePicked.getUserId());      
  AppApplication.applicationContext.getBean(homemovie.external.PaymentService.class)
            .pay(payment);


# 운영
Deploy /Pipeline 

git에서 소스 가져오기

 : git clone https://github.com/chickelly/homemovie

Build
 : mvn package 

Docker Image Build/Push, deploy/service 생성 (yml 이용)

namespace 생성 

 : kubectl create ns jykmovie

![image](https://user-images.githubusercontent.com/86760605/132383338-0be35a04-699f-4add-bae7-aff76dc5a38d.png)


Docker Image 생성/ Build 

![image](https://user-images.githubusercontent.com/86760605/132381013-e9300bba-883d-452d-9fc1-bba6e8e57880.png)

Deploy 
![image](https://user-images.githubusercontent.com/86760605/132382013-94658f00-2fa5-4add-bfaf-d33404f9b5d9.png)


동기식 호출 / 서킷 브레이킹 / 장애격리

# application.yml
	feign hsytrix 사용

	![image](https://user-images.githubusercontent.com/86760605/132442189-d44171c1-063e-4597-9c21-db854545ff80.png)
	
	
	
	< (payment) Payment.java (Entity)> 

    @PostPersist
    public void onPostPersist(){  //결제이력을 저장한 후 적당한 시간 끌기
        ...
        
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    -----------------------------------------------------
        
    부하 테스터 siege 툴을 통한 서킷 브레이커 동작 확인 . 동시 사용자 100명, 60초 동안 실시
		
    ![image](https://user-images.githubusercontent.com/86760605/132442239-d7fb09ba-396c-4e63-9296-bc618c3b83d9.png) 
 
	 
    ![image](https://user-images.githubusercontent.com/86760605/132442264-66301e33-e612-454e-aa1d-d31a581d3cd0.png)

	
    
### 오토스케일 아웃
앞서 CB 는 시스템을 안정되게 운영할 수 있게 해줬지만 사용자의 요청을 100% 받아들여주지 못했기 때문에 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다. 

![image](https://user-images.githubusercontent.com/86760605/132442327-8241196e-a092-475e-9336-0cf1e5fa42e0.png)


![image](https://user-images.githubusercontent.com/86760605/132442335-df75178d-ee65-4568-8d94-5c6fd437073c.png)



## 무정지 재배포







## Config Map






## Persistent Volume


