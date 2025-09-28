package com.barda.api.common;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.barda.api.home.SessionUserServiceImpl;
import com.barda.domain.organization.model.OrgMember;

import reactor.core.publisher.Mono;

@Primary
@Service
public class SessionUserServiceImplTest extends SessionUserServiceImpl {

    @Override
    public Mono<OrgMember> getVisitorOrgMemberCache() {
        return super.getVisitorOrgMember();
    }
}
