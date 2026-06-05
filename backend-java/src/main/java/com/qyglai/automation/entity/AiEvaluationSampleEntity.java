package com.qyglai.automation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI效果评测样本实体。
 */
@Data
@TableName("ai_evaluation_sample")
public class AiEvaluationSampleEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 业务场景。
     */
    private String scenario;

    /**
     * 输入文本。
     */
    @TableField("input_text")
    private String inputText;

    /**
     * 期望输出JSON。
     */
    @TableField("expected_output")
    private String expectedOutput;

    /**
     * 实际输出JSON。
     */
    @TableField("actual_output")
    private String actualOutput;

    /**
     * 评测分数。
     */
    private BigDecimal score;

    /**
     * 评测人用户ID。
     */
    @TableField("reviewer_user_id")
    private Long reviewerUserId;

    /**
     * 评测备注。
     */
    @TableField("review_comment")
    private String reviewComment;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

}
