package ru.project.quiz.domain.entity;

import lombok.*;
import ru.project.quiz.domain.enums.CategoryType;
import ru.project.quiz.domain.enums.DifficultyType;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    long id;

    @Column(name = "name")
    String name;

    @Column(name = "description")
    String description;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "difficultyType")
    @Enumerated(EnumType.STRING)
    DifficultyType difficultyType;

    @Column(name = "categoryType")
    CategoryType categoryType;
}
