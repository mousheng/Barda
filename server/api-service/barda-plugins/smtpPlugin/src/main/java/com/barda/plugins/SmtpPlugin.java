package com.barda.plugins;

import static com.barda.sdk.exception.BizError.DATASOURCE_CLOSE_FAILED;
import static com.barda.sdk.exception.PluginCommonError.DATASOURCE_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_ARGUMENT_ERROR;
import static com.barda.sdk.exception.PluginCommonError.QUERY_EXECUTION_ERROR;
import static com.barda.sdk.plugin.common.QueryExecutionUtils.querySharedScheduler;
import static com.barda.sdk.util.ExceptionUtils.ofPluginException;
import static com.barda.sdk.util.JsonUtils.fromJsonList;
import static com.barda.sdk.util.JsonUtils.toJson;
import static com.barda.sdk.util.MustacheHelper.renderMustacheArrayJsonString;
import static com.barda.sdk.util.MustacheHelper.renderMustacheJsonString;
import static com.barda.sdk.util.MustacheHelper.renderMustacheString;
import static javax.mail.Message.RecipientType.BCC;
import static javax.mail.Message.RecipientType.CC;
import static javax.mail.Message.RecipientType.TO;
import static org.apache.commons.collections4.MapUtils.getString;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import javax.activation.DataHandler;
import javax.annotation.Nonnull;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

import com.barda.plugins.SmtpQueryExecutionContext.Attachment;
import com.barda.sdk.exception.BizException;
import com.barda.sdk.exception.PluginException;
import com.barda.sdk.models.DatasourceTestResult;
import com.barda.sdk.models.QueryExecutionResult;
import com.barda.sdk.plugin.common.DatasourceQueryEngine;
import com.barda.sdk.query.QueryVisitorContext;
import com.barda.sdk.util.JsonUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * 一个用于发送电子邮件的SMTP插件。
 * 该插件实现了 {@link DatasourceQueryEngine} 接口，
 * 并使用JavaMail API来创建和管理SMTP会话，并执行发送电子邮件的查询。
 */
public class SmtpPlugin extends Plugin {

    public SmtpPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Slf4j
    @Extension
    public static class SmtpEngine implements DatasourceQueryEngine<SmtpDatasourceConfig, Session, SmtpQueryExecutionContext> {

        private static final int DEFAULT_PORT = 25;

        /**
         * 创建一个新的SMTP会话。
         *
         * @param connectionConfig SMTP数据源配置
         * @return 一个Mono，发出创建的SMTP会话
         */
        @Override
        public Mono<Session> createConnection(SmtpDatasourceConfig connectionConfig) {

            Properties prop = new Properties();
            prop.put("mail.transport.protocol", "smtp");
            prop.put("mail.smtp.host", connectionConfig.getHost());
            prop.put("mail.smtp.port", connectionConfig.getPort() <= 0 ? DEFAULT_PORT : connectionConfig.getPort());
            prop.put("mail.smtp.auth", true);
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

            return Mono.fromSupplier(() ->
                            // 阻塞式调用
                            Session.getInstance(prop, new Authenticator() {
                                @Override
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(connectionConfig.getUsername(), connectionConfig.getPassword());
                                }
                            }))
                    .subscribeOn(querySharedScheduler());
        }

        /**
         * 关闭并销毁SMTP会话。
         *
         * @param session 要关闭的SMTP会话
         * @return 一个Mono，发出关闭操作完成的信号
         */
        @Override
        public Mono<Void> destroyConnection(Session session) {
            return Mono.<Void>fromRunnable(() -> {
                        try {
                            if (session.getTransport() != null) {
                                session.getTransport().close();
                            }
                        } catch (MessagingException e) {
                            throw new BizException(DATASOURCE_CLOSE_FAILED, "DATASOURCE_CLOSE_FAILED", e.getMessage());
                        }
                    })
                    .subscribeOn(querySharedScheduler());
        }

        /**
         * 测试SMTP数据源的连接。
         *
         * @param connectionConfig SMTP数据源配置
         * @return 一个Mono，发出测试结果
         */
        @Override
        public Mono<DatasourceTestResult> testConnection(SmtpDatasourceConfig connectionConfig) {
            Mono<Session> sessionMono = doCreateConnection(connectionConfig).cache();
            return sessionMono
                    .map(session -> {
                        try {
                            session.getTransport().connect();
                            return DatasourceTestResult.testSuccess();
                        } catch (MessagingException e) {
                            return DatasourceTestResult.testFail(e);
                        }
                    })
                    .onErrorResume(throwable -> Mono.just(DatasourceTestResult.testFail(throwable)))
                    .doFinally(signalType -> sessionMono.flatMap(this::destroyConnection).subscribeOn(querySharedScheduler()).subscribe())
                    .subscribeOn(querySharedScheduler());
        }

        /**
         * 解析SMTP数据源配置。
         *
         * @param configMap 包含SMTP数据源配置的Map
         * @return 解析后的SMTP数据源配置
         */
        @Nonnull
        @Override
        public SmtpDatasourceConfig resolveConfig(Map<String, Object> configMap) {
            SmtpDatasourceConfig result = JsonUtils.fromJson(toJson(configMap), SmtpDatasourceConfig.class);
            if (result == null) {
                throw ofPluginException(DATASOURCE_ARGUMENT_ERROR, "INVALID_SMTP_CONFIG");
            }
            return result;
        }

        /**
         * 验证SMTP数据源配置。
         *
         * @param connectionConfig SMTP数据源配置
         * @return 包含验证结果的Set
         */
        @Override
        public Set<String> validateConfig(SmtpDatasourceConfig connectionConfig) {
            Set<String> invalids = new HashSet<>();
            if (isBlank(connectionConfig.getHost())) {
                invalids.add("HOST_EMPTY");
            }
            if (connectionConfig.getPort() < 0) {
                invalids.add("INVALID_PORT");
            }
            return invalids;
        }

