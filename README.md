# 개별과제 - 음악회 표예매(예술의 전당) 시스템
# SAC


# 서비스 시나리오

예술의 전당, 음악회 표 예매 Portal

기능적 요구사항
1.공연자가 공연을 등록한다.
2.고객이 공연 표를 예매한다.
3.고객이 공연 표를 취소할 수 있다.
4.공연자가 공연을 취소한다.
5.공연자가 공연을 취소하면, 공연 표 예매를 취소한다.
6.공연 표가 예매되거나 취소되면 참가인원수를 변경한다.

비기능적 요구사항
1. 장애격리
    1. 강의 관리 기능이 수행되지 않더라도 수강신청은 365일 24시간 받을 수 있어야 한다  Async (event-driven), Eventual Consistency



# 분석/설계

## TO-BE 조직 (Vertically-Aligned)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://msaez.io/#/storming/25xfhEqq7JS3ZlYEB44C4PN3dkT2/mine/d2a91ae4e123bd34d3b2fdd608ec8939/-M5VP2nfhFJ2OWxUwTE1




### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

    - 학생이 강의를 선택하여 수강신청 한다 (ok)
    - 학생이 결제한다 (ok -sync)
    - 수강신청이 되면 수강신청 내역이 강사의 강의시스템에 전달된다 (ok - event driven)
    - 학생이 수강신청을 취소한다 (ok)
    - 수강신청이 취소되면 결제가 취소된다 (ok)
    - 강사가 강의를 개설한다 (ok)
    - 강사가 개설된 강의를 취소한다 (ok)
    - 강사가 강의를 취소하면 학생의 수강신청이 취소된다 (ok)
    - 학생이 수강신청 내용을 조회한다 (view)
    - 강사가 강의수강 인원을 조회한다 (view)

### 요구사항을 커버하도록 모델링됨

## 헥사고날 아키텍처 
    
    - Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
    - 호출관계에서 PubSub 구현
    - 서브 도메인과 바운디드 컨텍스트의 분리:  각 팀의 KPI 별로 아래와 같이 관심 구현 스토리를 나눠가짐


# 구현

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 808n 이다)

```
cd courseRegistrationSystem
mvn spring-boot:run

cd paymentSystem
mvn spring-boot:run 

cd lectureSystem
mvn spring-boot:run  
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: (예시는 paymentSystem 마이크로 서비스). 이때 가능한 현업에서 사용하는 언어 (유비쿼터스 랭귀지)를 그대로 사용하였다. 모델링 시에 영문화 완료하였기 때문에 그대로 개발하는데 큰 지장이 없었다.

```
package skademy;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="PaymentSystem_table")
public class PaymentSystem {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long courseId;

    @PostPersist
    public void onPostPersist(){
        try {
            Thread.currentThread().sleep((long) (400 + Math.random() * 220));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PaymentCompleted paymentCompleted = new PaymentCompleted();
        BeanUtils.copyProperties(this, paymentCompleted);
        paymentCompleted.publish();
    }

    @PostRemove
    public void onPostRemove(){
        PaymentCanceled paymentCanceled = new PaymentCanceled();
        BeanUtils.copyProperties(this, paymentCanceled);
        paymentCanceled.publish();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
}
```
- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB) 에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다
```
package skademy;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface PaymentSystemRepository extends PagingAndSortingRepository<PaymentSystem, Long>{
}
```
- 적용 후 REST API 의 테스트
```
# courseRegistrationSystem 서비스의 수강신청 처리
http POST localhost:8081/courseRegistrationSystem lectureId=1
```
![image](https://user-images.githubusercontent.com/48303857/79857038-272bad00-8408-11ea-8096-7f54b482ea54.png)


```
# 주문 상태 확인
http localhost:8081/courseRegistrationSystem
```
![image](https://user-images.githubusercontent.com/48303857/79857153-4d514d00-8408-11ea-83be-cf9e002c9ce5.png)



## 동기식 호출 과 Fallback 처리

분석단계에서의 조건 중 하나로 수강신청(courseRegistrationSystem)->결제(paymentSystem) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하기로 하였다. 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

- 결제서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현 

```
# (courseRegistrationSystem) PaymentService.java

@FeignClient(name ="paymentSystems", url="http://52.231.118.204:8080")
public interface PaymentService {
    @RequestMapping(method = RequestMethod.POST, value = "/paymentSystems", consumes = "application/json")
    void makePayment(PaymentSystem paymentSystem);

}
```

- 수강신청 직후(@PostPersist) 결제를 요청하도록 처리
```
#CourseRegistrationSystem.java (Entity)

    @PostPersist
    public void onPostPersist(){
        CourseRegistered courseRegistered = new CourseRegistered();
        BeanUtils.copyProperties(this, courseRegistered);
        courseRegistered.publish();

        this.setLectureId(courseRegistered.getLectureId());
        this.setStudentId(12334);
        this.setStatus("수강신청중");

        System.out.println("##### POST CourseRegistrationSystem 수강신청 : " + this);

        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        PaymentSystem paymentSystem = new PaymentSystem();
        paymentSystem.setCourseId(this.id);
        // mappings goes here

        //결제 시작
        PaymentService paymentService = Application.applicationContext.getBean(PaymentService.class);
        paymentService.makePayment(paymentSystem);

    }
```

- 동기식 호출에서는 호출 시간에 따른 타임 커플링이 발생하며, 결제 시스템이 장애가 나면 주문도 못받는다는 것을 확인:


```
# 결제(paymentSystem) 서비스를 잠시 내려놓음

#수강신청 처리
http POST localhost:8081/courseRegistrationSystem lectureId=1   #Fail
http POST localhost:8081/courseRegistrationSystem lectureId=2   #Fail
```
![image](https://user-images.githubusercontent.com/48303857/79857341-9a352380-8408-11ea-908a-d776d192bb8e.png)

```
#결제서비스 재기동
cd paymentSystem
mvn spring-boot:run

#수강신청 처리
http POST localhost:8081/courseRegistrationSystem lectureId=1   #Success
http POST localhost:8081/courseRegistrationSystem lectureId=2   #Success
```
![image](https://user-images.githubusercontent.com/48303857/79857434-c05ac380-8408-11ea-88d4-8a6ce4af0100.png)


- 또한 과도한 요청시에 서비스 장애가 도미노 처럼 벌어질 수 있다. (서킷브레이커, 폴백 처리는 운영단계에서 설명한다.)




# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 azure-pipeline.yml 에 포함되었다.

- devops를 활용하여 pipeline을 구성하였고, CI CD 자동화를 구현하였다.
- pod 가 정상적으로 올라간 것을 확인하였다.
- 쿠버네티스에 모두 서비스로 등록된 것을 확인할 수 있다.








