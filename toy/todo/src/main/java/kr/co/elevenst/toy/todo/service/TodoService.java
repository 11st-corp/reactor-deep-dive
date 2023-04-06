package kr.co.elevenst.toy.todo.service;

import kr.co.elevenst.toy.todo.domain.Todo;
import kr.co.elevenst.toy.todo.domain.TodoDTO;
import kr.co.elevenst.toy.todo.domain.TodoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoService {

	private final TodoRepository todoRepository;

	public Mono<Todo> createTodo(final TodoDTO todoDTO) {
		return todoRepository.save(Todo.builder()
				.content(todoDTO.content())
				.build()
		);
	}

	public Flux<Todo> getTodos() {
		return todoRepository.findAll();
	}

	public Mono<Todo> getTodo(final long todoId) {
		return todoRepository.findById(todoId);
	}

	public Mono<Todo> updateTodo(final long todoId, final TodoDTO todoDTO) {
		return todoRepository.findById(todoId)
				.flatMap(todo -> {
					final var newTodo = Todo.builder()
							.id(todo.getId())
							.content(todoDTO.content())
							.createdDate(todo.getCreatedDate())
							.build();

					return todoRepository.save(newTodo);
				});
	}

	public Mono<Void> removeTodo(final long todoId) {
		return todoRepository.findById(todoId)
				.flatMap(todoRepository::delete)
				.then();
	}
}
