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
 * 工单AI回复建议实体。
 */
@Data
@TableName("ticket_reply_suggestion")
public class TicketReplySuggestionEntity {

    /**
     * 主键ID。
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 工单ID。
     */
    @TableField("ticket_id")
    private Long ticketId;

    /**
     * 建议回复内容。
     */
    @TableField("suggestion_text")
    private String suggestionText;

    /**
     * 引用来源JSON。
     */
    @TableField("citation_json")
    private String citationJson;

    /**
     * 置信度。
     */
    private BigDecimal confidence;

    /**
     * 是否采纳标识。
     */
    @TableField("accepted_flag")
    private Integer acceptedFlag;

    /**
     * 创建时间。
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

}
