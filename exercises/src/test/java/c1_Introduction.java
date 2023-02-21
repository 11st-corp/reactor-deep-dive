import org.junit.jupiter.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This chapter will introduce you to the basics of Reactor.
 * You will learn how to retrieve result from Mono and Flux
 * in different ways.
 *
 * Read first:
 *
 * https://projectreactor.io/docs/core/release/reference/#intro-reactive
 * https://projectreactor.io/docs/core/release/reference/#reactive.subscribe
 * https://projectreactor.io/docs/core/release/reference/#_subscribe_method_examples
 *
 * Useful documentation:
 *
 * https://projectreactor.io/docs/core/release/reference/#which-operator
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html
 *
 * @author Stefan Dragisic
 */
public class c1_Introduction extends IntroductionBase {

    /**
     * Every journey starts with Hello World!
     * As you may know, Mono represents asynchronous result of 0-1 element.
     * Retrieve result from this Mono by blocking indefinitely or until a next signal is received.
     */
    @Test
    public void hello_world() {
        Mono<String> serviceResult = hello_world_service();

        String result = serviceResult.block(); //todo: change this line only

        assertEquals("Hello World!", result);
    }

    /**
     * Retrieving result should last for a limited time amount of time, or you might get in trouble.
     * Try retrieving result from service by blocking for maximum of 1 second or until a next signal is received.
     */
    @Test
    public void unresponsive_service() {
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            Mono<String> serviceResult = unresponsiveService();

            String result = serviceResult.block(Duration.ofSeconds(1)); //todo: change this line only
        });

        String expectedMessage = "Timeout on blocking read for 1";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    /**
     * Services are unpredictable, they might and might not return a result and no one likes nasty NPE's.
     * Retrieve result from the service as optional object.
     */
    @Test
    public void empty_service() {
        Mono<String> serviceResult = emptyService();

        Optional<String> optionalServiceResult = serviceResult.blockOptional(); //todo: change this line only

        assertTrue(optionalServiceResult.isEmpty());
        assertTrue(emptyServiceIsCalled.get());
    }

    /**
     * Many services return more than one result and best services supports streaming!
     * It's time to introduce Flux, an Asynchronous Sequence of 0-N Items.
     *
     * Service we are calling returns multiple items, but we are interested only in the first one.
     * Retrieve first item from this Flux by blocking indefinitely until a first item is received.
     */
    @Test
    public void multi_result_service() {
        Flux<String> serviceResult = multiResultService();

        String result = serviceResult.blockFirst(); //todo: change this line only

        assertEquals("valid result", result);
    }

    /**
     * We have the service that returns list of fortune top five companies.
     * Collect companies emitted by this service into a list.
     * Retrieve results by blocking.
     */
    @Test
    public void fortune_top_five() {
        Flux<String> serviceResult = fortuneTop5();

        // FIXME : 틀렸던 문제!
        List<String> results = serviceResult.collectList().block(); //todo: change this line only

        assertEquals(Arrays.asList("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group"), results);
        assertTrue(fortuneTop5ServiceIsCalled.get());
    }

    /***
     * "I Used an Operator on my Flux, but it Doesn’t Seem to Apply. What Gives?"
     *
     * Previously we retrieved result by blocking on a Mono/Flux.
     * That really beats whole purpose of non-blocking and asynchronous library like Reactor.
     * Blocking operators are usually used for testing or when there is no way around, and
     * you need to go back to synchronous world.
     *
     * Fix this test without using any blocking operator.
     * Change only marked line!
     */
    @Test
    public void nothing_happens_until_you_() throws InterruptedException {
        CopyOnWriteArrayList<String> companyList = new CopyOnWriteArrayList<>();

        Flux<String> serviceResult = fortuneTop5();

        //FIXME : 어려웠음!!
        /**
         * Rx는 여러 파이프 라인을 조합해서 원하는 동작을 구현하는 개념이고,
         * subscribe는 파이프라인의 마지막 단에서 호출하기 때문에 다양하게 조합된 파이프의 각 부분에 들어가서 검사를 하거나 로그를 찍는 등의 작업이 필요할 때 onNext나 doOnNext를 사용할 수 있다.
         *
         * 기본적으로 do 계열의 함수들은 스트림에 영향을 주지 않고 로그를 쓴다든지 아님 아래처럼 새 리스트에 담는다든지 등의 부수효과를 일으킨다.
         *
         * doOnNext :
         * onNext가 호출된 후, 즉 데이터가 통지되는 시점에 동작하는 연산으로, onNext로 통지된 값을 인자로 받음.
         * 그러므로 통지된 값을 가지고 별도의 부수효과(바깥 리스트에 넣는다든지, 로그를 기록한다든지)를 적용할 수 있어서 유용하다.
         *
         * subscribe:
         * Flux든 뭐든 만들어서 발행을 했다면 얘를 구독해야 마무리가 된다. 그 구독의 역할을 하는거고,
         * subscribe()를 호출해야 비로소 변환한 데이터를 구독자에게 발행한다.
         * just 등으로 발행만 하면 데이터를 발행하지 않는다. 반드시 데이터를 수신할 구독자가 subscribe 함수를 호출해야 데이터가 발행된다.
         * 구독을 해야 데이터가 발행되니까 subscribe는 데이터를 발행하는 역할이기도 하다.
         *
         * ????
         * 여기서 그럼 누가 구독자야?
         * doOnNext 까지가 Flux이고, 거기에 대해 subscribe를 하는건데.... 여기서 발행자는 serviceResult아닌가?
         *
         * -- 정답
         * Flux는 생산자(발행자)
         * Subscriber는 없는 거 아니야? 누군지 모르지만 일단 구독은 한다.?
         * */
        serviceResult
                .doOnNext(companyList::add)
                .subscribe();
        //todo: add an operator here, don't use any blocking operator!
        ;

        Thread.sleep(1000); //bonus: can you explain why this line is needed?

        assertEquals(Arrays.asList("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group"), companyList);
    }

    /***
     * If you finished previous task, this one should be a breeze.
     *
     * Upgrade previously used solution, so that it:
     *  - adds each emitted item to `companyList`
     *  - does nothing if error occurs
     *  - sets `serviceCallCompleted` to `true` once service call is completed.
     *
     *  Don't use doOnNext, doOnError, doOnComplete hooks.
     */
    @Test
    public void leaving_blocking_world_behind() throws InterruptedException {
        AtomicReference<Boolean> serviceCallCompleted = new AtomicReference<>(false);
        CopyOnWriteArrayList<String> companyList = new CopyOnWriteArrayList<>();

        //FIXME : 기존 내 답은 doOnNext를 써버림. 근데 문제에서 doOnNext 쓰지 말랬다. subscribe에 옵션을 줄 수 있었음!
        //파라미터 순서가 정상consumer/에러consumer/다 마친 consumer임.
        fortuneTop5()
                .subscribe(companyList::add, null, ()-> serviceCallCompleted.set(true));
        //todo: change this line only
        ;

        Thread.sleep(1000);

        assertTrue(serviceCallCompleted.get());
        assertEquals(Arrays.asList("Walmart", "Amazon", "Apple", "CVS Health", "UnitedHealth Group"), companyList);
    }
}
