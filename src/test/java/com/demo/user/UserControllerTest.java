package com.demo.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testSignUp() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("signup"));
    }

    @Test
    void testLogin() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testLoginCheck() throws Exception {
        String userID = "testUserID";
        String password = "testPassword";
        User user = new User();
        user.setIsadmin(0); // Assuming regular user
        when(userService.checkLogin(anyString(), anyString())).thenReturn(user);

        MvcResult result = mockMvc.perform(post("/loginCheck.do")
                        .param("userID", userID)
                        .param("password", password)
                )
                .andExpect(status().isOk())
                .andReturn();
        String content = result.getResponse().getContentAsString();
        assert content.equals("/index");

        verify(userService, times(1)).checkLogin(userID, password);
    }

    @Test
    void testRegister() throws Exception {
        mockMvc.perform(post("/register.do")
                        .param("userID", "testUserID")
                        .param("userName", "testUserName")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("login"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    void testLogout() throws Exception {
        mockMvc.perform(get("/logout.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void testQuit() throws Exception {
        mockMvc.perform(get("/quit.do"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index"));
    }

    @Test
    void testUpdateUser() throws Exception {
        String userID = "testUserID";
        User user = new User();
        user.setUserID(userID);
        when(userService.findByUserID(userID)).thenReturn(user);

        mockMvc.perform(post("/updateUser.do")
                        .param("userName", "testUserName")
                        .param("userID", userID)
                        .param("passwordNew", "testNewPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_info"));

        verify(userService, times(1)).findByUserID(userID);
        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void testCheckPassword() throws Exception {
        String userID = "testUserID";
        String password = "testPassword";
        User user = new User();
        user.setPassword(password);
        when(userService.findByUserID(userID)).thenReturn(user);

        mockMvc.perform(get("/checkPassword.do")
                        .param("userID", userID)
                        .param("password", password)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).findByUserID(userID);
    }

    @Test
    void testUserInfo() throws Exception {
        mockMvc.perform(get("/user_info"))
                .andExpect(status().isOk())
                .andExpect(view().name("user_info"));
    }
}
