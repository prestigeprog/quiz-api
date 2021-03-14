package ru.project.quiz.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.When;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;
import ru.project.quiz.dto.QuestionDTO;
import ru.project.quiz.entity.Question;
import ru.project.quiz.enums.Category;
import ru.project.quiz.enums.Difficulty;
import ru.project.quiz.handler.exception.QuestionNotFoundException;
import ru.project.quiz.handler.response.Response;
import ru.project.quiz.repository.QuestionRepository;
import ru.project.quiz.service.QuestionService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
class QuestionControllerTest {

    private final ObjectMapper om = new ObjectMapper();
    @MockBean
    private QuestionRepository questionRepository;
    @Autowired
    private MockMvc mockMvc;

    private Question question;

    @BeforeEach
    public void setUp(){
        question = Question.builder()
                .id(1)
                .category(Category.CORE)
                .difficulty(Difficulty.EASY)
                .description("description")
                .imageUrl("http://site.com")
                .name("First Question")
                .build();
    }

    @Test
    void getRandomQuestion() throws Exception {
        Mockito.when(questionRepository.getRandomQuestion())
                .thenReturn(Optional.of(question));
        MvcResult request = mockMvc.perform(get("/api/question/random"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String result = request.getResponse().getContentAsString();
        QuestionDTO questionResponse = om.readValue(result, QuestionDTO.class);
        assertAll(
                () -> assertEquals(questionResponse.getCategory(), question.getCategory()),
                () -> assertEquals(questionResponse.getName(), question.getName()),
                () -> assertEquals(questionResponse.getImageUrl(), question.getImageUrl()),
                () -> assertEquals(questionResponse.getDifficulty(), question.getDifficulty()),
                () -> assertEquals(questionResponse.getId(), question.getId()),
                () -> assertEquals(questionResponse.getDescription(), question.getDescription())
        );
    }

    @Test
    public void getRandomQuestionWhenNullFromRepository() throws Exception {
        Mockito.when(questionRepository.getRandomQuestion())
                .thenReturn(Optional.ofNullable(null));
        MvcResult request = mockMvc.perform(get("/api/question/random"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String result = request.getResponse().getContentAsString();
        Response response = om.readValue(result, Response.class);
        assertEquals(response.getMessage(), "Question list is empty");
    }

    @Test
    void addQuestion() throws Exception {
        Mockito.when(questionRepository.save(question)).thenReturn(question);
        String jsonItem = om.writeValueAsString(question);
        MvcResult result = mockMvc
                .perform(
                        post("/api/question/add")
                                .content(jsonItem)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String resultContent = result.getResponse().getContentAsString();
        Response response = om.readValue(resultContent, Response.class);
        assertEquals("Question is added",response.getMessage());
    }

    @Test
    void addQuestionWhenIsExist() throws Exception {
        Example<Question> example = Example.of(question, ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withIgnoreCase("name", "description", "imageUrl"));
        Mockito.when(questionRepository.exists(example)).thenReturn(true);
        String jsonItem = om.writeValueAsString(question);
        MvcResult result = mockMvc
                .perform(
                        post("/api/question/add")
                                .content(jsonItem)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        String resultContent = result.getResponse().getContentAsString();
        Response response = om.readValue(resultContent, Response.class);
        assertEquals("Question is exist",response.getMessage());
    }

    @Test
    void addBadQuestionWithoutSomeFields() throws Exception {
        question.setName(null);
        String jsonItem = om.writeValueAsString(question);
        MvcResult result = mockMvc
                .perform(
                        post("/api/question/add")
                                .content(jsonItem)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}