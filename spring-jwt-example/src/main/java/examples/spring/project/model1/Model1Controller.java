package examples.spring.project.model1;

import examples.spring.project.Body;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController("/model1")
public class Model1Controller {

    private Logger logger = LogManager.getLogger();

    @Autowired
    private SqlSession sqlSession;

    @PostMapping("/add_task")
    ResponseEntity<Body> addTask(@Valid @RequestBody Model1Pojo model1Pojo) {
        sqlSession.insert("Model1.addTask");
        return ResponseEntity.ok(Body.build().ok("任务发布成功", model1Pojo));
    }
}
