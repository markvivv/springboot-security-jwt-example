package tools.table2word.mysql;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class Table2word {

    private Logger logger = LogManager.getLogger(getClass());

    private String[] headerValArray = {"字段名", "类型", "允许为空", "键/索引", "默认值", "附加默认值", "注释"};
    private String[] headerKeyArray = {"field", "type", "null", "key", "default", "extra", "comment"};

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${database.name}")
    private String databaseName;

    @PostConstruct
    public void initial() {

        // 获取数据库的所有表
        List<Map<String, Object>> allTableNames = jdbcTemplate.queryForList(
                "SHOW TABLE STATUS from " + databaseName);

        // Create a new document from scratch
        try (XWPFDocument doc = new XWPFDocument()) {
            // 按照表循环读取表结构
            for (Map<String, Object> tableInfo : allTableNames) {

                // 设置标题
                XWPFParagraph tablenamePara = doc.createParagraph();
                tablenamePara.setStyle("Heading6");
                XWPFRun tablenameTitle = tablenamePara.createRun();

                // 拼接标题名称
                String tableName = MapUtils.getString(tableInfo, "name", "");
                tablenameTitle.setText(MapUtils.getString(tableInfo, "comment", "") +
                                "(" + tableName + ")"
                        );

                try {
                    // 取表的所有字段说明
                    List<Map<String, Object>> tableColumns = jdbcTemplate.queryForList(
                            "SHOW FULL FIELDS FROM " +
                                    databaseName + "." + tableName);

                    // 因为有表头，所以table的行数等于数据行数+1
                    XWPFTable table = doc.createTable(tableColumns.size() + 1, 7);

                    // 表格属性
                    CTTblPr tablePr = table.getCTTbl().addNewTblPr();
                    // 表格宽度
                    CTTblWidth width = tablePr.addNewTblW();
                    width.setW(BigInteger.valueOf(8200));
                    // 设置表格宽度为非自动
                    width.setType(STTblWidth.DXA);

                    // 表头行
                    XWPFTableRow headRow = table.getRow(0);
                    for (int hvaIdx = 0, hvaSize = headerValArray.length; hvaIdx < hvaSize; hvaIdx++)  {
                        XWPFTableCell headCell = headRow.getCell(hvaIdx);
                        XWPFParagraph p = headCell.addParagraph();
                        XWPFRun headRun0 = p.createRun();
                        headRun0.setText(headerValArray[hvaIdx]);
                        headRun0.setFontSize(12);
                        headRun0.setBold(true);//是否粗体
                        headCell.setColor("DEDEDE");
                        // 垂直居中
                        p.setVerticalAlignment(TextAlignment.CENTER);
                        // 水平居中
                        p.setAlignment(ParagraphAlignment.CENTER);
                    }

                    // 表主体
                    int tableRowIdx = 1;
                    for (Map<String, Object> rowColumns : tableColumns) {
                        XWPFTableRow columnRow = table.getRow(tableRowIdx++);
                        for (int colIdx = 0, colSize = headerKeyArray.length; colIdx < colSize; colIdx++) {
                            XWPFTableCell dataCell = columnRow.getCell(colIdx);
                            XWPFParagraph p =  dataCell.addParagraph();
                            XWPFRun pRun = p.createRun();
                            pRun.setText(MapUtils.getString(rowColumns, headerKeyArray[colIdx], ""));

                            // 垂直居中
                            dataCell.setVerticalAlignment(XWPFTableCell.XWPFVertAlign.CENTER);
                            if (colIdx == colSize - 1) {
                                // 最后一列左对齐
                                p.setAlignment(ParagraphAlignment.LEFT);
                            } else {
                                // 水平居中
                                p.setAlignment(ParagraphAlignment.CENTER);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("表结构信息读取出错！", e);
                }
            }

            // write the file
            try (OutputStream out = new FileOutputStream(databaseName + ".docx")) {
                doc.write(out);
            }
        } catch (IOException e) {
            logger.error("初始化文件出错！", e);
        }

    }

}
