package com.demo.controller.admin;

import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeAvailable;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@ExtendWith(SpringExtension.class)
@WebMvcTest(AdminNewsController.class)
public class AdminNewsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NewsService newsService;

    @Test
    public void news_manageTest() throws Exception {
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        List<News> news1 = new ArrayList<>();
        news1.add(news);
        Pageable news_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(news1, news_pageable, 1));

        ResultActions perform = mockMvc.perform(get("/news_manage"));
        perform.andExpect(status().isOk());
        verify(newsService).findAll(any());

        MvcResult mvcResult = mockMvc.perform(get("/news_manage")).andReturn();
        ModelAndView mv = mvcResult.getModelAndView();
        assertAll("", () -> assertModelAttributeAvailable(mv, "total"));
        verify(newsService, times(2)).findAll(any());
    }

    @Test
    public void news_addTest() throws Exception {
        // return "/admin/news_add";
        ResultActions perform = mockMvc.perform(get("/news_add"));
        perform.andExpect(status().isOk());
    }

    @Test
    public void news_editTest() throws Exception {
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        when(newsService.findById(1)).thenReturn(new News(id, title, content, ldt));

        ResultActions perform = mockMvc.perform(get("/news_edit").param("newsID", "1"));
        perform.andExpect(status().isOk());
        verify(newsService).findById(1);

        MvcResult mvcResult = mockMvc.perform(get("/news_edit").param("newsID", "1")).andReturn();
        ModelAndView mv = mvcResult.getModelAndView();
        assertAll("", () -> assertModelAttributeAvailable(mv, "news"));
        verify(newsService, times(2)).findById(1);
    }

    @Test
    public void newsListTest() throws Exception {
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        List<News> news1 = new ArrayList<>();
        news1.add(news);
        Pageable news_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(news1, news_pageable, 1));

        ResultActions perform = mockMvc.perform(get("/newsList.do"));
        perform.andExpect(status().isOk());
        verify(newsService).findAll(any());
    }

    @Test
    public void delNewsTest() throws Exception {
        // newsService.delById(newsID);
        // return true;

        doNothing().when(newsService).delById(1);

        ResultActions perform = mockMvc.perform(post("/delNews.do").param("newsID", "1"));
        perform.andExpect(status().isOk());
        verify(newsService).delById(1);
    }

    @Test
    public void modifyNewsTest() throws Exception {
        int id = 1;
        String title = "title";
        String test_title = "test";
        String content = "this is content";
        String test_content = "test pass";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        when(newsService.findById(id)).thenReturn(news);
        doNothing().when(newsService).update(any());

        ResultActions perform = mockMvc.perform(post("/modifyNews.do").param("newsID", Integer.toString(id))
                .param("title", test_title).param("content", test_content));
        perform.andExpect(status().is3xxRedirection());
        verify(newsService).findById(id);
        verify(newsService).update(any());
        assertAll("", () -> assertEquals(news.getTitle(), test_title),
                () -> assertEquals(news.getContent(), test_content));
    }

    @Test
    public void addNewsTest() throws Exception {
        when(newsService.create(any())).thenReturn(1);

        ResultActions perform=mockMvc.perform(post("/addNews.do"));
        perform.andExpect(status().is3xxRedirection());
        verify(newsService).create(any());
    }

}
