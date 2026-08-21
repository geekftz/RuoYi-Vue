package com.ruoyi.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import com.ruoyi.common.utils.poi.ExcelHandlerAdapter;

/**
 * 自定义导出Excel数据注解
 * <p>
 * 【作用】标在实体字段上，声明该字段参与Excel导入/导出及导出时的列名、格式、字典转换等，
 *         由ExcelUtil反射解析本注解生成/解析Excel（底层基于Apache POI）。
 * 【典型用法】@Excel(name = "用户性别", readConverterExp = "0=男,1=女") —— 导出时0自动显示为"男"。
 * 【调用链路】Controller调ExcelUtil.exportExcel(list) → 反射读取实体上所有@Excel字段 →
 *             按sort排序生成列头 → 逐行写字段值（含字典/表达式/日期转换）→ 输出xlsx文件流给前端下载。
 * 【属性速查】name列名 / type导出还是导入 / dictType字典自动翻译 / readConverterExp手动值映射 /
 *             targetAttr取关联对象属性 / dateFormat日期格式 / cellType数字文本图片 / combo下拉框。
 * 
 * @author ruoyi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Excel
{
    /**
     * 导出时在excel中排序
     */
    public int sort() default Integer.MAX_VALUE;

    /**
     * 导出到Excel中的名字.
     */
    public String name() default "";

    /**
     * 日期格式, 如: yyyy-MM-dd
     */
    public String dateFormat() default "";

    /**
     * 如果是字典类型，请设置字典的type值 (如: sys_user_sex)
     * <p>
     * 【与readConverterExp区别】dictType从sys_dict_data表动态取翻译（字典改了导出自动跟着变）；
     * readConverterExp是写死的映射。推荐用dictType，字典统一走后台管理。
     */
    public String dictType() default "";

    /**
     * 读取内容转表达式 (如: 0=男,1=女,2=未知)
     */
    public String readConverterExp() default "";

    /**
     * 分隔符，读取字符串组内容
     */
    public String separator() default ",";

    /**
     * BigDecimal 精度 默认:-1(默认不开启BigDecimal格式化)
     */
    public int scale() default -1;

    /**
     * BigDecimal 舍入规则 默认:BigDecimal.ROUND_HALF_EVEN
     */
    public int roundingMode() default BigDecimal.ROUND_HALF_EVEN;

    /**
     * 导出时在excel中每个列的高度
     */
    public double height() default 14;

    /**
     * 导出时在excel中每个列的宽度
     */
    public double width() default 16;

    /**
     * 文字后缀,如% 90 变成90%
     */
    public String suffix() default "";

    /**
     * 当值为空时,字段的默认值
     */
    public String defaultValue() default "";

    /**
     * 提示信息
     */
    public String prompt() default "";

    /**
     * 是否允许内容换行 
     */
    public boolean wrapText() default false;

    /**
     * 设置只能选择不能输入的列内容.
     */
    public String[] combo() default {};

    /**
     * 是否从字典读数据到combo,默认不读取,如读取需要设置dictType注解.
     */
    public boolean comboReadDict() default false;

    /**
     * 是否需要纵向合并单元格,应对需求:含有list集合单元格)
     */
    public boolean needMerge() default false;

    /**
     * 是否导出数据,应对需求:有时我们需要导出一份模板,这是标题需要但内容需要用户手工填写.
     */
    public boolean isExport() default true;

    /**
     * 另一个类中的属性名称,支持多级获取,以小数点隔开
     * <p>
     * 【使用场景】字段是关联对象时拆列导出：SysUser的dept字段配@Excel(name="部门名称", targetAttr="deptName")，
     * 导出时取dept.getDeptName()的值。多层级如targetAttr="dept.parent.deptName"。
     */
    public String targetAttr() default "";

    /**
     * 是否自动统计数据,在最后追加一行统计数据总和
     */
    public boolean isStatistics() default false;

    /**
     * 导出类型（0数字 1字符串 2图片）
     */
    public ColumnType cellType() default ColumnType.STRING;

    /**
     * 导出列头背景颜色
     */
    public IndexedColors headerBackgroundColor() default IndexedColors.GREY_50_PERCENT;

    /**
     * 导出列头字体颜色
     */
    public IndexedColors headerColor() default IndexedColors.WHITE;

    /**
     * 导出单元格背景颜色
     */
    public IndexedColors backgroundColor() default IndexedColors.WHITE;

    /**
     * 导出单元格字体颜色
     */
    public IndexedColors color() default IndexedColors.BLACK;

    /**
     * 导出字段对齐方式
     */
    public HorizontalAlignment align() default HorizontalAlignment.CENTER;

    /**
     * 自定义数据处理器
     */
    public Class<?> handler() default ExcelHandlerAdapter.class;

    /**
     * 自定义数据处理器参数
     */
    public String[] args() default {};

    /**
     * 字段类型（0：导出导入；1：仅导出；2：仅导入）
     * <p>
     * 【典型场景】主键ID列通常type=EXPORT（导出给用户看，但导入时不用传，由数据库自增）；
     * 部门编号列通常type=IMPORT（导入时定位部门，导出无意义）。
     */
    Type type() default Type.ALL;

    public enum Type
    {
        ALL(0), EXPORT(1), IMPORT(2);
        private final int value;

        Type(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }

    public enum ColumnType
    {
        NUMERIC(0), STRING(1), IMAGE(2), TEXT(3);
        private final int value;

        ColumnType(int value)
        {
            this.value = value;
        }

        public int value()
        {
            return this.value;
        }
    }
}