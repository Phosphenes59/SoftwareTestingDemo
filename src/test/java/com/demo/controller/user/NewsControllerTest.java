package com.demo.controller.user;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.ModelAndViewAssert.assertModelAttributeAvailable;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NewsController.class)
class NewsControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private NewsService newsService;

    @Test
    void newsTest() throws Exception {
        // News news= newsService.findById(newsID);
        // model.addAttribute("news",news);
        // return "news";
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        when(newsService.findById(id)).thenReturn(news);

        ResultActions perform = mockMvc.perform(get("/news").param("newsID", String.valueOf(id)));
        perform.andExpect(status().isOk());
        verify(newsService).findById(id);

        MvcResult mvcResult = mockMvc.perform(get("/news").param("newsID", String.valueOf(id))).andReturn();
        ModelAndView mv = mvcResult.getModelAndView();
        assertAll("", () -> assertModelAttributeAvailable(mv, "news"));
        verify(newsService, times(2)).findById(id);
    }

    @Test
    void news_listTest1() throws Exception {
        // System.out.println("success");
        // Pageable news_pageable= PageRequest.of(page-1,5,
        // Sort.by("time").descending());
        // return newsService.findAll(news_pageable);
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        List<News> news1 = new ArrayList<>();
        news1.add(news);
        Pageable news_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(news1, news_pageable, 1));

        ResultActions perform = mockMvc.perform(get("/news/getNewsList"));
        perform.andExpect(status().isOk());
        verify(newsService).findAll(any());
    }

    @Test
    void news_listTest2() throws Exception {
        // Pageable news_pageable= PageRequest.of(0,5, Sort.by("time").descending());
        // List<News> news_list= newsService.findAll(news_pageable).getContent();
        // model.addAttribute("news_list",news_list);
        // model.addAttribute("total",
        // newsService.findAll(news_pageable).getTotalPages());
        // return "news_list";
        int id = 1;
        String title = "title";
        String content = "this is content";
        LocalDateTime ldt = LocalDateTime.now();
        News news = new News(id, title, content, ldt);
        List<News> news1 = new ArrayList<>();
        news1.add(news);
        Pageable news_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(newsService.findAll(any())).thenReturn(new PageImpl<>(news1, news_pageable, 1));

        ResultActions perform = mockMvc.perform(get("/news_list"));
        perform.andExpect(status().isOk());
        verify(newsService, times(2)).findAll(any());

        MvcResult mvcResult = mockMvc.perform(get("/news_list")).andReturn();
        ModelAndView mv = mvcResult.getModelAndView();
        assertAll("", () -> assertModelAttributeAvailable(mv, "news_list"),
                () -> assertModelAttributeAvailable(mv, "total"));
        verify(newsService, times(4)).findAll(any());
    }
}