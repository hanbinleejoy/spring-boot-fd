package kr.co.joy.eatgo.interfaces;

import kr.co.joy.eatgo.application.EmailNotExistedException;
import kr.co.joy.eatgo.application.UserService;
import kr.co.joy.eatgo.application.PasswordWrongException;
import kr.co.joy.eatgo.domain.User;
import kr.co.joy.eatgo.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(SessionController.class)
class SessionControllerTests {

    @Autowired
    MockMvc mvc;

    @MockBean
    private UserService userService;
    // 올바른 경우
    @Test
    public void createWithValidAttributes() throws Exception {
        Long id = 1004L;
        String name = "Tester";

        String email = "tester@example.com";
        String password = "test";

        User mockUser = User.builder().id(id).name(name).build();
        given(userService.authenticate(email, password)).willReturn(mockUser);

        mvc.perform(post("/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tester@example.com\",\"password\":\"test\" }")
        )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", "/session"))
                .andExpect(content().string(containsString("{\"accessToken\":\"")))
                .andExpect(content().string(containsString(".")));


        verify(userService).authenticate(eq(email), eq(password));
    }
    // 1. 이메일이 존재하지 않는경우
    @Test
    public void createWithNotExistedEmail() throws Exception {
        given(userService.authenticate("x@example.com","test"))
                .willThrow(EmailNotExistedException.class);

        mvc.perform(post("/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"x@example.com\",\"password\":\"test\" }")
        )
                .andExpect(status().isBadRequest());

        verify(userService).authenticate(eq("x@example.com"), eq("test"));
    }
    // 2. 패스워드가 틀린경우
    @Test
    public void createWithWrongPassword() throws Exception {
        given(userService.authenticate("tester@example.com","x"))
                .willThrow(PasswordWrongException.class);

        mvc.perform(post("/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"tester@example.com\",\"password\":\"x\" }")
        )
                .andExpect(status().isBadRequest());

        verify(userService).authenticate(eq("tester@example.com"), eq("x"));
    }
}