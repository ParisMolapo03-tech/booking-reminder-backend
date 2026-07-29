package com.parismolapo.bookingreminder.repository.spec;

import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class BookingSpecifications {

    private BookingSpecifications() {
    }

    public static Specification<Booking> forBusiness(Long businessId) {
        return (root, query, cb) ->
                cb.equal(root.get("business").get("id"), businessId);
    }

    public static Specification<Booking> withStatus(BookingStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Booking> from(LocalDateTime from) {
        return (root, query, cb) ->
                from == null ? null
                        : cb.greaterThanOrEqualTo(root.get("appointmentTime"), from);
    }

    public static Specification<Booking> to(LocalDateTime to) {
        return (root, query, cb) ->
                to == null ? null
                        : cb.lessThanOrEqualTo(root.get("appointmentTime"), to);
    }

    public static Specification<Booking> search(String term) {
        return (root, query, cb) -> {
            if (term == null || term.isBlank()) {
                return null;
            }
            String like = "%" + term.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("customer").get("name")), like),
                    cb.like(cb.lower(root.get("customer").get("phoneNumber")), like),
                    cb.like(cb.lower(root.get("service")), like)
            );
        };
    }
}