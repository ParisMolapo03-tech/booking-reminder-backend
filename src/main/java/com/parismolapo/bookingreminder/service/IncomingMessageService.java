package com.parismolapo.bookingreminder.service;

import com.parismolapo.bookingreminder.dto.IncomingMessageDto;
import com.parismolapo.bookingreminder.response.Response;

public interface IncomingMessageService {

    Response<String> handleIncoming(IncomingMessageDto dto);
}