# Testing through Reactor

---

## `build.gradle`

```groovy
dependencies {
	testCompile 'io.projectreactor:reactor-test'
}
```

## `StepVerifier`

### Usage

```java
StepVerifier.create(<T>Publisher)
        .{expectations}
        .verify();
```

### Example

```java
public <T> Flux<T> appendBoomError(Flux<T> source) {
  return source.concatWith(Mono.error(new IllegalArgumentException("boom")));
}
```

```java
@Test
public void testAppendBoomError() {
  Flux<String> source = Flux.just("thing1", "thing2"); 

  StepVerifier.create(appendBoomError(source)) // Or appendBoomError(source).as(StepVerifier::create) 
    .expectNext("thing1") 
    .expectNext("thing2")
    .expectErrorMessage("boom") 
    .verify(); 
}

```

### Manipulating Time

```java
StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofDays(1)))
// ... Continue Expectations
```

- Verifier가 Core Scheduler (`Scheduler.parallel()`) 대신 `VirtualTimeScheduler`로 임의 교체하여 테스트에 소요되는 시간의 빨리 감기를 수행한다.
- Virtual Time Guarantee를 위해서 `Supplier<Publisher<T>>`는 lazy하게 선언되어야 한다.
- expectation method로 두 가지가 존재한다.
  - `thenAwait(Duration)` : Step 검증이 일시 중단된다.
  - `expectNoEvent(Duration)` : 주어진 시간동안 시퀀스를 재상하지만 그 시간 동안 시그널이 하나라도 발생되면 테스트가 실패된다.
    - 이 메서드는 `subscription`도 하나의 이벤트로 간주하기 때문에 보통 첫번째 Step에 사용하면 구독 신호가 감지되어 실패한다. 따라서 `expectSubscription().expectNoEvent(Duration)`을 사용하자.

```java
StepVerifier.withVirtualTime(() -> Mono.delay(Duration.ofDays(1)))
    .expectSubscription()
    .expectNoEvent(Duration.ofDays(1)) // 혹은 .thenAwait(Duration.ofDays(1))
    .expectNext(0L)
    .verifyComplete(); // verify()를 사용하면 Duration이 반환되어 테스트 동안 실제 걸린 시간이 포함된다.
```

> Virtual Time은 infinite sequence를 사용하면 메우 제한적이라, 시퀀스와 검증을 실행할 스레드를 모두 독차지할 수 있다.


### Performing Post-execution Assertions with `StepVerifier`

- 원한다면 시나리오 상 마지막 expectation 다음 `verify()` 트리거 대신 다른 `Assertion` API 전환이 가능하다.
  - 이 경우 `verifyThenAssertThat()`을 사용하면 된다.

### Manually Emitting (`TestPublisher`)

```java
public Flux<String> processOrFallback(Mono<String> source, Publisher<String> fallback) {
    return source
            .flatMapMany(phrase -> Flux.fromArray(phrase.split("\\s+")))
            .switchIfEmpty(fallback);
}
```

```java
@Test
public void testSplitPathIsUsed() {
    StepVerifier.create(processOrFallback(Mono.just("just a  phrase with    tabs!"),
            Mono.just("EMPTY_PHRASE")))
                .expectNext("just", "a", "phrase", "with", "tabs!")
                .verifyComplete();
}

@Test
public void testEmptyPathIsUsed() {
    StepVerifier.create(processOrFallback(Mono.empty(), Mono.just("EMPTY_PHRASE")))
                .expectNext("EMPTY_PHRASE")
                .verifyComplete();
}
```

```java
private Mono<String> executeCommand(String command) {
    return Mono.just(command + " DONE");
}

public Mono<Void> processOrFallback(Mono<String> commandSource, Mono<Void> doWhenEmpty) {
    return commandSource
            .flatMap(command -> executeCommand(command).then()) 
            .switchIfEmpty(doWhenEmpty); 
}
```


```java
@Test
public void testCommandEmptyPathIsUsed() {
    PublisherProbe<Void> probe = PublisherProbe.empty(); 

    StepVerifier.create(processOrFallback(Mono.empty(), probe.mono())) 
                .verifyComplete();

    probe.assertWasSubscribed(); 
    probe.assertWasRequested(); 
    probe.assertWasNotCancelled(); 
}
```