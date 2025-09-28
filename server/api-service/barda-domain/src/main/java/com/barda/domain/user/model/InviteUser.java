package com.barda.domain.user.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * 邀请用户类，继承自User类，并使用MongoDB的Document注解。
 */
@Setter
@Getter
@Document
public class InviteUser extends User {

    /**
     * 邀请者的用户ID
     */
    String inviterUserId;

    /**
     * 邀请链接的token
     */
    String token;

}