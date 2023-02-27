# Streaming Processing, Reactive Stream, Project Reactor, Spring WebFlux

## Streaming Processing

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-1.png)

- 전통적 데이터 처리 방식은 데이터 처리 요청이 왔을 때 Payload 전체를 애플리케이션 메모리에 저장한 후 다음 처리를 해야 한다. 추가로 필요한 데이터도 스토리지에서 조회해서 메모리에 로드해야 한다.
  - 전달된 데이터는 물론 스토리지에서 조회한 데이터까지 모든 데이터가 애플리케이션 메모리에 로드되어야 Response를 만들 수 있다. 필요한 데이터 크기가 메모리 사이즈보다 클 경우 OOM(Out Of Memory)이 발생할 수 있다. 혹은 순간적으로 많은 요청이 몰릴 경우 다량의 GC가 발생해 서버가 정상적으로 응답하기 어렵다.
- 많은 양의 데이터를 처리하는 애플리케이션에 스트림 처리 방식을 적용하면 크기가 작은 시스템 메모리로도 많은 양의 데이터를 처리할 수 있다.
  - 입력 데이터에 대한 파이프라인을 만들어 데이터가 들어오는대로 물 흐르는듯이 Subscribe하고, 처리한 뒤 Publish까지 한 번에 연결하여 처리 가능하다. 이 경우 서버는 많은 양의 데이터도 탄력적인 처리가 가능하다.



## Reactive Stream



Standard Specification of **Asynchronous Stream Processing**

> *Reactive Streams is a standard for asynchronous data processing in a streaming fashion with non-blocking backpressure.*
>
> Non-blocking Backpressure를 이용해 비동기 스트림 처리의 표준을 제공
>
> 비동기의 경계를 명확히 하여 스트림 데이터의 교환을 효과적으로 관리하는 것 - 데이터 처리가 목적지의 리소스 소비를 예측 가능한 범위에서 신중하게 제어할 수 있어야 한다.

1. 잠재적으로 무한한 숫자의 데이터 처리
2. 순서대로 처리
3. 컴포넌트 간 데이터를 비동기적으로 전달
4. Backpressure를 이용한 데이터 흐름 제어



### Asynchronous

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-2.png)

- 동기 방식에서는 클라이언트가 서버에 요청하면 응답을 받기 전까지 Blocking된다. -> 현재 스레드가 다른 일을 하지 못하고 대기한다.
- 비동기 방식에서는 현재 스레드가 Blocking되지 않는다. -> 다른 일을 계속할 수 있다.
  - **빠른 속도** : 여러 요청을 동시에 보낼 수 있어 더 빠른 응답 시간을 보여준다.
  - **적은 리소스 사용** : 현재 스레드가 Blocking되지 않고 다른 업무를 처리할 수 있어 더 적은 수의 thread로 더 많은 양의 처리가 가능하다.



### Backpressure?

#### Observer Pattern Push 방식

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-3.png)

- Publisher가 Subscriber에게 밀어넣는 방식으로 데이터가 전달된다.
- Subscriber의 상태를 고려하지 않고 데이터 전달하는 것에만 충실하다.

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-4.png)

- Publisher가 단위 시간당 보내는 이벤트 양과, Subscriber가 단위 시간당 처리할 수 있는 이벤트 양에 차이가 있는 경우 Queue를 이용해 대기 중인 이벤트를 저장해야 한다.
- 서버의 가용 메모리는 한정되어 있으므로 버퍼가 Overflow가 되는 경우 버퍼 타입에 따라 다음과 같은 현상이 발생한다.
  - ![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-5.png)
    - 고정 길이 버퍼의 경우 신규로 수신된 메시지를 거절한다. 거절된 메시지는 재요청하게 되는데, 이 과정에서 네트워크와 CPU 연산 비용이 추가로 발생한다.
  - ![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-6.png)
    - 가변 길이 버퍼의 경우 OOM 에러가 발생하면서 서버 Crash가 발생한다.
