package kr.co.elevenst.toy.todo.service;

import java.time.LocalDateTime;
import java.util.stream.LongStream;

import kr.co.elevenst.toy.todo.domain.Todo;
import kr.co.elevenst.toy.todo.domain.TodoDTO;
import kr.co.elevenst.toy.todo.domain.TodoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTests {

	@Mock
	private TodoRepository todoRepository;

	@InjectMocks
	private TodoService todoService;

	@DisplayName("[TODO] Create Test")
	@Test
	void givenTodoDTO_whenCreateTodo_thenOk() {
		final var todoDTO = new TodoDTO("테스트");
		final var expected = newTodo(1L, "스터디");

		given(todoRepository.save(any(Todo.class))).willReturn(Mono.just(expected));

		todoService.createTodo(todoDTO)
				.as(StepVerifier::create)
				.expectNext(expected)
				.verifyComplete();
	}

	@DisplayName("[TODO] Read (All) Test")
	@Test
	void givenNothing_whenReadTodos_thenReturnsTodoList() {
		final var expectedTodos = 10L;
		final var todoList = LongStream.range(1L, expectedTodos + 1L) // 10 Todos
				.mapToObj(this::fixtureTodo)
				.collect(toList());

		given(todoRepository.findAll()).willReturn(Flux.fromIterable(todoList));

		todoService.getTodos()
				.as(StepVerifier::create)
				.expectNextCount(expectedTodos)
				.verifyComplete();
	}

	@DisplayName("[TODO] Read (Single) Test")
	@Test
	void givenTodoId_whenReadTodo_thenReturnsTodoMatchingId() {
		final var arbitraryTodoId = 1L;
		final var expectedTodo = newTodo(arbitraryTodoId, "스터디");

		given(todoRepository.findById(anyLong())).willReturn(Mono.just(expectedTodo));

		todoService.getTodo(arbitraryTodoId)
				.as(StepVerifier::create)
				.expectNext(expectedTodo)
				.verifyComplete();
	}

	@DisplayName("[TODO] Update Test")
	@Test
	void givenTodoContent_whenUpdateTodo_thenReturnsUpdatedTodo() {
		final var targetTodoId = 1L;
		final var updatedContent = "업데이트된 내용";
		final var preUpdatedTodo = newTodo(targetTodoId, "업데이트되기 전 내용");
		final var postUpdatedTodo = newTodo(targetTodoId, updatedContent);

		given(todoRepository.findById(anyLong())).willReturn(Mono.just(preUpdatedTodo));
		given(todoRepository.save(any(Todo.class))).willReturn(Mono.just(postUpdatedTodo));

		todoService.updateTodo(targetTodoId, new TodoDTO(updatedContent))
				.as(StepVerifier::create)
				.expectNext(postUpdatedTodo)
				.verifyComplete();
	}

	@DisplayName("[TODO] Delete Test")
	@Test
	void givenTodoId_whenRemoveTodo_thenRemovedAndReturnsVoid() {
		final var expectedTodo = newTodo(1L, "스터디");

		given(todoRepository.findById(anyLong())).willReturn(Mono.just(expectedTodo));
		given(todoRepository.delete(any(Todo.class))).willReturn(Mono.empty());

		todoService.removeTodo(1L)
				.as(StepVerifier::create)
				.expectComplete()
				.verify();

		verify(todoRepository, times(1)).findById(anyLong());
		verify(todoRepository, times(1)).delete(any(Todo.class));
	}

	private Todo fixtureTodo(final long id) {
		return newTodo(id, "스터디");
	}

	private Todo newTodo(final long id, final String content) {
		final var now = LocalDateTime.now();
		return Todo.builder()
				.id(id)
				.content(content)
				.createdDate(now)
				.modifiedDate(now)
				.build();
	}
}