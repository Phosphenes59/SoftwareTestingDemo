package com.demo.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testUserManage() throws Exception {
        Page<User> usersPage = new PageImpl<>(new ArrayList<>());
        when(userService.findByUserID(any(Pageable.class))).thenReturn(usersPage);

        mockMvc.perform(get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_manage"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void testUserAdd() throws Exception {
        mockMvc.perform(get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_add"));
    }

    @Test
    void testUserList() throws Exception {
        Page<User> usersPage = new PageImpl<>(new ArrayList<>());
        when(userService.findByUserID(any(Pageable.class))).thenReturn(usersPage);

        mockMvc.perform(get("/userList.do").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(usersPage.getContent().size()));
    }

    @Test
    void testUserEdit() throws Exception {
        int id = 1;
        User user = new User();
        user.setId(id);
        when(userService.findById(id)).thenReturn(user);

        mockMvc.perform(get("/user_edit").param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/user_edit"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void testModifyUser() throws Exception {
        User user = new User();
        when(userService.findByUserID(anyString())).thenReturn(user);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", "testUserID")
                        .param("oldUserID", "oldTestUserID")
                        .param("userName", "testUserName")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService, times(1)).findByUserID(anyString());
        verify(userService, times(1)).updateUser(any(User.class));
    }

    @Test
    void testAddUser() throws Exception {
        mockMvc.perform(post("/addUser.do")
                        .param("userID", "testUserID")
                        .param("userName", "testUserName")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("user_manage"));

        verify(userService, times(1)).create(any(User.class));
    }

    @Test
    void testCheckUserID() throws Exception {
        when(userService.countUserID(anyString())).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do")
                        .param("userID", "testUserID")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).countUserID(anyString());
    }

    @Test
    void testDelUser() throws Exception {
        mockMvc.perform(post("/delUser.do")
                        .param("id", "1")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(userService, times(1)).delByID(anyInt());
    }
}
