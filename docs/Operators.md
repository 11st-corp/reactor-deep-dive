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
  > subscribe 메서드는 (Both Mono and Flux) Unbounded Request Strategy를 사용한다. 즉, Backpressure가 Default로 사용되지 않는다.

## c2_TransformingSequence

- `.map(Function<? super T, ? extends R> mapper)`
  - ![map](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/mapForFlux.svg)
  - Synchronous Function인 mapper에 따라 각 item에 대한 transform을 진행한다.
- `.cast(Class<E> clazz)
  - ![cast](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/castForMono.svg)
  - Sequence에서 emit되는 각 item에 대해서 지정한 클래스 타입으로 변환한다.
- `.defaultIfEmpty(T defaultV)`
  - ![defaultIfEmpty](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/defaultIfEmpty.svg)
  - Sequence에서 아무런 데이터 emit 없이 complete되는 경우 지정한 Default 데이터를 반환한다.
- `Flux.reduce(A initial, BiFunction<A, ? super T, A) accumulator, Flux.reduce(BiFunction<T, T, T> aggregator)`
  - ![Flux.reduce(initial, accumulator)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/reduce.svg)
  - ![Flux.reduce(aggregator)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/reduceWithSameReturnType.svg)
  - Flux sequence에서 emit되는 값을 단일 값으로 reduce 시킨다. initial parameter가 포함되는 경우 해당 값을 기준으로 하는 reduction이 반영된다.
- `Flux.scan(A initial, BiFunction<A, ? super T, A> accumulator), Flux.scan(BiFunction<T, T, T> accumulator)`
  - ![Flux.scan(initial, accumulator)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/scan.svg)
  - ![Flux.scan(accumulator)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/scanWithSameReturnType.svg)
  - Sequence에서 emit되는 값은 accumulator에 의해서 계산된 중간 결과들로 변환된다. initial 값이 파라미터로 들어가지 않는 경우 Sequence의 첫번째 아이템이 initial로 간주된다.
- `Flux.startWith(Iterable<? extends T> iterable), Flux.startWith(Publisher<? extends T> publisher), Flux.startWith(T ... values)`
  - ![Flux.startWith(iterable)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/startWithIterable.svg)
  - ![Flux.startWith(publisher)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/startWithPublisher.svg)
  - ![Flux.startWith(values)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/startWithValues.svg)
  - 파라미터에 들어가는 값 Sequence를 현재 Sequence에서 가지고 있는 값 앞에 Prepend한다.

## c3_FilteringSequence

- `.filter(Predicate<? super T> p)`
  - ![filter](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/filterForFlux.svg)
  - 주어진 predicate에 대해서 source의 개별 아이템을 판단하여 필터링한다. 
- `.ofType(Class<U> clazz)`
  - ![ofType](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/ofTypeForFlux.svg)
  - 주어진 Class Type에 대해서 source의 개별 아이템을 판단하여 필터링한다.
- `.distinct() / .distinct(Function<? super T, ? extends V> keySelector) / .distinct(Function<? super T, ? extends V> keySelector, Supplier<C> distinctCollectionSupplier)`
  - ![distinct](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/distinct.svg)
  - ![distinct(keySelector)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/distinctWithKey.svg)
  - ![distinct(keySelector, distinctCollectionSupplier)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/distinctWithKey.svg)
  - sequence의 각 아이템에 대하여 unique하지 않은 item들은 제거하여 개별 item이 uniqueness를 유지할 수 있도록 한다.
  - keySelector Function이 파라미터로 제공되는 경우 각 아이템에 해당 Function이 적용된 결과를 기준으로 필터링한다.
  - distinctCollectionSupplier가 파라미터로 제공되는 경우 Sequence의 모든 아이템은 supplier function의 새로운 인스턴스에 추가되고 해당 컬렉션을 기반으로 distinct를 계산한다.
- `.next()`
  - ![next](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/next.svg)
  - Sequence에서 첫번째 Item을 emit한다.
- `.take(Duration timeSpan) / .take(Duration timeSpan, Scheduler timer) / .take(long n) / .take(long n, boolean limitRequest)`
  - ![take(timeSpan)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/takeWithTimespanForFlux.svg)
  - ![take(timeSpan, timer)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/takeWithTimespanForFlux.svg)
  - ![take(n)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/takeLimitRequestTrue.svg)
  - Sequence의 앞에서부터 N개만큼의 값을 emit된다.
  - n = 0일 경우 Source가 구독되지 않으며 operator가 구독 즉시 완료된다.
  - `.take(n)`의 경우 두 번째 파라미터 `limitRequest = true`가 생략된 케이스이다.
  - limitRequest = false로 설정되는 경우 unbounded request가 되므로 publisher와 subscriber의 속도 차이가 발생할 때 이슈가 발생할 수 있다. (Complete signal이 발생한 후에도 값이 계속해서 emit된다.)
  - timespan을 지정하는 경우 해당 시간 동안만 sequence 값 출력을 relay한다.
- `.takeLast(int n)`
  - ![takeLast](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/takeLast.svg)
  - Completion 직전의 N개 값을 emit한다.

## c4_LifecycleHooks

- `doOnSubscribe(Consumer<? super Subscription> onSubscribe)`
  - ![doOnSubscribe](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnSubscribe.svg)
  - Publisher에서 Subscription이 생성되어 Subscriber.onSubscribe로 전달될 때의 behavior를 정의한다. 
- `doFirst(Runnable onFirst)`
  - ![doFirst](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doFirstForFlux.svg)
  - 구독되기 전 실행될 behavior를 정의한다.
  - chaining을 통해 여러 번 사용할 수 있지만, 그 순서는 역방향이다. (구독 신호가 Subscriber에서 Publisher로 흐르기 때문이다.)
- `doOnNext(Consumer<? super T> onNext)`
  - ![doOnNext](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnNextForFlux.svg)
  - item이 emit될 때의 behavior를 정의한다.
- `doOnComplete(Runnable onComplete)`
  - ![doOnComplete](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnComplete.svg)
  - 구독이 Complete했을 때의 Behavior를 정의한다.
- `doOnCancel(Runnable onCancel)`
  - ![doOnCancel](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnCancelForFlux.svg)
  - 구독이 cancel되었을 때의 behavior를 정의한다.
- `doOnTerminate(Runnable onTerminate)`
  - ![doOnTerminate Successful](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnTerminateForFlux.svg)
  - ![doOnTerminate Failed](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnTerminateForFlux.svg)
  - 구독이 complete되었거나 혹은 실패(에러 발생)했을 때의 behavior를 정의한다.
- `doFinally(Consumer<SignalType> onFinally)`
  - ![doFinally](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doFinallyForFlux.svg)
  - 구독이 (cancel을 포함하여) 종료되었을 때의 behavior를 정의한다.
- `doOnEach(Consumer<? super Signal<T>> signalConsumer)`
  - ![doOnEach](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/doOnEachForFlux.svg)
  - 개별 아이템이 emit될 때 성공하거나 실패하는 등 어떠한 경우에나 발생되는 behavior를 정의한다.

## c5_CreatingSequence

- `Mono.just(T data)`
  - ![just](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/just.svg)
  - 구독 시 해당 값을 emit한다.
  - Hot Publisher
- `Mono.justOrEmpty(T data) / Mono.justOrEmpty(Optional<? extends T> data)`
  - ![justOrEmpty](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/justOrEmpty.svg)
  - 구독 시 해당 값을 emit한다.
  - null 값이 있을 경우 onComplete만 emit된다.
- `Mono.fromCallable(Callable<? extends T> supplier)`
  - ![fromCallable](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromCallable.svg)
    - Callable로부터 값을 생성하는 Mono를 생성한다.
- `Mono.fromFuture(Supplier<? extends CompletableFuture<? extends T>> futureSupplier) / Mono.fromFuture(Supplier<? extends CompletableFuture<? extends T>> futureSupplier, boolean suppressCancel)`
  - ![fromFuture(futureSupplier)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromFutureSupplier.svg)
  - ![fromFuture(futureSupplier, suppressCancel)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromFutureSupplier.svg)
  - CompletableFuture로부터 값을 생성하는 Mono를 생성하고, suppressCancel == false인 경우 Mono가 취소되었을 경우 CompletableFuture 또한 취소된다.
- `Mono.fromRunnable(Runnable runnable)`
  - ![fromRunnable](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromRunnable.svg)
  - Runnable로부터 값을 생성하는 Mono를 생성한다.
- `Mono.empty()`
  - ![empty](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/empty.svg)
  - item을 emit하지 않고 complete되는 Mono를 생성한다.
- `Mono.error(Throwable error) / Mono.error(Supplier<? extends Throwable> errorSupplier)`
  - ![error(error)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/error.svg)
  - ![error(errorSupplier)](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/errorWithSupplier.svg)
  - 구독 직후 error를 일으키는 Mono를 생성한다.
- `Flux.fromArray(T[] array)`
  - ![fromArray](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromArray.svg)
  - 제공된 배열의 item들을 emit하는 Flux를 생성한다.
- `Flux.fromIterable(Iterable<? extends T> it)`
  - ![fromIterable](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromIterable.svg)
  - Iterable의 items를 emit하는 Flux를 생성한다.
- `Flux.fromStream(Stream<? extends T> s) / Flux.fromStream(Supplier<Stream<? extends T>> streamSupplier)`
  - ![fromStream](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/fromStream.svg)
  - Stream의 items를 emit하는 Flux를 생성한다.
- `Flux.interval(Duration period) / Flux.interval(Duration delay, Duration period) / Flux.interval(Duration delay, Duration period, Scheduler timer) / Flux.interval(Duration period, Scheduler timer)`
  - ![interval](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/docㅈ-files/marbles/interval.svg)
  - ![interval](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/intervalWithDelay.svg)
  - 0에서 시작하여 period interval만큼 증가하면서 long value를 emit하는 Flux를 생성한다. (0, 1, 2, ...)
  - delay가 주어지는 경우 initial delay가 흐른 후 emit을 시작한다.
- `Flux.range(int start, int count)`
  - ![range](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/range.svg)
  - start ~ start + count - 1 까지의 값을 순차적으로 emit하는 Flux를 생성한다.
- `Mono.repeat()`
  - ![repeat](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/repeatForMono.svg)
  - 이전 구독이 완료되면 계속해서 무한하게 source를 구독한다.
- `Flux.generate(Callable<S> stateSupplier, BiFunction<S,SynchronousSink<T>,S> generator) / Flux.generate(Callable<S> stateSupplier, BiFunction<S,SynchronousSink<T>,S> generator, Consumer<? super S> stateConsumer) / Flux.generate(Consumer<SynchronousSink<T>> generator)`
  - ![generate](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/generate.svg)
  - ![generateWithCleanup](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/generateWithCleanup.svg)
  - Low-level 메서드로 Flux sequence의 state와 backpressure를 직접 Handling할 수 있다.
  - stateSupplier는 generator function의 Initial state object를 제공한다.
  - generator function은 currentState와 SynchronousSink API를 input으로 하는 BiFunction으로 sequence의 next element를 생성한다. SynchronousSink API를 통해 element를 emit하거나 complete, error를 발생시킬 수 있다.
  - stateConsumer는 generator function이 호출된 후의 state object를 수정하는 것과 관련된다. cleanup 구현이 주로 이루어진다.
- `Flux.create(Consumer<? super FluxSink<T>> emitter) / Flux.create(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure)`
  - ![create](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/createForFlux.svg)
  - ![createWithOverflowStrategy](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/createWithOverflowStrategy.svg)
  - FluxSink API를 통해서 동기/비동기식으로 여러 elements를 emit하는 기능을 가지는 Flux를 programmatically하게 구현한다.
  - backpressure strategy를 직접 설정할 수 있다.
- `Flux.push(Consumer<? super FluxSink<T>> emitter) / Flux.push(Consumer<? super FluxSink<T>> emitter, FluxSink.OverflowStrategy backpressure)`
  - ![push](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/push.svg)
  - ![pushWithOverflowStrategy](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/doc-files/marbles/pushWithOverflowStrategy.svg)
  - FluxSink API를 통해 동기/비동기식으로 여러 elements를 emit하는 기능을 가지는 Flux를 programmatically하게 구현한다.
  - backpressure strategy를 직접 설정할 수 있다.

  > Flux.create() 와 Flux.push() 두 메서는 모두 FluxSink API를 사용해 동일한 기능을 구현하지만, create 메서드의 경우 멀티스레드에서 item을 생성할 수 있다는 점에서, push 메서드의 경우 그렇지 못하다는 점에서 차이가 있다.