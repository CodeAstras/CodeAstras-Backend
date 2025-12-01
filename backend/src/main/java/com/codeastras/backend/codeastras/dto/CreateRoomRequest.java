package com.codeastras.backend.codeastras.dto;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class CreateRoomRequest {

    @NotBlank
    private String name;

    private List<String> inviteEmails;

    public CreateRoomRequest(String name, List<String> inviteEmails) {
        this.name = name;
        this.inviteEmails = inviteEmails;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getInviteEmails() {
        return inviteEmails;
    }

    public void setInviteEmails(List<String> inviteEmails) {
        this.inviteEmails = inviteEmails;
    }
}
