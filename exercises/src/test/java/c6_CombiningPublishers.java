import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In this important chapter we are going to cover different ways of combining publishers.
 * <p>
 * Read first:
 * <p>
 * https://projectreactor.io/docs/core/release/reference/#which.values
 * <p>
 * Useful documentation:
 * <p>
 * https://projectreactor.io/docs/core/release/reference/#which-operator
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html
 * https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html
 *
 * @author Stefan Dragisic
 */
public class c6_CombiningPublishers extends CombiningPublishersBase {

    /**
     * Goal of this exercise is to retrieve e-mail of currently logged-in user.
     * `getCurrentUser()` method retrieves currently logged-in user
     * and `getUserEmail()` will return e-mail for given user.
     * <p>
     * No blocking operators, no subscribe operator!
     * You may only use `flatMap()` operator.
     */
    @Test
    public void behold_flatmap() {
        Hooks.enableContextLossTracking(); //used for testing - detects if you are cheating!

        Mono<String> currentUserMono = getCurrentUser();
        Mono<String> currentUserEmail = currentUserMono.flatMap(this::getUserEmail);

        //don't change below this line
        StepVerifier.create(currentUserEmail)
                .expectNext("user123@gmail.com")
                .verifyComplete();
    }

    /**
     * `taskExecutor()` returns tasks that should execute important work.
     * Get all the tasks and execute them.
     * <p>
     * Answer:
     * - Is there a difference between Mono.flatMap() and Flux.flatMap()?
     */
    @Test
    public void task_executor() {
        Flux<Void> tasks = taskExecutor().flatMap(task -> task);

        //don't change below this line
        StepVerifier.create(tasks)
                .verifyComplete();

        Assertions.assertEquals(taskCounter.get(), 10);
    }

    /**
     * `streamingService()` opens a connection to the data provider.
     * Once connection is established you will be able to collect messages from stream.
     * <p>
     * Establish connection and get all messages from data provider stream!
     * 람다식 그냥 argument고대로 나오는 것은 Function.identity로 할 수 있어 보이는듯?
     */
    @Test
    public void streaming_service() {
        Flux<Message> messageFlux = streamingService().flatMapMany(messages -> messages);

        //don't change below this line
        StepVerifier.create(messageFlux)
                .expectNextCount(10)
                .verifyComplete();
    }


    /**
     * Join results from services `numberService1()` and `numberService2()` end-to-end.
     * First `numberService1` emits elements and then `numberService2`. (no interleaving)
     * <p>
     * Bonus: There are two ways to do this, check out both! - 하나는 뭘까? 그냥 mergeWith?
     * Q? merge로 했는데 왜 통과하누?
     */
    @Test
    public void i_am_rubber_you_are_glue() {
        Flux<Integer> numbers = Flux.concat(numberService1(), numberService2());

        //don't change below this line
        StepVerifier.create(numbers)
                .expectNext(1, 2, 3, 4, 5, 6, 7)
                .verifyComplete();
    }

    /**
     * Similar to previous task:
     * <p>
     * `taskExecutor()` returns tasks that should execute important work.
     * Get all the tasks and execute each of them.
     * <p>
     * Instead of flatMap() use concatMap() operator.
     * <p>
     * Answer:
     * - What is difference between concatMap() and flatMap()? 설명 다시 볼 것 ㅋ
     * - What is difference between concatMap() and flatMapSequential()?
     * - Why doesn't Mono have concatMap() operator?
     */
    @Test
    public void task_executor_again() {
        Flux<Void> tasks = taskExecutor().concatMap(task -> task);

        //don't change below this line
        StepVerifier.create(tasks)
                .verifyComplete();

        Assertions.assertEquals(taskCounter.get(), 10);
    }

