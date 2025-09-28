package com.barda.infra.event.user;

import com.barda.infra.event.AbstractEvent;
import com.barda.infra.event.EventType;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class UserLoginEvent extends AbstractEvent {

    private final String source;

    @Override
    public EventType getEventType() {
        return EventType.USER_LOGIN;
    }
}
