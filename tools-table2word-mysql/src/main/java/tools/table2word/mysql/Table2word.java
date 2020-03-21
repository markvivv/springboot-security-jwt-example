package tools.table2word.mysql;

import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
        XWPFDocument doc = new XWPFDocument();
        // -- OR --
        // open an existing empty document with styles already defined
        //XWPFDocument doc = new XWPFDocument(new FileInputStream("base_document.docx"));
        // 设置字段表头
        XWPFParagraph tableHeaderPara = doc.createParagraph();
        tableHeaderPara.setBorderLeft(Borders.SINGLE);
        tableHeaderPara.setBorderTop(Borders.SINGLE);
        tableHeaderPara.setBorderRight(Borders.SINGLE);
        tableHeaderPara.setBorderBottom(Borders.SINGLE);
        tableHeaderPara.setBorderBetween(Borders.SINGLE);
        tableHeaderPara.setAlignment(ParagraphAlignment.CENTER);
        tableHeaderPara.setVerticalAlignment(TextAlignment.CENTER);

        // 设置字段表body
        XWPFParagraph tablePara = doc.createParagraph();
        tablePara.setBorderLeft(Borders.SINGLE);
        tablePara.setBorderTop(Borders.SINGLE);
        tablePara.setBorderRight(Borders.SINGLE);
        tablePara.setBorderBottom(Borders.SINGLE);
        tablePara.setBorderBetween(Borders.SINGLE);
        tablePara.setAlignment(ParagraphAlignment.LEFT);
        tablePara.setVerticalAlignment(TextAlignment.CENTER);

        // 按照表循环读取表结构
        try {
            for (Map<String, Object> tableInfo : allTableNames) {

                // 设置标题
                XWPFParagraph tablenamePara = doc.createParagraph();
                tablenamePara.setStyle("Heading6");
                XWPFRun tablenameTitle = tablenamePara.createRun();

                // 拼接标题名称
                String tableName = MapUtils.getString(tableInfo, "name", "");
                tablenameTitle.setText(new StringBuilder()
                        .append(MapUtils.getString(tableInfo,  "comment", ""))
                        .append("(").append(tableName).append(")").toString()
                        );

                XWPFTable table = doc.createTable();

                // 设置表格头部信息
                XWPFTableRow headerRow = table.createRow();
                for (String headerColumn : headerValArray)  {
                    XWPFTableCell headerCell = headerRow.addNewTableCell();
                    headerCell.setParagraph(tableHeaderPara);
                    headerCell.setColor("DEDEDE");
                    headerCell.setText(headerColumn);
                }

                // 取表的所有字段说明
                List<Map<String, Object>> tableColumns = jdbcTemplate.queryForList(
                        new StringBuilder()
                                .append("SHOW FULL FIELDS FROM ")
                                .append(databaseName).append(".").append(tableName).toString());
                for (Map<String, Object> rowColumns : tableColumns) {
                    XWPFTableRow columnRow = table.createRow();
                    for (String column : headerKeyArray)  {
                        XWPFTableCell headerCell = headerRow.addNewTableCell();
                        headerCell.setParagraph(tablePara);
                        headerCell.setText(MapUtils.getString(rowColumns, column, ""));
                    }
                }
            }

            // write the file
            try (OutputStream out = new FileOutputStream(new StringBuilder()
                    .append(databaseName).append(".docx").toString())) {
                doc.write(out);
            }
        } catch (IOException e) {
            logger.error("初始化文件出错！", e);
        } finally {
            try {
                doc.close();
            } catch (Exception e) {}
        }

    }

}
