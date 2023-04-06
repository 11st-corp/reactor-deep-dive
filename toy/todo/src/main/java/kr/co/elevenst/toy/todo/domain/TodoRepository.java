package kr.co.elevenst.toy.todo.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface TodoRepository extends R2dbcRepository<Todo, Long> {}
