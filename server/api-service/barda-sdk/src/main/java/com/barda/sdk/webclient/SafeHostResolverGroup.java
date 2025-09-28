package com.barda.sdk.webclient;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.InetNameResolver;
import io.netty.resolver.InetSocketAddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.SocketUtils;

/**
 * 安全的主机解析器组，用于解析InetSocketAddress并拒绝某些主机。
 */
public class SafeHostResolverGroup extends AddressResolverGroup<InetSocketAddress> {

    /**
     * 禁止的主机列表。
     */
    private final Set<String> disallowedHosts;

    /**
     * 构造函数，用于创建SafeHostResolverGroup。
     *
     * @param disallowedHosts 禁止的主机列表
     */
    public SafeHostResolverGroup(Set<String> disallowedHosts) {
        this.disallowedHosts = disallowedHosts;
    }

    /**
     * {@inheritDoc}
     *
     * 创建新的InetSocketAddress解析器，并使用SafeHostNameResolver。
     */
    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
        return new InetSocketAddressResolver(executor, new SafeHostNameResolver(executor, disallowedHosts));
    }

    /**
     * 安全的主机名解析器，用于拒绝某些主机。
     */
    private static class SafeHostNameResolver extends InetNameResolver {

        /**
         * 禁止的主机列表。
         */
        private final Set<String> disallowedHosts;

        /**
         * 构造函数，用于创建SafeHostNameResolver。
         *
         * @param executor 事件执行器
         * @param disallowedHosts 禁止的主机列表
         */
        public SafeHostNameResolver(EventExecutor executor, Set<String> disallowedHosts) {
            super(executor);
            this.disallowedHosts = disallowedHosts;
        }

        /**
         * {@inheritDoc}
         *
         * 解析主机名并检查是否在禁止的主机列表中。
         * 如果在禁止的主机列表中，则返回UnknownHostException。
         */
        @Override
        protected void doResolve(String inetHost, Promise<InetAddress> promise) {
            if (disallowedHosts.contains(inetHost)) {
                promise.setFailure(new UnknownHostException("Host not allowed."));
                return;
            }

            final InetAddress address;
            try {
                address = SocketUtils.addressByName(inetHost);
            } catch (UnknownHostException e) {
                promise.setFailure(e);
                return;
            }

            if (disallowedHosts.contains(address.getHostAddress())) {
                promise.setFailure(new UnknownHostException("Host not allowed."));
                return;
            }

            promise.setSuccess(address);
        }

        /**
         * {@inheritDoc}
         *
         * 解析主机名并检查是否在禁止的主机列表中。
         * 如果在禁止的主机列表中，则返回UnknownHostException。
         */
        @Override
        protected void doResolveAll(String inetHost, Promise<List<InetAddress>> promise) {
            if (disallowedHosts.contains(inetHost)) {
                promise.setFailure(new UnknownHostException("Host not allowed."));
                return;
            }

            final List<InetAddress> addresses;
            try {
                addresses = Arrays.asList(SocketUtils.allAddressesByName(inetHost));
            } catch (UnknownHostException e) {
                promise.setFailure(e);
                return;
            }

            // 即使列表中包含一个禁止的地址，也会返回UnknownHostException。
            for (InetAddress address : addresses) {
                if (disallowedHosts.contains(address.getHostAddress())) {
                    promise.setFailure(new UnknownHostException("Host not allowed."));
                    return;
                }
            }

            promise.setSuccess(addresses);
        }
    }
}