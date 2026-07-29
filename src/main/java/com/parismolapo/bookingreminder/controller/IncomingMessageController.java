package com.parismolapo.bookingreminder.controller;

import com.parismolapo.bookingreminder.dto.IncomingMessageDto;
import com.parismolapo.bookingreminder.response.Response;
import com.parismolapo.bookingreminder.service.IncomingMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class IncomingMessageController {

    private final IncomingMessageService incomingMessageService;

    @PostMapping("/incoming")
    public ResponseEntity<Response<String>> incoming(
            @RequestBody @Valid IncomingMessageDto dto) {
        return ResponseEntity.ok(incomingMessageService.handleIncoming(dto));
    }
}