# Operators

## c1_Introduction

- `Mono.block() / Mono.block(Duration timeout)`
  - ![block](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/block.svg) 
  - ![block with duration](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/blockWithTimeout.svg)
  - [해당 Mono Publisher를 구독하고 다음 시그널이 올 때까지 무한정 block한다. Mono에 wrapping된 내부의 값을 반환한다.](https://github.com/reactor/reactor-core/blob/main/reactor-core/src/main/java/reactor/core/publisher/Mono.java#L1706-L1711)
  - Parameter로 Duration(timeout)이 들어가는 경우 다음 시그널이 오는 경우 혹은 timeout이 만료될 때까지 block된다.
- `Mono.blockOptional() / Mono.blockOptional(Duration timeout)`
  - ![blockOptional](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/blockOptional.svg) 
  - block과 동일한데, Return되는 값은 Optional로 wrapping된 값이 반환된다.
- `Flux.blockFirst() / Flux.blockFirst(Duration timeout)`
  - ![blockFirst](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/blockFirst.svg)
  - upstream에서 첫번째 아이템에 대한 signal을 보낼 때까지 Flux Publisher를 무한정 block한다. 첫번째 아이템 value가 반환된다.
- `Flux.collectList()`
  - ![collectList](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/collectList.svg)
  - Flux Publisher에서 emit하는 모든 요소를 List에 묶어서 Mono Publisher로 반환한다.
  - `Flux<Something>` -> `Mono<List<Something>>`
- `.subscribe() / .subscribe(Consumer<? super T> consumer) / .subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer), .subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer), .subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Consumer<? super Subscription> subscriptionConsumer), .subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer, Runnable completeConsumer, Context initialContext)`
  - Publisher를 구독한다. (구독하기 전까지는 elements가 emit되지 않는다.)
  - `consumer` : next signal 처리
  - `errorConsumer` : error signal 처리
  - `completeConsumer` : complete signal 처리
  - `subscriptionConsumer` : Subscriber의 onSubscribe에 대응
  - ![subscribe (consumer, errorConsumer, completeConsumer, subscriptionConsumer)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/subscribeForMono.svg)

  > subscribe 메서드의 return type은 [Disposable](https://github.com/reactor/reactor-core/blob/main/reactor-core/src/main/java/reactor/core/Disposable.java) 이다. 해당 인터페이스는 `dispose()` 메서드 호출로 구독을 취소할 수 있는데 (Source가 elements 생성을 중단), 즉시 중지가 보장되지 않는다. - 몇몇 Source는 elements 생성이 너무 빨라 dispose 전에 완료될 수 있다.

## c2_TransformingSequence

- `.map()`
- `.cast()`
- `.defaultIfEmpty()`
- `.reduce()`
- `.scan()`
- `.startWith()`

## c3_FilteringSequence

- `.filter()`
- `.ofType()`
- `.distinct()`
- `.next()`
- `.take()`
- `.takeLast()`

## c4_LifecycleHooks

- `doOnSubscribe()`
- `doFirst()`
- `doOnNext()`
- `doOnComplete()`
- `doOnCancel()`
- `doOnTerminate()`
- `doFinally()`
- `doOnEach()`

## c5_CreatingSequence

- `Mono.just()`
- `Mono.justOrEmpty()`
- `Mono.fromCallable()`
- `Mono.fromFuture()`
- `Mono.fromRunnable()`
- `Mono.empty()`
- `Mono.error()`
- `Flux.fromArray()`
- `Flux.fromIterable()`
- `Flux.fromStream()`
- `Flux.interval()`
- `Flux.range()`
- `Mono.repeat()`
- `Flux.generate()`
- `Flux.create()`
- `Flux.push()`
