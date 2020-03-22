package examples.spring.project.model1;

import examples.spring.project.Body;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController("/model1")
public class Model1Controller {

    @PostMapping("/add_task")
    ResponseEntity<Body> addTask(@Valid @RequestBody Model1Pojo model1Pojo) {

        return  ResponseEntity.ok(Body.build().ok("任务发布成功", model1Pojo));
    }
}
