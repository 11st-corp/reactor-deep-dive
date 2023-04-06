package kr.co.elevenst.toy.todo.controller;

import java.time.LocalDateTime;
import java.util.stream.LongStream;

import kr.co.elevenst.toy.todo.configuration.DisabledSecurityConfiguration;
import kr.co.elevenst.toy.todo.domain.Todo;
import kr.co.elevenst.toy.todo.domain.TodoDTO;
import kr.co.elevenst.toy.todo.domain.TodoRepository;
import kr.co.elevenst.toy.todo.service.TodoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@WebFluxTest(TodoController.class)
@Import(DisabledSecurityConfiguration.class)
class TodoControllerTests {

	private static final LocalDateTime NOW = LocalDateTime.now();

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private TodoRepository todoRepository;

	@MockBean
	private TodoService todoService;

	@Test
	void createTodoTest() {
		final var content = "스터디";
		final var newTodo = Todo.builder()
				.id(1L)
				.content(content)
				.createdDate(NOW)
				.modifiedDate(NOW)
				.build();

		given(todoRepository.save(any(Todo.class))).willReturn(Mono.just(newTodo));
		given(todoService.createTodo(any(TodoDTO.class))).willReturn(Mono.just(newTodo));

		webTestClient
				.method(HttpMethod.POST)
				.uri("/todos")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(new TodoDTO(content))
				.exchange()
				.expectStatus().isCreated()
				.expectBody().jsonPath("$.content").isEqualTo(content);
	}

	@Test
	void readTodosTest() {
		final var expectedTodos = 10L;
		final var todoList = LongStream.range(1L, expectedTodos + 1L) // 10 Todos
				.mapToObj(this::fixtureTodo)
				.collect(toList());
		final var expected = Flux.fromIterable(todoList);

		given(todoRepository.findAll()).willReturn(expected);
		given(todoService.getTodos()).willReturn(expected);

		webTestClient
				.method(HttpMethod.GET)
				.uri("/todos")
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(Todo.class);
	}

	@Test
	void readTodoTest() {
		final var todoId = 1L;
		final var expectedTodo = newTodo(todoId, "스터디");
		final var expected = Mono.just(expectedTodo);

		given(todoRepository.findById(anyLong())).willReturn(expected);
		given(todoService.getTodo(anyLong())).willReturn(expected);

		webTestClient
				.method(HttpMethod.GET)
				.uri(uriBuilder -> uriBuilder.path("/todos/{id}").build(todoId))
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.id").isEqualTo(todoId)
				.jsonPath("$.content").isEqualTo(expectedTodo.getContent());
	}

	@Test
	void updateTodoTest() {
		final var todoId = 1L;
		final var updatedTodoDTO = new TodoDTO("스터디");
		final var expectedTodo = newTodo(todoId, updatedTodoDTO.content());
		final var expected = Mono.just(expectedTodo);

		given(todoRepository.save(any(Todo.class))).willReturn(expected);
		given(todoService.updateTodo(anyLong(), any(TodoDTO.class))).willReturn(expected);

		webTestClient
				.method(HttpMethod.PUT)
				.uri(uriBuilder -> uriBuilder.path("/todos/{id}").build(todoId))
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(updatedTodoDTO)
				.exchange()
				.expectStatus().isOk()
				.expectBody()
				.jsonPath("$.content").isEqualTo(updatedTodoDTO.content());
	}

	@Test
	void removeTodoTest() {
		final var todoId = 1L;
		final var expectedTodo = newTodo(todoId, "스터디");
		final var expected = Mono.just(expectedTodo);

		given(todoRepository.findById(anyLong())).willReturn(expected);
		given(todoService.removeTodo(anyLong())).willReturn(Mono.empty());

		webTestClient
				.method(HttpMethod.DELETE)
				.uri(uriBuilder -> uriBuilder.path("/todos/{id}").build(todoId))
				.exchange()
				.expectStatus().isNoContent()
				.expectBody().isEmpty();
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