# Scheduler

`Project Reactor`에서 `Scheduler`란 `Thread`를 관리하는 역할을 담당한다.

`Project Reactor`는 동시성에 구애받지 않고 개발자가 이를 직접 동제할 수 있다.

개발자가 이를 직접 통제할 수 있도록, 복잡한 멀티쓰레딩을 손쉽게 사용할 수 있도록 `Scheduler`가 도와준다.

#### `Project Reactor`는 일반적으로 호출된, 즉 `subscribe()`가 호출된 쓰레드에서 동작을 수행한다.

> 🤔무슨의미?

![스크린샷 2023-03-07 오후 11 25 43](https://user-images.githubusercontent.com/60773356/223450888-7d2efab3-28c7-4572-8f4f-9cf67b409c65.png)

위 사진과 같은 상황에서 새로운 쓰레드에서 `subscribe()`를 호출하고 있기 떄문에 메인 쓰레드가 아닌 호출된 새로운 쓰레드에서 동작을 수행한다.

<br>

---

<br>

## Scheduler의 종류

`reactor.core.scheduler` 하위에는 `Scheduler` 인터페이스를 구현한 여러 구현체들이 존재한다.

추상 클래스인 `Schedulers`에서 제공하는 팩토리 메서드를 사용해 여러 구현체들을 손쉽게 생성하고 사용할 수 있다. 

### 1. ImmediateScheduler

이는 현재 쓰레드에서 작업을 즉시 실행하는 `Scheduler`이다.(`Runnable`이 바로 실행됨)

`Schedulers.immediate()`를 사용해 생성할 수 있다.

작업이 즉시 실행이 되므로 지연시간이 없는 빠른 반응성을 제공하는 특징을 가지고 있다.

또한, 쓰레드 풀이나 큐를 별도로 사용하지 않고 있기 때문에 성능상의 이점이 있다.

하지만 동시성이 고려되지 않은 `Scheduler`이므로 작업이 실행되는 동안 `Blocking`이 될 수 있어, 작업 시간이 길다면 사용을 지양하는 것이 좋다.


<br>

### 2. SingleScheduler

싱글 쓰레드를 사용하여 작업을 처리하는 `Scheduler`이다.

`Schedulers.single()`을 사용해 생성할 수 있다.

`SingleScheduler`는 내부적으로 쓰레드를 캐시하지 않기 때문에 매번 호출할 때마다 새로운 쓰레드를 생성한다.

쓰레드를 하나를 사용하기 때문에 동시성을 보장할 수 있고 이를 위해 내부적으로 쓰레드를 관리한다.(순차적 실행)

하나의 쓰레드를 사용하기 때문에 위와 마찬가지로 별도의 쓰레드 풀이나 큐를 사용하지 않는다.

모든 작업을 하나의 쓰레드에서 처리하기 때문에 작업양이 많은 경우에는 지양하는 것이 좋다.

순차적으로 처리해야하는 비동기 작업(네트워크, DB 연결)과 같은 작업에 유용하게 사용할 수 있다.

호출할 때마다 해당 작업에 대한 전용 쓰레드를 사용하고 싶다면 `Schedulers.newSingle()`를 통해 싱글 쓰레드를 여러개 만들 수 있다.

`SingleScheduler`는 `FluxSink`와 함께 사용하여 비동기 작업을 처리한다.

`FluxSink`는 `Subscriber`와 비슷한 역할을 한다.

#### 엥? 공식문서에는 `Schedulers.single()`를 사용하면 동일한 쓰레드를 재사용할 수 있다는데요?

`SingleScheduler`는 내부적으로 캐시를 하지 않기 때문에 매번 새로운 단일 쓰레드를 생성한다.

하지만 팩토리 메서드가 존재하는 `Schedulers`가 내부적으로 쓰레드를 캐시하기 때문에 `Schedulers.single()`의 반환값이 `SingleScheduler`이지만 매번 동일한 쓰레드를 재사용할 수 있다.


<br>

### 3. SingleWorkerScheduler

`SingleWorkerScheduler`는 `SingleScheduler`와 유사하다.

하지만 `SingleWorkerScheduler`는 내부적으로 쓰레드를 캐시하기 때문에 한번 생성된 뒤에는 매번 똑같은 쓰레드를 재사용한다.

`SingleWorkerScheduler`는 `SchedulerProcessor`를 `Publisher`와 함께 사용하여 작업을 처리한다.

`Publisher`가 작업을 `Processor`에게 전달하고 이 작업을 단일 쓰레드에서 처리한다.

따라서, `SingleScheduler`보다는 더 많은 작업량을 처리할 수 있다.

<br>

### 4. ElasticScheduler

쓰레드 풀을 사용해 작업을 처리하는 `Scheduler`이다.

`Schedulers.elastic()`을 사용해 생성할 수 있다.

쓰레드 풀을 기반으로 하기 때문에 작업량에 따라 쓰레드의 개수를 동적으로 조정할 수 있다.

> 제일 처음 초기화될 시점에 쓰레드 풀의 쓰레드 개수는 0개이다.

따라서, 작업량에 따라 최적의 쓰레딩을 유지하므로 높은 처리량과 성능을 보장한다.

일반적으로, 비동기 처리나 작업이 긴 경우에 유용하게 사용할 수 있다.

하지만, 쓰레드에 대한 제한이 없어 요청시마다 쓰레드를 생성하는 문제가 있어 `Backpressure` 전략을 제대로 지원하지 못해 `BoundedElasticScheduler`로 대체된다.

<br>

### 5. BoundedElasticScheduler

`ElasticScheduler`와 매우 유사하다.

`Schedulers.boundedElastic()`을 사용해 생성할 수 있다.

`ElasticScheduler`와의 차이점으로는 유휴시간이 긴 쓰레드가 있다면 폐기하며 쓰레드 풀의 최대 쓰레드의 수를 제한한다.

> 기본값은 유휴시간은 60초, 쓰레드 수 제한은 `CPU 코어 X 10`이다.

쓰레드 수가 최대치에 도달한뒤의 작업은 100,000개 까지 큐에 담고 작업 가능한 쓰레드가 생긴다면 재스케쥴링을 통해 작업을 처리한다.

너무 많은 작업이 동시에 처리되는 것을 방지하여 작업의 안정성을 높이기 위한 전략이다.

처리량도 높고 안정성도 높기 때문에 대규모의 동시성 작업(HTTP Request, DB Query)에 유용하게 사용할 수 있다.

<br>

### 6. ParallelScheduler

다중 스레드를 사용해 작업을 병렬로 처리하는 `Scheduler`이다.

이는 내부적으로 고정된 쓰레드 풀을 사용한다.

`ParallelScheduler`는 데이터를 작은 여러개의 청크로 쪼개고 해당 청크를 병렬로 처리하고 다시 합치는 방식으로 작업을 처리한다.

기본적으로 CPU 코어 수만큼의 쓰레드를 생성한다.

CPU 코어 수만큼 쓰레드를 생성하기 때문에 작업양이 커 CPU를 많이 사용하더라도 최대한의 성능을 보장한다.

`Reactive Stream`의 `Backpressure`기능과 함께 사용될 때 더욱 안정된 작업을 보장할 수 있다.

`ParallelScheduler`는 대량의 데이터를 처리해야할 때 유용하다.(DB Query, 대규모의 파일 등)

<br>

### 7. DelegateServiceScheduler

이름에서 알 수 있듯이 `DelegateServiceScheduler`는 다른 `Scheduler` 래핑하여 스케쥴링을 위임한다.

생성자를 통해 다른 `Scheduler`를 인자로 받고 해당 `Scheduler`의 기능을 그대로 사용한다.

부가적인 기능(로깅, 예외처리)을 추가할 수 있기 때문에 보다 유연하게 사용할 수 있다.

<br>

### 8. ExecutorScheduler

`Executor`를 기반으로 동작하는 `Scheduler`이다.

`Executor`를 인자로 받아 생성된다.

```java 
public interface Executor {
    void excute(Runnable command);
}

// Executor는 java.util.concurrent 하위에 존재하는 쓰레드를 관리하는 인터페이스로써 쓰레드를 생성하고 쓰레드 풀을 만들고 쓰레드에 작업을 할당하는 역할을 한다.

// 비동기 작업 처리를 위한 인터페이스로써, `Runnable`을 구현한 객체를 실행하는 객체가 구현해야하는 인터페이스이다.

while(true){
  Request request = acceptRequest();
  Runnable requestHAndler = new RequestHandler(request);
  new Thread(requestHandler).start();
}

// 위와 같은 코드에서 요청시마다 새로운 쓰레드를 생성하고 종료한다면 많은 오버헤드가 발생하고 JVM에 부하를 주게되어 메모리 누수가 발생할 수 있다.

// 이를 Executor를 구현한 객체를 통해 해결할 수 있다.

Executor executor = new Executor();

while(true) {
    Request request = acceptRequest();
    Runnable requestHandler = new RequestHandler(request);
    executor.excute(requestHandler);
}
```

따라서 `ExecutorScheduler`는 `Executor`를 사용해 작업을 처리하고, `Executor`는 쓰레드 풀을 제공한다.

`Executor`의 종류에 관계없기 때문에 기존에 존재하던 것을 래핑해 재사용할 수 있다.

<br>

























