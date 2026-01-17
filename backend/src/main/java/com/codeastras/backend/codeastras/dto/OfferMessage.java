package com.codeastras.backend.codeastras.dto;

import com.codeastras.backend.codeastras.dto.chat.CallSignalMessage;
import lombok.Data;

@Data
public class OfferMessage extends CallSignalMessage {
    private String sdp;
}
