package com.barda.infra.event.user;

import com.barda.infra.event.AbstractEvent;
import com.barda.infra.event.EventType;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class UserLogoutEvent extends AbstractEvent {

    @Override
    public EventType getEventType() {
        return EventType.USER_LOGOUT;
    }
}
