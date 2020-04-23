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


# 구현 (레파지토리)

표예매 : https://github.com/naorange/ticketbook

공연(음악회) 관리:   https://github.com/naorange/concert

예매 상황 대시보드 :  https://github.com/naorange/dashboard

게이트웨이:  https://github.com/naorange/gateway

# 운영

## CI/CD 설정


각 구현체들은 각자의 source repository 에 구성되었고, 사용한 CI/CD 플랫폼은 Azure를 사용하였으며, pipeline build script 는 각 프로젝트 폴더 이하에 azure-pipeline.yml 에 포함되었다.

- devops를 활용하여 pipeline을 구성하였고, CI CD 자동화를 구현하였다.
- pod 가 정상적으로 올라간 것을 확인하였다.
- 쿠버네티스에 모두 서비스로 등록된 것을 확인할 수 있다.








