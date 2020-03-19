package examples.spring.mybatis.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import examples.spring.mybatis.Body;
import org.apache.commons.collections4.MapUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@Transactional
@RequestMapping("/sample")
public class SampleController {

    private Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private SqlSession sqlSession;

    @RequestMapping("/query_all")
    public ResponseEntity queryAll(@RequestBody(required = false) Map<String, String> param) {

        // 设置分页组件
        PageHelper.startPage(MapUtils.getIntValue(param, "page_num", 1), MapUtils.getIntValue(param, "page_size", 10));
        List<Map<String, Object>> resultList = sqlSession.selectList("SampleData.queryAll");
        logger.debug("查询到 {} 条数据", resultList.size());

        PageInfo<Map<String, Object>> resultPage = new PageInfo<>(resultList);

        // 将分页查询结果包装后返回
        return ok(Body.build().ok("查询所有数据成功。", resultPage));
    }
}