    /**
     * You are writing software for broker house. You can retrieve current stock prices by calling either
     * `getStocksGrpc()` or `getStocksRest()`.
     * Since goal is the best response time, invoke both services but use result only from the one that responds first.
     * 질문 : firstWithSignal vs firstWithValue
     */
    @Test
    public void need_for_speed() {
        Flux<String> stonks = Flux.firstWithValue(
                getStocksGrpc(),
                getStocksRest());

        //don't change below this line
        StepVerifier.create(stonks)
                .expectNextCount(5)
                .verifyComplete();
    }

    /**
     * As part of your job as software engineer for broker house, you have also introduced quick local cache to retrieve
     * stocks from. But cache may not be formed yet or is empty. If cache is empty, switch to a live source:
     * `getStocksRest()`.
     */
    @Test
    public void plan_b() {
        Flux<String> stonks =
                getStocksLocalCache().switchIfEmpty(
                        getStocksRest());

        //don't change below this line
        StepVerifier.create(stonks)
                .expectNextCount(6)
                .verifyComplete();

        Assertions.assertTrue(localCacheCalled.get());
    }

    /**
     * You are checking mail in your mailboxes. Check first mailbox, and if first message contains spam immediately
     * switch to a second mailbox. Otherwise, read all messages from first mailbox.
     * 약간 정리할 필요는 있어보임요
     */
    @Test
    public void mail_box_switcher() {
        //todo: feel free to change code as you need
        Flux<Message> myMail = mailBoxPrimary()
                .switchOnFirst((signal, messageFlux) -> {
                    if (signal.hasValue()) {
                        Message firstMessage = signal.get();
                        if (firstMessage.metaData.equals("spam"))
                            return mailBoxSecondary();
                    }
                    return messageFlux;
                });

        //don't change below this line
        StepVerifier.create(myMail)
                .expectNextMatches(m -> !m.metaData.equals("spam"))
                .expectNextMatches(m -> !m.metaData.equals("spam"))
                .verifyComplete();

        Assertions.assertEquals(1, consumedSpamCounter.get());
    }

    /**
     * You are implementing instant search for software company.
     * When user types in a text box results should appear in near real-time with each keystroke.
     * <p>
     * Call `autoComplete()` function for each user input
     * but if newer input arrives, cancel previous `autoComplete()` call and call it for latest input.
     * 흑흑 한참 찾음...근데 마지막 publisher가 Mono아닌가? ㅠ
     */
    @Test
    public void instant_search() {
        Flux<String> suggestions = userSearchInput().switchMap(this::autoComplete);
        //don't change below this line
        StepVerifier.create(suggestions)
                .expectNext("reactor project", "reactive project")
                .verifyComplete();
    }


    /**
     * Code should work, but it should also be easy to read and understand.
     * Orchestrate file writing operations in a self-explanatory way using operators like `when`,`and`,`then`...
     * If all operations have been executed successfully return boolean value `true`.
     * 근데 순서가 이게 맞낭!
     */
    @Test
    public void prettify() {
        Mono<Boolean> successful = Mono.when(openFile())
                .then(writeToFile("0x3522285912341"))
                .then(closeFile())
                .thenReturn(true);


        //don't change below this line
        StepVerifier.create(successful)
                .expectNext(true)
                .verifyComplete();

        Assertions.assertTrue(fileOpened.get());
        Assertions.assertTrue(writtenToFile.get());
        Assertions.assertTrue(fileClosed.get());
    }

    /**
     * Before reading from a file we need to open file first.
     * 맞나!?...
     */
    @Test
    public void one_to_n() {
        Flux<String> fileLines = openFile().ignoreElement().thenMany(readFile());

        StepVerifier.create(fileLines)
                .expectNext("0x1", "0x2", "0x3")
                .verifyComplete();
    }

