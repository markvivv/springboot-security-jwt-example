package examples.spring.project.model1;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.Date;

public class Model1Pojo {

    private String taskId;
    @NotBlank(message = "任务名不能为空")
    private String taskName;

    private Date taskStartDate;
    private Date taskEndDate;

    @Min(1)
    @Max(20)
    private int memberNum;
}
