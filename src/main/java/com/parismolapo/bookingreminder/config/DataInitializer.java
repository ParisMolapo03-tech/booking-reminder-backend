package com.parismolapo.bookingreminder.config;

import com.parismolapo.bookingreminder.entity.Booking;
import com.parismolapo.bookingreminder.entity.BookingStatus;
import com.parismolapo.bookingreminder.entity.Business;
import com.parismolapo.bookingreminder.entity.Customer;
import com.parismolapo.bookingreminder.repository.BookingRepository;
import com.parismolapo.bookingreminder.repository.BusinessRepository;
import com.parismolapo.bookingreminder.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-demo-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final BusinessRepository businessRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    @Override
    public void run(String... args) {

        if (businessRepository.count() > 0) {
            log.info("Demo data already present, skipping seed");
            return;
        }

        log.info("Seeding demo data");

        Business salon = businessRepository.save(Business.builder()
                .name("Naledi Hair Studio")
                .whatsappNumber("+27821110000")
                .ownerName("Naledi Dlamini")
                .ownerPhoneNumber("+27821110001")
                .active(true)
                .build());

        Business barber = businessRepository.save(Business.builder()
                .name("Sharp Cuts Barbershop")
                .whatsappNumber("+27833330000")
                .ownerName("Tebogo Mabaso")
                .ownerPhoneNumber("+27833330001")
                .active(true)
                .build());

        Customer thandi  = saveCustomer("Thandi Mokoena", "0821234567", false);
        Customer sipho   = saveCustomer("Sipho Ndlovu", "0731234567", false);
        Customer lerato  = saveCustomer("Lerato Khumalo", "0761234567", false);
        Customer nomsa   = saveCustomer("Nomsa Dube", "0824445555", false);
        Customer kabelo  = saveCustomer("Kabelo Sithole", "0791112222", false);
        Customer zanele  = saveCustomer("Zanele Mthembu", "0723334444", false);
        Customer ayanda  = saveCustomer("Ayanda Ngcobo", "0845556666", true);
        Customer bongani = saveCustomer("Bongani Zulu", "0716667777", false);

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Booking> bookings = List.of(
                booking(salon, thandi,  tomorrow, LocalTime.of(9, 0),
                        "Cut and colour", BookingStatus.CONFIRMED, true),

                booking(salon, zanele,  tomorrow, LocalTime.of(10, 30),
                        "Braids", BookingStatus.CONFIRMED, true),

                booking(salon, lerato,  tomorrow, LocalTime.of(12, 0),
                        "Blow dry", BookingStatus.PENDING, true),

                booking(salon, nomsa,   tomorrow, LocalTime.of(13, 30),
                        "Manicure", BookingStatus.CANCELLED, true),

                booking(salon, ayanda,  tomorrow, LocalTime.of(15, 0),
                        "Treatment", BookingStatus.PENDING, false),

                booking(salon, bongani, tomorrow, LocalTime.of(16, 30),
                        "Cut", BookingStatus.CONFIRMED, true),

                booking(barber, sipho,  tomorrow, LocalTime.of(9, 30),
                        "Beard trim", BookingStatus.CONFIRMED, true),

                booking(barber, kabelo, tomorrow, LocalTime.of(11, 0),
                        "Fade", BookingStatus.PENDING, true)
        );

        bookingRepository.saveAll(bookings);

        log.info("Seeded {} businesses, {} customers, {} bookings",
                businessRepository.count(),
                customerRepository.count(),
                bookingRepository.count());
    }

    private Customer saveCustomer(String name, String phone, boolean optedOut) {
        return customerRepository.save(Customer.builder()
                .name(name)
                .phoneNumber(phone)
                .optedOut(optedOut)
                .build());
    }

    private Booking booking(Business business,
                            Customer customer,
                            LocalDate date,
                            LocalTime time,
                            String service,
                            BookingStatus status,
                            boolean reminderSent) {

        return Booking.builder()
                .business(business)
                .customer(customer)
                .appointmentTime(LocalDateTime.of(date, time))
                .service(service)
                .status(status)
                .reminderSent(reminderSent)
                .reminderSentAt(reminderSent ? LocalDateTime.now().minusHours(2) : null)
                .build();
    }
}