    /**
     * Execute all tasks sequentially and after each task have been executed, commit task changes. Don't lose id's of
     * committed tasks, they are needed to further processing!
     * concatMap을 사용하여 task 그룹핑을 진행한다. concatMap 내부에서는 task마다 commitTask 작업을 진행한다.
     * Q: 한번 맞춰볼 필요는 있어보이는 노낌스~ task처리할 때 map vs flatMap?
     */
    @Test
    public void acid_durability() {
        //todo: feel free to change code as you need
        Flux<String> committedTasksIds = tasksToExecute().concatMap(task -> task.map(id -> {
            commitTask(id);
            return id;
        }));
        //don't change below this line
        StepVerifier.create(committedTasksIds)
                .expectNext("task#1", "task#2", "task#3")
                .verifyComplete();

        Assertions.assertEquals(3, committedTasksCounter.get());
    }


    /**
     * News have come that Microsoft is buying Blizzard and there will be a major merger.
     * Merge two companies, so they may still produce titles in individual pace but as a single company.
     * Q : merge vs mergeWith
     */
    @Test
    public void major_merger() {
        //todo: feel free to change code as you need
        Flux<String> microsoftBlizzardCorp = Flux.merge(microsoftTitles(), blizzardTitles());

        //don't change below this line
        StepVerifier.create(microsoftBlizzardCorp)
                .expectNext("windows12",
                        "wow2",
                        "bing2",
                        "overwatch3",
                        "office366",
                        "warcraft4")
                .verifyComplete();
    }


    /**
     * Your job is to produce cars. To produce car you need chassis and engine that are produced by a different
     * manufacturer. You need both parts before you can produce a car.
     * Also, engine factory is located further away and engines are more complicated to produce, therefore it will be
     * slower part producer.
     * After both parts arrive connect them to a car.
     * 마블다이어그램!
     */
    @Test
    public void car_factory() {
        //todo: feel free to change code as you need
        Flux<Car> producedCars = Flux.zip(carChassisProducer(), carEngineProducer(), Car::new);

        //don't change below this line
        StepVerifier.create(producedCars)
                .recordWith(ConcurrentLinkedDeque::new)
                .expectNextCount(3)
                .expectRecordedMatches(cars -> cars.stream()
                        .allMatch(car -> Objects.equals(car.chassis.getSeqNum(),
                                car.engine.getSeqNum())))
                .verifyComplete();
    }

    /**
     * When `chooseSource()` method is used, based on current value of sourceRef, decide which source should be used.
     * Q : deferCopntextual vs defer
     */

    //only read from sourceRef
    AtomicReference<String> sourceRef = new AtomicReference<>("X");

    //todo: implement this method based on instructions
    public Mono<String> chooseSource() {
        return Mono.deferContextual(ctx -> {
            final String value = sourceRef.getAcquire();
            if ("A".equals(value)) return Mono.defer(this::sourceA);
            else if ("B".equals(value)) return Mono.defer(this::sourceB);
            return Mono.empty();
        });
    }

    @Test
    public void deterministic() {
        //don't change below this line
        Mono<String> source = chooseSource();

        sourceRef.set("A");
        StepVerifier.create(source)
                .expectNext("A")
                .verifyComplete();

        sourceRef.set("B");
        StepVerifier.create(source)
                .expectNext("B")
                .verifyComplete();
    }

    /**
     * Sometimes you need to clean up after your self.
     * Open a connection to a streaming service and after all elements have been consumed,
     * close connection (invoke closeConnection()), without blocking.
     * <p>
     * This may look easy...
     * 마블다이어그램 확인 + do : 람다 공부 ㅋ
     */
    @Test
    public void cleanup() {
        BlockHound.install(); //don't change this line, blocking = cheating!

        //todo: feel free to change code as you need
        Flux<String> stream = Flux.usingWhen(StreamingConnection.startStreaming(),
                element -> element,
                element -> StreamingConnection.closeConnection());

        //don't change below this line
        StepVerifier.create(stream)
                .then(() -> Assertions.assertTrue(StreamingConnection.isOpen.get()))
                .expectNextCount(20)
                .verifyComplete();
        Assertions.assertTrue(StreamingConnection.cleanedUp.get());
    }
}
