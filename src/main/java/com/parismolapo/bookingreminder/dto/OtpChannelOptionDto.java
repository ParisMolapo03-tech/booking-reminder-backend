package com.parismolapo.bookingreminder.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpChannelOptionDto {

    /** EMAIL or WHATSAPP */
    private String channel;

    /** Masked destination, e.g. "p****a@g****.co.za" or "Number ending **33" */
    private String maskedDestination;
}