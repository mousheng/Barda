package com.barda.sdk.webclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.barda.sdk.plugin.common.ssl.DisableVerifySslConfig;
import com.barda.sdk.plugin.common.ssl.SslConfig;
import com.barda.sdk.plugin.common.ssl.SslHelper;
import com.barda.sdk.plugin.common.ssl.VerifySelfSignedCertSslConfig;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.SslProvider;
import reactor.netty.transport.ProxyProvider.Proxy;

/**
 * 一个帮助构建 {@link WebClient} 的辅助类。
 * 该类使用建造者模式来构建 {@link WebClient}，并提供配置 SSL、代理和禁止的主机等功能。
 */
@Slf4j
public class WebClientBuildHelper {

    /**
     * 系统属性 "http.proxyHost" 的值。
     */
    private static final String proxyHost;

    /**
     * 系统属性 "http.proxyPort" 的值。
     */
    private static final String proxyPortStr;

    /**
     * SSL 配置。
     */
    private SslConfig sslConfig;

    /**
     * 禁止的主机列表。
     */
    private Set<String> disallowedHosts;

    /**
     * 是否使用系统代理。
     */
    private boolean systemProxy;

    static {
        proxyHost = System.getProperty("http.proxyHost");
        proxyPortStr = System.getProperty("http.proxyPort");
    }

    private WebClientBuildHelper() {
    }

    /**
     * 获取 {@link WebClientBuildHelper} 的一个实例。
     *
     * @return {@link WebClientBuildHelper} 的一个实例
     */
    public static WebClientBuildHelper builder() {
        return new WebClientBuildHelper();
    }

    /**
     * 设置 SSL 配置。
     *
     * @param sslConfig SSL 配置
     * @return {@link WebClientBuildHelper}
     */
    public WebClientBuildHelper sslConfig(SslConfig sslConfig) {
        this.sslConfig = sslConfig;
        return this;
    }

    /**
     * 设置禁止的主机列表。
     *
     * @param disallowedHosts 禁止的主机列表
     * @return {@link WebClientBuildHelper}
     */
    public WebClientBuildHelper disallowedHosts(Set<String> disallowedHosts) {
        this.disallowedHosts = disallowedHosts;
        return this;
    }

    /**
     * 启用系统代理。
     *
     * @return {@link WebClientBuildHelper}
     */
    public WebClientBuildHelper systemProxy() {
        this.systemProxy = true;
        return this;
    }

    /**
     * 构建 {@link WebClient}。
     *
     * @return {@link WebClient}
     */
    public WebClient build() {
        return toWebClientBuilder().build();
    }

    /**
     * 获取 {@link WebClient.Builder}。
     *
     * @return {@link WebClient.Builder}
     */
    public Builder toWebClientBuilder() {
        HttpClient httpClient = HttpClient.create();
        if (sslConfig != null) {
            if (sslConfig instanceof DisableVerifySslConfig) {
                httpClient = httpClient.secure(sslProviderWithoutCertVerify());
            }
            if (sslConfig instanceof VerifySelfSignedCertSslConfig verifySelfSignedCertSslConfig) {
                httpClient = httpClient.secure(sslProviderWithSelfSignedCert(verifySelfSignedCertSslConfig));
            }
        }
        if (systemProxy && StringUtils.isNoneBlank(proxyHost, proxyPortStr)) {
            httpClient = httpClient.proxy(typeSpec -> typeSpec.type(Proxy.HTTP)
                    .host(proxyHost)
                    .port(Integer.parseInt(proxyPortStr)));
        }
        if (CollectionUtils.isNotEmpty(disallowedHosts)) {
            httpClient = httpClient.resolver(new SafeHostResolverGroup(disallowedHosts));
        }
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * 获取一个使用自签名证书的 SSL 提供者。
     *
     * @param verifySelfSignedCertSslConfig 自签名证书 SSL 配置
     * @return SSL 提供者
     */
    private static SslProvider sslProviderWithSelfSignedCert(VerifySelfSignedCertSslConfig verifySelfSignedCertSslConfig) {
        try {
            X509Certificate x509Certificate = SslHelper.parseCertificate(verifySelfSignedCertSslConfig.getSelfSignedCert());
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(x509Certificate)
                    .build();
            return SslProvider.builder()
                    .sslContext(sslContext)
                    .build();
        } catch (CertificateException | SSLException e) {
            log.error("解析证书出错", e);
            return SslProvider.defaultClientProvider();
        }
    }

    /**
     * 获取一个不验证证书的 SSL 提供者。
     *
     * @return SSL 提供者
     */
    private static SslProvider sslProviderWithoutCertVerify() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            return SslProvider.builder()
                    .sslContext(sslContext)
                    .build();
        } catch (SSLException e) {
            return SslProvider.defaultClientProvider();
        }
    }
}