        /**
         * 从datasourceConnectionConfig构建查询执行上下文的Mono。
         *
         * @param connectionConfig    数据源连接配置
         * @param queryConfig         查询配置
         * @param requestParams       请求参数
         * @param queryVisitorContext 查询访问器上下文
         * @return 查询执行上下文的Mono
         */
        @Override
        public Mono<SmtpQueryExecutionContext> doBuildQueryExecutionContextMono(SmtpDatasourceConfig connectionConfig, Map<String, Object> queryConfig, Map<String, Object> requestParams, QueryVisitorContext queryVisitorContext) {
            return Mono.just(SmtpQueryExecutionContext.builder()
                    .from(renderAsStringAndParse(getString(queryConfig, "from"), requestParams)[0])
                    .to(renderAsArrayAndParse(getString(queryConfig, "to"), requestParams))
                    .cc(renderAsArrayAndParse(getString(queryConfig, "cc", ""), requestParams))
                    .bcc(renderAsArrayAndParse(getString(queryConfig, "bcc", ""), requestParams))
                    .replyTo(renderAsArrayAndParse(getString(queryConfig, "replyTo", ""), requestParams))
                    .subject(renderMustacheString(getString(queryConfig, "subject"), requestParams))
                    .content(renderMustacheString(getString(queryConfig, "content", ""), requestParams))
                    .attachments(fromJsonList(renderMustacheJsonString(getString(queryConfig, "attachments", ""), requestParams), Attachment.class))
                    .build());
        }

        /**
         * 渲染并解析单个电子邮件地址。
         *
         * @param template 模板字符串
         * @param paramMap 参数Map
         * @return 解析后的电子邮件地址
         */
        private InternetAddress[] renderAsStringAndParse(String template, Map<String, Object> paramMap) {
            if (StringUtils.isBlank(template)) {
                return new InternetAddress[0];
            }
            String rendered = renderMustacheString(template, paramMap);
            return parse(List.of(rendered));
        }

        /**
         * 渲染并解析一组电子邮件地址。
         *
         * @param template 模板字符串
         * @param paramMap 参数Map
         * @return 解析后的电子邮件地址
         */
        private InternetAddress[] renderAsArrayAndParse(String template, Map<String, Object> paramMap) {
            if (StringUtils.isBlank(template)) {
                return new InternetAddress[0];
            }
            String rendered = renderMustacheArrayJsonString(template, paramMap);
            List<String> addresses = fromJsonList(rendered, String.class);
            return parse(addresses);
        }

        /**
         * 解析一组电子邮件地址。
         *
         * @param internetAddresses 要解析的电子邮件地址
         * @return 解析后的电子邮件地址
         */
        private InternetAddress[] parse(List<String> internetAddresses) {
            if (CollectionUtils.isEmpty(internetAddresses)) {
                return new InternetAddress[0];
            }
            return internetAddresses.stream()
                    .map(s -> {
                        try {
                            return InternetAddress.parse(s);
                        } catch (AddressException e) {
                            throw new PluginException(QUERY_ARGUMENT_ERROR, "QUERY_ARGUMENT_ERROR", e.getMessage());
                        }
                    })
                    .flatMap(Stream::of)
                    .toList()
                    .toArray(new InternetAddress[0]);
        }

        /**
         * 执行SMTP查询。
         *
         * @param session               SMTP会话
         * @param queryExecutionContext SMTP查询执行上下文
         * @return 一个Mono，发出查询执行结果
         */
        @Override
        public Mono<QueryExecutionResult> executeQuery(Session session, SmtpQueryExecutionContext queryExecutionContext) {
            return Mono.fromSupplier(() -> {
                        try {
                            Transport.send(buildMessage(session, queryExecutionContext));
                            return QueryExecutionResult.success(null);
                        } catch (MessagingException e) {
                            return QueryExecutionResult.error(QUERY_EXECUTION_ERROR, "QUERY_EXECUTION_ERROR", e.getMessage());
                        }
                    })
                    .subscribeOn(querySharedScheduler());
        }

        /**
         * 构建并发送电子邮件。
         *
         * @param session SMTP会话
         * @param context SMTP查询执行上下文
         * @return 构建的电子邮件
         * @throws MessagingException 可能在构建电子邮件时发生的MessagingException
         */
        private Message buildMessage(Session session, SmtpQueryExecutionContext context) throws MessagingException {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(context.getFrom());
            message.setReplyTo(context.getReplyTo());
            message.setRecipients(TO, context.getTo());
            if (isNotEmpty(context.getCc())) {
                message.setRecipients(CC, context.getCc());
            }
            if (isNotEmpty(context.getBcc())) {
                message.setRecipients(BCC, context.getBcc());
            }
            message.setSubject(context.getSubject());

            Multipart multipart = new MimeMultipart();
            MimeBodyPart contentPart = new MimeBodyPart();
            contentPart.setContent(context.getContent(), "text/html; charset=UTF-8");// default mime type
            multipart.addBodyPart(contentPart);
            if (CollectionUtils.isNotEmpty(context.getAttachments())) {
                Decoder decoder = Base64.getDecoder();
                for (Attachment attachment : context.getAttachments()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.setDataHandler(
                            new DataHandler(new ByteArrayDataSource(decoder.decode(attachment.content()), attachment.contentType())));
                    attachmentPart.setDisposition(Part.ATTACHMENT);
                    attachmentPart.setFileName(attachment.name());
                    multipart.addBodyPart(attachmentPart);
                }
            }
            message.setContent(multipart);
            return message;
        }
    }
}