- 이와 같은 문제 해결을 위해서 Publisher가 데이터를 전달할 때 Subscriber가 필요한 만큼만 전달해야 한다.

#### Observer Pattern Pull 방식

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-7.png)

- Subscriber가 처리 가능한 개수만큼 Publisher에게 요청하고, Publisher는 요청받은 만큼만 데이터를 전달한다. 이렇게 될 경우 OOM 에러의 걱정이 없다.
  - 이와 같이 Pull 방식에서는 전달되는 모든 데이터의 크기를 Subscriber가 결정한다.
  - **Backpressure란 Subscriber가 수용 가능한 만큼 데이터를 요청하는 방식이다.**



### APIs

스트림은 서로 유기적으로 엮여서 흘러야 의미가 있는데, 이를 위해 공통의 스펙을 설정하고 구현하는 표준화가 필요하다. Reactive Stream은 표준화된 Stream API이다.

- RxJava (Netflix), WebFlux (Pivotal), Akka (Lightbend)

#### Interfaces

- Publisher
  - **데이터를 생성하고 통지한다.**

- Subscriber
  - **통지된 데이터를 전달받아서 처리한다.**

- Subscription
  - **전달받을 데이터 개수를 요청**하고 구독 해지한다.

- Processor
  - Publisher와 Subscriber 기능 모두를 가진다.


```java
public interface Publisher<T> {
   public void subscribe(Subscriber<? super T> s); 	// Subscriber가 Publisher에게 구독 요청
}
 
public interface Subscriber<T> {
   public void onSubscribe(Subscription s); 				// Publisher가 Subscriber에게 Subscription 전달
   public void onNext(T t);													// 데이터 전달
   public void onError(Throwable t);								// 전달 오류
   public void onComplete();												// 전달 성공
}
 
public interface Subscription {
   public void request(long n);											// Subscriber가 Publisher에게 데이터 요청
   public void cancel();														// 구독 취소
}
```
### 추가 설명
- 내가 구독하고 싶은 Publisher가 있으면 걔에 대해서 구독하겠다는 메소드를 호출해. publisher.subscribe()를 실행시켜서 퍼블리셔를 구독함.
---
- Subsciber 본인은 본인이 구독할 준비가 되었다며, Subscription을 만들어서 자기가 받을 양을 선언해놔. subscription.request()로.
  - 그리고 그렇게 만든 subsription을 가지고 Subscriber.onSubscribe(subscription). 나 구독할 준비 됐음!을 말함.
- Publisher는 요청 받은 개수만큼 subsciber에게 데이터를 통지해. 나를 구독하고 있는 subscriber야, subscriber.onNext(내 데이터);
- Publisher는 subsriber에게, 나 너에게 데이터 다 통지했어! 라고 알리기 위해서 subsriber.onComplete를 호출해.

메소드를 가지고 있는 애는 메소드 내용 동작의 주체가 아니야. 
메소드 내용에 해당되는 동작을 실제로 한 애가, 이 동작에 대해 알고 싶은 애한테 알려주는건데, 알고 싶은 애가 그 메소드를 갖고 있는거지. 
즉, 행동주체가, '행동을알고싶은애.메소드()'를 호출하는 구조
- subsribe()는 구독하는 행위의 주체는 구독자야. 나를 구독하는지 알고 싶은 건 생성자고.
  - 따라서 행위의 주체인 구독자가, 생성자.subscribe()를 호출함.
- onNext()는 데이터를 통지하는 행위야. 행위의 주체는 생성잔데, 이걸 알고 싶은 건 구독자라서 구독자가 메소드를 갖고 있어.
  - 따라서 행동주체인 생성자가, 이 행위를 알아내고 싶어하는 구독자.onNext()를 호출
- onComplete는 데이터를 마무리한단 행위야. 행위의 주체는 생산자인데, 이 행위를 알고 싶어하는 앤 구독자야.
  - 생성자 publisher가 이 행위를 알고 싶어하는 구독자.onComplete()를 호출


![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-9.png)

