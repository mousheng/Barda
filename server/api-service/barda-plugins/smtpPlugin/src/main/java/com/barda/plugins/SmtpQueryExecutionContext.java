package com.barda.plugins;

import java.util.List;

import javax.mail.internet.InternetAddress;

import com.barda.sdk.query.QueryExecutionContext;

import lombok.Builder;
import lombok.Getter;

/**
 * 一个表示SMTP查询执行上下文的类。
 * 它继承自 {@link QueryExecutionContext}，并使用Lombok库的注解来生成getter方法和构建器。
 */
@Getter
@Builder
public class SmtpQueryExecutionContext extends QueryExecutionContext {

    /**
     * 发送电子邮件的发件人。
     */
    private final InternetAddress from;

    /**
     * 电子邮件的收件人。
     */
    private final InternetAddress[] to;

    /**
     * 电子邮件的抄送人。
     */
    private final InternetAddress[] cc;

    /**
     * 电子邮件的密送人。
     */
    private final InternetAddress[] bcc;

    /**
     * 电子邮件的回复地址。
     */
    private final InternetAddress[] replyTo;

    /**
     * 电子邮件的主题。
     */
    private final String subject;

    /**
     * 电子邮件的内容。
     */
    private final String content;

    /**
     * 电子邮件的附件。
     */
    private final List<Attachment> attachments;

    /**
     * 一个表示电子邮件附件的记录类。
     *
     * @param name 附件的名称
     * @param contentType 附件的MIME类型
     * @param content 附件的内容，以base64编码
     */
    public record Attachment(String name, String contentType, String content) {
    }
}
