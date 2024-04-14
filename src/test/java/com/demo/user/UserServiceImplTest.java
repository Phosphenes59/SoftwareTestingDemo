package com.demo.user;

import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServicelTest {

    @Autowired
    private UserService userService;

    @Test
    void testFindByUserID() {
        String userID = "exampleUserID";
        User user = userService.findByUserID(userID);
        assertNotNull(user);
        assertEquals(userID, user.getUserID());
    }

    @Test
    void testFindById() {
        int id = 1;
        User user = userService.findById(id);
        assertNotNull(user);
        assertEquals(id, user.getId());
    }

    @Test
    void testFindByUserIDWithPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = userService.findByUserID(pageable);
        assertNotNull(userPage);
        assertTrue(userPage.hasContent());
    }

    @Test
    void testCheckLogin() {
        String userID = "exampleUserID";
        String password = "examplePassword";
        User user = userService.checkLogin(userID, password);
        assertNotNull(user);
        assertEquals(userID, user.getUserID());
        assertEquals(password, user.getPassword());
    }

    @Test
    void testCreate() {
        User user = new User();
        user.setUserID("testUserID");
        user.setPassword("testPassword");
        int result = userService.create(user);
        assertEquals(1, result);
    }

    @Test
    void testDelByID() {
        int id = 1;
        userService.delByID(id);
        assertNull(userService.findById(id));
    }

    @Test
    void testUpdateUser() {
        int id = 1;
        User user = userService.findById(id);
        assertNotNull(user);
        user.setPassword("newPassword");
        userService.updateUser(user);
        assertEquals("newPassword", userService.findById(id).getPassword());
    }

    @Test
    void testCountUserID() {
        String userID = "exampleUserID";
        int count = userService.countUserID(userID);
        assertEquals(1, count);
    }
    
}