![img](https://engineering.linecorp.com/wp-content/uploads/2020/02/reactivestreams1-10.png)

### Hot versus. Cold

https://projectreactor.io/docs/core/snapshot/reference/#reactor.hotCold

- Cold란 Subscribe할 때마다 매번 독립적으로 새로 데이터를 생성해서 동작함을 의미.
  - 즉 subscribe 호출 전까지 아무런 동작을 하지 않고, 호출되면 그제서야 새로운 데이터를 생성한다.
  - 특별하게 Hot을 취급하는 Operator가 아닌 이상 Cold로 동작한다. (Project Reactor)
- 반대로 Hot의 경우 구독 전부터 데이터 스트림이 동작할 수 있다.
  - e.g. 마우스 클릭이나 키보드 입력 같은 이벤트성은 구독 여부와 상관없이 발생하고 있다가 해당 이벤트를 구독하는 여러 Subscriber가 붙으면 해당 이벤트가 발생할 때 모두 동일한 값을 전달받을 수 있다.
  - 즉 Hot에 해당하는 스트림을 여러 곳에서 구독하면 현재 스트림에서 나오는 값을 구독하는 Subscriber들은 동일하게 받을 수 있다.



### Protocols

> The benefits of asynchronous processing would be negated if the communication of back pressure were synchronous (see also the Reactive Manifesto), therefore care has to be taken to mandate fully non-blocking and asynchronous behavior of all aspects of a Reactive Streams implementation.

- Publisher가 생성하고 Subscriber가 소비하는 모든 Signal은 Non-Blocking이어야 한다.
  - Reactive Streams를 구현함에 있어서 Publisher / Subscriber 구현에 Blocking 동작이 존재해서는 안된다.
- 프로그램 정확성을 위해서 외부 동기화를 사용하지 않고 호출되어야 한다. (Thread-safe)
  - Signal이 순차적으로만 이루어질 수밖에 없다.
- 병렬화
  - ![img](https://blog.kakaocdn.net/dn/bHyjlv/btqTrTnqDmB/sMqlve1rmNKJvF6KG7HUE0/img.png)
  - 각 단계를 비동기적으로 메시지를 전달
    - 처리 단계별로 별도의 스레드에 바인딩한다.
    - 각 구현체에서는 이를 지원하는 Scheduler API (추상화된 ThreadPool Strategy)를 제공한다.



## Project Reactor

Reactive Stream 구현체로 Spring Framework 개발사인 Pivotal에서 개발한 오픈소스이다.

#### 특징

- 13년 7월 reactor 1.0 릴리즈
  - Reactor 패턴, 함수형 프로그래밍, 메시지 기반 등의 설계와 Best Practices를 통합
  - 당시 Spring 개발팀에서 대용량 데이터 개발을 단순화하는 Spring XD Project를 개발 중이었다.
    - 해당 프로젝트가 17년 7월 Deprecated 되었고 Spring Cloud Data Flow가 이를 대체하고 있다.
    - Spring XD에 Asynchronous Non-Blocking을 Support하기 위해 만들어진 프로젝트가 Reactor이다.
    - 장점 : Spring과의 완벽한 통합, Netty supported, High-Performance, Asynchronous Non-Blocking 메시지 처리
    - 단점 : Backpressure 기능 없음 (Reactive Streams 표준 스펙 발표 이전 시기임), 복잡한 오류 처리
- 15년 3월 reactor 2.0 릴리즈
  - reactor 1.0의 단점 개선
    - Backpressure 지원 (onOverflowBuffer(), onOverflowDrop(), etc,.)
- Reactive Streams 표준 스펙 발표 이후 해당 스펙을 2.5 Milestone 부터 지원 (16년 2월), 3.0 릴리즈 (16년 8월)
- JDK 8부터 지원
- `Publisher<T>` 구현체
  - `Flux` / `Mono` *<u>(별도 문서에서 기술)</u>*



## Spring WebFlux

![img](https://blog.kakaocdn.net/dn/bizkcj/btqwFAP9IfT/PrvFnmR7XJf2oKKkc56mlK/img.png)

Non-Blocking 런타임 (Netty) 기반의 Spring Web Framework. 비동기 처리의 특징인 적은 리소스 사용과 빠른 처리를 지원한다.

Java 언어 기반으로 개발하는 경우 Reactive API로 Project Reactor를, Kotlin 언어 기반으로 개발하는 경우 Couroutine API를 WebFlux에서 사용할 수 있다.



### Concurrency Model

- Spring MVC의 경우 애플리케이션이 처리 중인 Thread가 잠시 중단될 수 있다. (Servlet API가 Blocking 하기 때문) 그렇기 때문에 Servlet Container가 Blocking을 대비하여 큰 ThreadPool로 요청을 처리한다.
- Spring WebFlux의 경우 실행 중인 Thread가 중단되지 않는다는 전제가 있다. (Servlet 3.1+ API 혹은 Netty가 Non-Blocking이 지원되므로) 따라서 작은 ThreadPool (Event Loop Worker - 하단 기술)을 고정해놓고 요청을 처리한다.



> #### WebFlux 애플리케이션은 어떤 Thread를 얼마나 실행할까?
>
> 최소한의 설정으로 WebFlux 서버를 띄우는 경우 (예를 들어 데이터 접근이나 다른 dependency가 없다고 가정) 서버는 Thread 1개로 운영하고, 소량의 Threads로 요청을 처리할 수 있다. (일반적으로 실행되는 머신의 CPU 코어 수만큼)
>
> 반면 Servlet 컨테이너 (MVC 애플리케이션) 는 Servlet Blocking I/O (그리고 Servlet 3.1+의 Non Blocking I/O) 에 의해 더 많은 Thread를 실행할 것이다. (e.g. Tomcat min default 10개)



### Event Loop

![img](https://blog.kakaocdn.net/dn/bCXdOE/btq0ZLnHZhk/5HXudI4kjhJtQ4d0uw2o3k/img.png)

- Spring WebFlux에서 사용되는 모델.

- 하나의 Thread에서 계속 돌게 된다.

  - CPU 코어에 따라 여러 개를 돌릴 수도 있긴 하다.
    - ![img](https://blog.kakaocdn.net/dn/tN9mD/btq0XpeR7yp/MZM55asOAtlHsS6tJNZZuK/img.png)
  - 이벤트를 Event Queue에서 순차적으로 대기하고 플랫폼에 Callback이 등록되면 곧바로 반환된다.
    - 데이터베이스 읽기 요청 이벤트를 보내면 Event Queue에서 대기한다.
    - 자신의 차례가 되었을 때 요청 콜백을 등록하고 곧바로 반환된다.
  - 콜백의 이벤트 완료 통지를 받으면 요청을 보낸 Caller에게 결과를 전달한다.
  - **Netty**, Nginx, Node 등에서 사용되는 모델이다.

  

> #### Thread-per Request Model
>
> ![img](https://blog.kakaocdn.net/dn/bwYbT1/btq0Y48UFEv/8xuuaUixkwKQDw82h4L840/img.png)
>
> Spring MVC에서 사용되는 모델. Thread 하나당 하나의 요청이 처리된다. (Multi-core 환경에서 여러 개의 스레드가 여러 개의 요청을 처리한다.)
>
> - 하나의 요청 (Thread)에 대한 처리 시간이 긴 경우 Blocking 된다.
> - Java 에서 사용하는 Native Thread는 Context Switching 비용이 매우 크다.
> - 요청이 많아질 경우 효율적인 방식은 아니다.



---

## References

- https://engineering.linecorp.com/ko/blog/reactive-streams-with-armeria-1/
- https://reactive-streams.org
- https://github.com/reactive-streams/reactive-streams-jvm
- https://sjh836.tistory.com/182
- https://godekdls.github.io/Reactive%20Spring/springwebflux/
- https://velog.io/@jihoson94/EventLoopModelInSpring
