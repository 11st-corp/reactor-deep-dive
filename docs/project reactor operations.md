# ⚛️ Project Reactor의 Operations

<br>

## Mono.flatMap()
![스크린샷 2023-03-22 오후 4 33 06](https://user-images.githubusercontent.com/60773356/226831744-dd6b996d-5504-4ecb-adc6-703e7d45dcb7.png)

`Publisher` 객체의 타입 변환이 주목적으로써, 변환된 이후 비동기로 실행된다.

따라서, `forEach`와 같은 동기 실행보다 속도가 빠름.

여러 개의 `Mono` 인스턴스를 연결해 복잡한 비동기 처리를 수행할 수 있음

```java
Mono.just("Hello")
    .flatMap(str -> Mono.just(str + " World"))
    .subscribe(System.out::println); // Hello Wolrd

```

<br>

## Flux.flatMap()
![스크린샷 2023-03-22 오후 5 55 16](https://user-images.githubusercontent.com/60773356/226850768-5c97ef00-d023-4364-9061-8165dbd91de2.png)

`Publisher` 객체의 타입 변환이 주목적으로써, 변환된 이후 비동기로 실행된다.

따라서, `forEach`와 같은 동기 실행보다 속도가 빠름.

여러 개의 `Flux` 인스턴스를 연결해 복잡한 비동기 처리를 수행할 수 있음

```java
Flux.just(1, 2, 3)
    .flatMap(i -> Flux.range(1, i))
    .subscribe(System.out::println);

/* 출력
 * 1
 * 1
 * 2
 * 1
 * 2
 * 3
 * */

```

<br>

## Mono.flatMapMany()
![스크린샷 2023-03-22 오후 5 58 07](https://user-images.githubusercontent.com/60773356/226851565-e33f621a-3818-4c8f-a8a9-c9a3e707f544.png)

`Mono`가 생성한 결과를 기반으로 추가 작업을 할 때, 해당 작업에서 `Flux`를 반환한다면 `flatMapMany()를 사용하여 처리할 수 있음.

```java
Mono.just(Arrays.asList("apple", "banana", "orange"))
    .flatMapMany(Flux::fromIterable)
    .subscribe(System.out::println);

/* 출력
    apple
    banana
    orange
 */
```

<br>

## Flux.concatWith()
![스크린샷 2023-03-22 오후 6 01 16](https://user-images.githubusercontent.com/60773356/226852334-79238e7d-3247-446b-9417-926d3fc02c21.png)

`concatWith()`은 현재 `Flux`와 다른 `Publisher` 인스턴스를 연결해 새로운 `Flux`를 생성한다.

여러 개의 `Flux`하나로 결합하는 경우 유용하게 사용할 수 있다.

여러개의 인스턴스 결과가 순차적으로 연결되기 때문에 결과의 순서가 보장된다.

```java
Flux<Integer> flux1 = Flux.just(1, 2, 3);
Flux<Integer> flux2 = Flux.just(4, 5, 6);
Flux<Integer> resultFlux = flux1.concatWith(flux2);
resultFlux.subscribe(System.out::println);

/* 출력
   1
   2
   3
   4
   5
   6 
 */
```

<br>

## Flux.concatMap()
![스크린샷 2023-03-22 오후 6 12 06](https://user-images.githubusercontent.com/60773356/226855096-a25299ea-21e7-43d4-853c-fa9b8047f780.png)

여러 `Flux`를 연결해 하나의 `Flux`를 생성한다는 점은 `concatWith()`와 같지만 `concatMap()`은 각 요소를 처리하는 `Publisher`의 결과를 순서대로 결합하는데,

이 `Publisher`가 비동기로 처리되기 때문에 결과의 순서가 보장되지 않을 수 있다.

<br>

## Flux.firstWithSignal()
![스크린샷 2023-03-22 오후 6 26 46](https://user-images.githubusercontent.com/60773356/226858833-0e81e840-f501-4b09-a6ad-2cdb68053a94.png)

`firstWithSignal()`은 여러 `Flux`중에서 첫번째로 도착한 결과를 반환한다.

`Flux`가 결과를 생성할 때까지 블로킹하지 않고, 각 `Flux`의 결과를 비동기로 처리한다.

만약, 여러 `Flux`중 하나에서라도 에러가 발생하면 바로 에러를 전파한다.(결과 반환 X)

```java
Flux<Integer> flux1 = Flux.just(1, 2, 3)
    .delayElements(Duration.ofMillis(500));
Flux<Integer> flux2 = Flux.just(4, 5, 6)
    .delayElements(Duration.ofMillis(200));
Flux<Integer> flux3 = Flux.just(7, 8, 9)
    .delayElements(Duration.ofMillis(1000));
Flux<Integer> result = Flux.firstWithSignal(flux1, flux2, flux3);
result.subscribe(System.out::println);

/* 출력
    4
    5
    6
 */
```

가장 먼저 도착하는 `flux2`가 출력된다.

<br>

## Flux.switchIfEmpty()
![스크린샷 2023-03-22 오후 6 30 49](https://user-images.githubusercontent.com/60773356/226860187-beaac6c0-5e97-4327-94b3-b8da447144a4.png)

현재 `Flux`가 비어있을 때, 다른 `Flux`로 대체하는 메서드이다.

```java
Flux<Integer> flux1 = Flux.empty();
Flux<Integer> flux2 = Flux.just(1, 2, 3);
Flux<Integer> result = flux1.switchIfEmpty(flux2);
result.subscribe(System.out::println);

/* 출력
    1
    2
    3
 */
```

<br>

## Flux.switchOnFirst()
![스크린샷 2023-03-23 오후 2 44 43](https://user-images.githubusercontent.com/60773356/227114430-5ce8cc05-c299-410c-a456-49052ff57328.png)

첫번째 이벤트를 발생시킨 `Flux`를 다른 `Flux`로 대체하는 메서드이다.
- `Next`
- `Complete`
- `Error`

```java
Flux<Integer> flux1 = Flux.just(1, 2, 3);
Flux<Integer> flux2 = Flux.just(4, 5, 6);
Flux<Integer> result = flux1.switchOnFirst((signal, flux) -> signal.isOnNext() ? flux2 : flux);
result.subscribe(System.out::println);

/* 출력
    4
    5
    6
 */
```

첫번째로 발생한 이벤트가 `onNext`인 경우에는 `flux2`를 반환하고 `onError`, `onComplete`인 경우에는 `flux1`을 반환하는 코드이다.

이런식으로 예외 처리나 특정 상황에서 대체 처리 작업을 수행할 수 있다.

<br>

## Flux.switchMap()
![스크린샷 2023-03-23 오후 2 53 05](https://user-images.githubusercontent.com/60773356/227115728-b598824c-0dca-45b7-9d52-936d0b2b3b68.png)

`Flux`에 대해 다른 `Flux`나 `Mono`를 반환하는 함수를 적용해 새로운 `Pulisher`를 생성한다.

```java
Flux.just(1, 2, 3)
    .switchMap(i -> Flux.range(1, i))
    .subscribe(System.out::println);

/* 출력
    1
    1
    2
    1
    2
    3
 */
```

<br>

## Mono.when()
![스크린샷 2023-03-23 오후 3 35 49](https://user-images.githubusercontent.com/60773356/227122841-57f3f3f2-0341-45d1-8f12-a4462ab54c1a.png)

여러 `Mono`를 동시에 실행하고 그 결과를 모아 하나의 `Mono`로 반환한다.

모든 `Mono`가 성공적으로 완료되야하고 하나라도 실패하면 반환된 `Mono`도 즉시 실패한다.

`zip`과의 차이점은 조합의 목적이 아닌 모든 `Mono`가 성공적으로 완료되었는지에 목적이 있다.(실행 순서는 보장되지 않음)

```java
Mono<Integer> mono1 = Mono.just(1);
Mono<Integer> mono2 = Mono.just(2);
Mono<Integer> mono3 = Mono.just(3);

Mono<Void> whenMono = Mono.when(mono1, mono2, mono3);

```

<br>

## Mono.and()
![스크린샷 2023-03-23 오후 3 40 34](https://user-images.githubusercontent.com/60773356/227123620-c662dacb-53fd-4458-bcc8-8fb59e18e0bb.png)

주어진 `Mono`와 현재 `Mono`의 종료 신호를 결합해 새로운 `Mono<Void>`를 반환한다.

두 `Mono`가 모두 성공적으로 종료되어야 완료된다. 하나라도 실패한다면 반환된 `Mono`도 즉시 실패한다.

```java
Mono<String> mono1 = Mono.just("Hello");
Mono<String> mono2 = Mono.just("World");

Mono<Void> voidMono = mono1.and(mono2);

```

<br>

## Mono.then()
![스크린샷 2023-03-23 오후 3 47 51](https://user-images.githubusercontent.com/60773356/227124940-0bc30d98-8f32-4122-8587-4ea066d1d0f0.png)

현재 `Mono`의 완료 및 오류 신호만 재생산하는 `Mono`를 반환한다.

`Mono`의 출력을 무시하고 단순히 완료 또는 오류 신호만 전달한다.

```java
Mono<String> mono = Mono.just("Hello, World!");

Mono<Void> voidMono = mono.then();
```

<br>


## Mono.thenMany()
![스크린샷 2023-03-23 오후 3 49 15](https://user-images.githubusercontent.com/60773356/227125199-cb51f145-8c40-41ff-b018-555ef5a346a3.png)

현재 `Mono`가 성공적으로 완료되면 다른 `Publisher`를 실행하는 `Flux`를 반환한다.

`Mono`에서 발생한 오류는 `Publisher`에까지 전달된다.

```java
Mono<String> mono = Mono.just("Hello, World!");
Flux<Integer> flux = Flux.just(1, 2, 3);

Flux<Integer> result = mono.thenMany(flux);
```

<br>

## Flux.mergeWith()
![스크린샷 2023-03-23 오후 3 51 15](https://user-images.githubusercontent.com/60773356/227125577-dd1c4400-2f67-4cfb-93d8-089c3bd37d72.png)

현재 `Flux`와 다른 `Publisher`를 병합해 교차 통합된 시퀀스를 생성한다.(즉시 구독)

```java
Flux<String> flux = Flux.just("A", "B", "C");
Mono<String> mono = Mono.just("D");

Flux<String> result = flux.mergeWith(mono); // "A", "D", "B", "C"
```

<br>

## Flux.zipWith()
![스크린샷 2023-03-23 오후 3 54 18](https://user-images.githubusercontent.com/60773356/227126200-0d9efdab-2173-404d-90bd-5317bd45e216.png)

현재 `Flux`와 다른 `Publisher`를 결합해 각각 하나의 요소를 방출해 `Tuple`들을 생성한다.

2개 중에서 하나라도 완료된다면 작동은 중지되고 에러가 발생하면 즉시 에러가 전달된다.

```java
Flux<Integer> flux1 = Flux.just(1, 2, 3);
Flux<Integer> flux2 = Flux.just(4, 5, 6);

Flux<Tuple2<Integer, Integer>> result = flux1.zipWith(flux2); // (1, 4), (2, 5), (3, 6)
```

<br>

## Mono.defer()
![스크린샷 2023-03-23 오후 3 56 39](https://user-images.githubusercontent.com/60773356/227126634-3ccd4abf-ffda-49f7-9dc7-45bb6dcc9096.png)

구독을 시작한 시점에 `Mono`생성하고 구독한다.

이를 활용하면 구독 시점에 생성해야하는 `Mono`를 최소한의 리소스로 생성이 가능하다.

<br>

## Flux.usingWhen()
![스크린샷 2023-03-23 오후 3 58 21](https://user-images.githubusercontent.com/60773356/227126983-e2cc92a2-1a27-439c-8998-f363f0b9ac59.png)

`Subscriber`마다 생성된 리소스를 사용해 `Publisher`에서 발생한 값을 스트리밍한다.

시퀀스가 종료되면 함수를 사용해 `CleanUp Publisher`를 생성한다.

이는 시퀀스의 내용을 변경하지 않고 종료를 연기한다.

리소스를 제공하는 `Publisher`가 여러개의 리소르를 발행하면 다음 리소스는 무시된다.

또한, 최소 1개의 `onNext` 이벤트를 발생시킨 뒤에 에러를 발생시키면 에러도 무시된다.

비어있는 `Completion`, `onNext` 신호가 하나라도 발행되지 않은 에러가 발생하면 즉시 중단된다.(`cleanUp` 호출 x)

리소스를 안정하게 관리하는데에 주로 사용된다.

```java
Flux<String> flux = Flux.usingWhen(
        Mono.fromCallable(() -> Files.newBufferedReader(Paths.get("filename.txt"))),
        reader -> Flux.fromIterable(() -> reader.lines().iterator()),
        BufferedReader::close
);

flux.subscribe(System.out::println);
```

