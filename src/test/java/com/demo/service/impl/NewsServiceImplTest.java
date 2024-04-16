package com.demo.service.impl;

import com.demo.dao.NewsDao;
import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class NewsServiceImplTest {
    @Autowired
    NewsService newsService;

    @Autowired
    NewsDao newsDao;

    @Test
    public void findAllTest(){
        Pageable pageable = PageRequest.of(0,5, Sort.by("time").descending());
        Page<News> allNews = newsService.findAll(pageable);
        //检查分页信息
        assertEquals(1, allNews.getTotalPages());
        assertEquals(3, allNews.getTotalElements());
        //检查排序及数据正确性
        assertEquals("关于公共体育俱乐部所有室内培训课程", allNews.getContent().get(0).getTitle());
        assertEquals("关于暂停邯郸校区第14周体质测试的通知", allNews.getContent().get(1).getTitle());
        assertEquals("健步走安全提示", allNews.getContent().get(2).getTitle());
    }

    @Test
    @Transactional
    public void findByIdTest1(){
        //id存在时
        News news = newsService.findById(12);
        assertEquals("关于公共体育俱乐部所有室内培训课程", news.getTitle());
    }

    @Test
    @Transactional
    public void findByIdTest2(){
        //id不存在时
        try{
            News news = newsService.findById(12312);
            assertNull(news);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void createTest1(){
        //id不存在时
        News newsMock = new News(0, "create test", "create test", LocalDateTime.now());
        int id = newsService.create(newsMock);
        Optional<News> newsOpMock = newsDao.findById(id);
        newsDao.deleteById(id);
        if(!newsOpMock.isPresent()){
            fail();
        }
        assertEquals("create test", newsOpMock.get().getTitle());
    }

    @Test
    public void createTest2(){
        //id已存在时
        Optional<News> newsOri = newsDao.findById(12);
        assertTrue(newsOri.isPresent());
        News newsCreate = new News(newsOri.get().getNewsID(), "create test1", "create test1", LocalDateTime.now());
        int id = newsService.create(newsCreate);
        Optional<News> newsOPCreate = newsDao.findById(id);
        newsDao.save(newsOri.get());
        if(!newsOPCreate.isPresent()){
            fail();
        }
        assertEquals("关于公共体育俱乐部所有室内培训课程", newsOPCreate.get().getTitle());
        assertEquals("根据邯郸校区体育馆物业办通知，因麻疹疾控需要，体育馆近期限制进出，故11月23日公共体育俱乐部所有室内培训课程暂停，积分不会因此产生变动，恢复日期另行通知。天气逐渐转寒，请各位同学运动后及时保暖，寝室多开窗门，保持空气流通。", newsOPCreate.get().getContent());
    }

    @Test
    public void delByIdTest() {
        News newsMock = new News(0, "create test", "create test", LocalDateTime.now());
        int id = newsService.create(newsMock);
        Optional<News> newsOpMock = newsDao.findById(id);
        assertTrue(newsOpMock.isPresent());
        newsService.delById(id);
        newsOpMock = newsDao.findById(id);
        assertFalse(newsOpMock.isPresent());
    }

    @Test
    public void updateTest1(){
        //id不存在时
        News newsMock = new News(111, "update test", "update test", LocalDateTime.now());
        int id = newsService.create(newsMock);
        Optional<News> newsOpMock = newsDao.findById(id);
        newsDao.deleteById(id);
        if(newsOpMock.isPresent()){
            fail();
        }
    }

    @Test
    @Transactional
    public void updateTest2(){
        //id存在时
        News newsMock = new News(0, "update test1", "update test1", LocalDateTime.now());
        newsMock = newsDao.save(newsMock);
        newsMock.setContent("update!!");
        newsService.update(newsMock);
        newsMock = newsDao.getOne(newsMock.getNewsID());
        assertEquals("update!!", newsMock.getContent());
    }
}
