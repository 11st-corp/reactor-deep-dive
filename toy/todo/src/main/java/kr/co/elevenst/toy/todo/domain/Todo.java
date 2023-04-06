package kr.co.elevenst.toy.todo.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table(value = "todo")
@Getter
@Builder
public class Todo {

	@Id
	private Long id;

	private String content;

	@CreatedDate
	private LocalDateTime createdDate;

	@LastModifiedDate
	private LocalDateTime modifiedDate;
}
