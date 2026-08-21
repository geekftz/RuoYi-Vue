package com.ruoyi.common.utils.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Excel数据格式处理适配器
 * <p>
 * 【架构位置】ruoyi-common → utils → poi，Excel 导入导出的「自定义格式化扩展点」（SPI 思路）。
 * 【解决什么问题】当 @Excel 注解自带的 readConverterExp/dictType 无法满足复杂转换时
 * （例如导出时把 userId 换成「部门+姓名」拼接串、导入时把中文描述反查成字典值），
 * 在实体字段上配置 @Excel(handlerAdapter = 自定义Adapter.class)，ExcelUtil 读写该字段时反射调用本接口的 format()。
 * 【用法示例】
 * <pre>
 * public class DeptNameAdapter implements ExcelHandlerAdapter {
 *     public Object format(Object value, String[] args, Cell cell, Workbook wb) {
 *         return "前缀-" + value; // 自定义加工
 *     }
 * }
 * // 实体字段：@Excel(name = "部门", handlerAdapter = DeptNameAdapter.class)
 * </pre>
 * 【入参说明】value=字段原始值；args=@Excel 注解的 args() 传参；cell/wb=POI 原生对象，可做样式级定制。
 * 
 * @author ruoyi
 */
public interface ExcelHandlerAdapter
{
    /**
     * 格式化
     * 
     * @param value 单元格数据值
     * @param args excel注解args参数组
     * @param cell 单元格对象
     * @param wb 工作簿对象
     *
     * @return 处理后的值
     */
    Object format(Object value, String[] args, Cell cell, Workbook wb);
}
