package kr.co.elevenst.toy.todo.controller;

import kr.co.elevenst.toy.todo.domain.Todo;
import kr.co.elevenst.toy.todo.domain.TodoDTO;
import kr.co.elevenst.toy.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

	private final TodoService todoService;

//	Create
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Todo> createTodo(@RequestBody final TodoDTO todoDTO) {
		return todoService.createTodo(todoDTO);
	}

//	Read
	@GetMapping
	public Flux<Todo> getTodos() {
		return todoService.getTodos();
	}

//	Read
	@GetMapping("/{todoId}")
	public Mono<Todo> getTodo(@PathVariable final long todoId) {
		return todoService.getTodo(todoId);
	}

//	Update
	@PutMapping("/{todoId}")
	public Mono<Todo> updateTodo(@PathVariable final long todoId, @RequestBody final TodoDTO todoDTO) {
		return todoService.updateTodo(todoId, todoDTO);
	}

//	Delete
	@DeleteMapping("/{todoId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> removeTodo(@PathVariable final long todoId) {
		return todoService.removeTodo(todoId);
	}
}
